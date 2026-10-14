package io.oltre_backend.common;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

// Renvoie le motif des ResponseStatusException au format { "message": ... }
// déjà lu par le frontend (err.error.message). Les autres exceptions gardent
// la réponse d'erreur par défaut de Spring, sans leur message interne.
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException e) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        String message = e.getReason() != null ? e.getReason()
                : status != null ? status.getReasonPhrase() : "Erreur";
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", message));
    }
}
