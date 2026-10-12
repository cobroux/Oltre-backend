package io.oltre_backend.coach;

// Erreur lors de l'appel à l'IA ou du traitement de sa réponse (timeout,
// clé invalide, JSON mal formé...) - distincte de RuntimeException pour que
// le controller puisse la mapper en 502 sans intercepter au passage les
// ResponseStatusException (404, 400...) qui doivent garder leur propre code.
public class CoachGenerationException extends RuntimeException {
    public CoachGenerationException(String message) {
        super(message);
    }

    public CoachGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
