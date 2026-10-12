package io.oltre_backend.coach;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

// Client minimal pour l'API Gemini (generateContent) - un seul usage pour
// l'instant (CoachService), donc pas besoin de SDK complet. Choisi pour son
// tier gratuit (contrairement à l'API Anthropic, qui n'en a pas), adapté à
// un usage perso + quelques utilisateurs du site.
@Service
public class GeminiService {

    private final String apiKey;
    private final String model;
    private final RestClient restClient;

    public GeminiService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;

        // Une génération de plan implique un aller-retour LLM, nettement
        // plus lent qu'un simple appel REST - 20s de lecture plutôt que les
        // 3-5s habituels ailleurs dans le backend.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(20000);

        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .requestFactory(requestFactory)
                .build();
    }

    public String complete(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new CoachAiNotConfiguredException(
                    "Coach IA pas encore configuré : aucune clé API Gemini (GEMINI_API_KEY) n'est définie sur le serveur.");
        }

        GenerateContentResponse response;
        try {
            response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .header("content-type", "application/json")
                    .body(Map.of(
                            "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                            "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", userPrompt)))),
                            "generationConfig", Map.of("maxOutputTokens", 1500)
                    ))
                    .retrieve()
                    .body(GenerateContentResponse.class);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new CoachGenerationException("Clé API Gemini invalide ou expirée.");
        } catch (Exception e) {
            throw new CoachGenerationException("Erreur lors de l'appel au Coach IA : " + e.getMessage(), e);
        }

        if (response == null || response.candidates == null || response.candidates.isEmpty()
                || response.candidates.get(0).content == null
                || response.candidates.get(0).content.parts == null
                || response.candidates.get(0).content.parts.isEmpty()) {
            throw new CoachGenerationException("Réponse vide du Coach IA.");
        }
        return response.candidates.get(0).content.parts.get(0).text;
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
