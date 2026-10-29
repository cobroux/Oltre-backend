package io.oltre_backend.coach;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

// Client minimal pour l'API Gemini (generateContent) - un seul usage pour
// l'instant (CoachService), donc pas besoin de SDK complet. Choisi pour son
// tier gratuit (contrairement à l'API Anthropic, qui n'en a pas), adapté à
// un usage perso + quelques utilisateurs du site.
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final String apiKey;
    private final String model;
    private final String fallbackModel;
    private final RestClient restClient;
    private final long[] retryDelaysMs;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Gemini renvoie 503 (modèle saturé) ou 429 lors des pics de charge,
    // généralement pour quelques secondes : on réessaie 2 fois, puis on passe
    // au modèle de secours (souvent moins demandé) avant d'abandonner.
    private static final long[] RETRY_DELAYS_MS = {2000, 5000};

    @Autowired
    public GeminiService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model,
            @Value("${gemini.fallback-model:}") String fallbackModel) {
        this(apiKey, model, fallbackModel, RestClient.builder().requestFactory(requestFactory()), RETRY_DELAYS_MS);
    }

    GeminiService(String apiKey, String model, String fallbackModel, RestClient.Builder builder, long[] retryDelaysMs) {
        this.apiKey = apiKey;
        this.model = model;
        this.fallbackModel = fallbackModel;
        this.retryDelaysMs = retryDelaysMs;
        this.restClient = builder.baseUrl("https://generativelanguage.googleapis.com").build();
    }

    private static SimpleClientHttpRequestFactory requestFactory() {
        // Une génération de plan implique un aller-retour LLM, nettement
        // plus lent qu'un simple appel REST. Aux heures de pointe le palier
        // gratuit met 35-50 s à répondre (mesuré le 29/10/2026) : 90 s de lecture.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(90000);
        return requestFactory;
    }

    public String complete(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new CoachAiNotConfiguredException(
                    "Coach IA pas encore configuré : aucune clé API Gemini (GEMINI_API_KEY) n'est définie sur le serveur.");
        }
        boolean hasFallback = fallbackModel != null && !fallbackModel.isBlank() && !fallbackModel.equals(model);
        try {
            return generate(model, systemPrompt, userPrompt);
        } catch (OverloadedException e) {
            if (!hasFallback) throw e.toCoachError();
            log.warn("Gemini {} saturé, bascule sur le modèle de secours {}", model, fallbackModel);
        }
        try {
            return generate(fallbackModel, systemPrompt, userPrompt);
        } catch (OverloadedException e) {
            throw e.toCoachError();
        }
    }

    private String generate(String model, String systemPrompt, String userPrompt) {

        GenerateContentResponse response;
        for (int attempt = 0; ; attempt++) {
            try {
                // .body(String.class) plutôt que .body(GenerateContentResponse.class) :
                // l'API Gemini répond parfois avec un Content-Type
                // "application/octet-stream" au lieu d'"application/json" (observé en
                // production), ce qui fait échouer la désérialisation automatique de
                // RestClient (elle choisit son convertisseur d'après le Content-Type
                // déclaré, pas le contenu réel). Récupérer une String fonctionne quel
                // que soit le Content-Type, puis on parse nous-mêmes.
                String raw = restClient.post()
                        .uri("/v1beta/models/{model}:generateContent", model)
                        .header("x-goog-api-key", apiKey)
                        .header("content-type", "application/json")
                        .body(Map.of(
                                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", userPrompt)))),
                                // responseMimeType force une sortie JSON stricte (pas de prose
                                // ni de ```fences``` autour) - sans ça Gemini respecte la
                                // consigne "JSON only" de façon moins fiable que Claude, d'où
                                // des parsings qui échouaient en pratique. maxOutputTokens monté
                                // à 2048 : les modèles "flash" récents consomment une partie du
                                // budget en réflexion interne avant la réponse visible, donc
                                // 1500 pouvait couper le JSON en plein milieu.
                                "generationConfig", Map.of(
                                        "maxOutputTokens", 2048,
                                        "responseMimeType", "application/json"
                                )
                        ))
                        .retrieve()
                        .body(String.class);
                response = raw == null ? null : objectMapper.readValue(raw, GenerateContentResponse.class);
                break;
            } catch (HttpServerErrorException.ServiceUnavailable | HttpClientErrorException.TooManyRequests e) {
                if (attempt >= retryDelaysMs.length) throw new OverloadedException(e);
                try {
                    Thread.sleep(retryDelaysMs[attempt]);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new CoachGenerationException("Génération du Coach IA interrompue.", ie);
                }
            } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
                throw new CoachGenerationException("Clé API Gemini invalide ou expirée.");
            } catch (Exception e) {
                throw new CoachGenerationException("Erreur lors de l'appel au Coach IA : " + e.getMessage(), e);
            }
        }

        if (response == null || response.candidates == null || response.candidates.isEmpty()
                || response.candidates.get(0).content == null
                || response.candidates.get(0).content.parts == null
                || response.candidates.get(0).content.parts.isEmpty()) {
            throw new CoachGenerationException("Réponse vide du Coach IA.");
        }
        return response.candidates.get(0).content.parts.get(0).text;
    }

    private static class OverloadedException extends RuntimeException {
        OverloadedException(Exception cause) { super(cause); }

        CoachGenerationException toCoachError() {
            return new CoachGenerationException(
                    "Le Coach IA est surchargé en ce moment (forte demande chez Google). Réessaie dans quelques minutes.", getCause());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GenerateContentResponse {
        public List<Candidate> candidates;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Candidate {
        public Content content;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Content {
        public List<Part> parts;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Part {
        public String text;
    }
}
