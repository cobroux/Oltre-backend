package io.oltre_backend.common;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

// Renvoie le motif des ResponseStatusException au format { "message": ... }
// déjà lu par le frontend (err.error.message). Les autres exceptions gardent
// la réponse d'erreur par défaut de Spring, sans leur message interne.
@RestControllerAdvice
public class ApiExceptionHandler {

    // Échec de @Valid : 400 avec le premier message de contrainte.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(err -> err.getDefaultMessage()).findFirst().orElse("Requête invalide");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException e) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        String message = e.getReason() != null ? e.getReason()
                : status != null ? status.getReasonPhrase() : "Erreur";
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", message));
    }
}
