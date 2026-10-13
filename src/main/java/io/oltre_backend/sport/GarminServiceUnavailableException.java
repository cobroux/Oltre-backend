package io.oltre_backend.sport;

public class GarminServiceUnavailableException extends RuntimeException {
    public GarminServiceUnavailableException(String message) {
        super(message);
    }
}
