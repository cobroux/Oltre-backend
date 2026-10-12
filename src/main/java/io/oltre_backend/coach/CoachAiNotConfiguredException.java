package io.oltre_backend.coach;

// Levée quand GEMINI_API_KEY n'est pas configurée - le Coach IA reste
// inactif plutôt que de planter avec une erreur obscure (401 de l'API
// Gemini, NullPointerException...).
public class CoachAiNotConfiguredException extends RuntimeException {
    public CoachAiNotConfiguredException(String message) {
        super(message);
    }
}
