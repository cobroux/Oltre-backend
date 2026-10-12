package io.oltre_backend.coach;

// Levée quand ANTHROPIC_API_KEY n'est pas configurée - le Coach IA reste
// inactif plutôt que de planter avec une erreur obscure (401 de l'API
// Anthropic, NullPointerException...).
public class AnthropicNotConfiguredException extends RuntimeException {
    public AnthropicNotConfiguredException(String message) {
        super(message);
    }
}
