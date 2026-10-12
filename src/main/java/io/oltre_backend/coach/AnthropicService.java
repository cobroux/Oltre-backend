package io.oltre_backend.coach;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

// Client minimal pour l'API Messages d'Anthropic - un seul usage pour
// l'instant (CoachService), donc pas besoin de SDK complet.
@Service
public class AnthropicService {

    private final String apiKey;
    private final String model;
    private final RestClient restClient;

    public AnthropicService(
            @Value("${anthropic.api-key}") String apiKey,
            @Value("${anthropic.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;

        // Une génération de plan implique un aller-retour LLM, nettement
        // plus lent qu'un simple appel REST - 20s de lecture plutôt que les
        // 3-5s habituels ailleurs dans le backend.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(20000);

        this.restClient = RestClient.builder()
                .baseUrl("https://api.anthropic.com")
                .requestFactory(requestFactory)
                .build();
    }

    public String complete(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AnthropicNotConfiguredException(
                    "Coach IA pas encore configuré : aucune clé API Anthropic (ANTHROPIC_API_KEY) n'est définie sur le serveur.");
        }

        MessagesResponse response;
        try {
            response = restClient.post()
                    .uri("/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .body(Map.of(
                            "model", model,
                            "max_tokens", 1500,
                            "system", systemPrompt,
                            "messages", List.of(Map.of("role", "user", "content", userPrompt))
                    ))
                    .retrieve()
                    .body(MessagesResponse.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new CoachGenerationException("Clé API Anthropic invalide ou expirée.");
        } catch (Exception e) {
            throw new CoachGenerationException("Erreur lors de l'appel au Coach IA : " + e.getMessage(), e);
        }

        if (response == null || response.content == null || response.content.isEmpty()) {
            throw new CoachGenerationException("Réponse vide du Coach IA.");
        }
        return response.content.get(0).text;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MessagesResponse {
        public List<ContentBlock> content;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ContentBlock {
        public String text;
    }
}
