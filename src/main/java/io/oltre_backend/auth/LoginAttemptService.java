package io.oltre_backend.auth;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MILLIS = 15 * 60 * 1000;

    private record Attempts(int count, long lockedUntil) {}

    private final ConcurrentHashMap<String, Attempts> attemptsByEmail = new ConcurrentHashMap<>();

    public boolean isLocked(String email) {
        Attempts a = attemptsByEmail.get(key(email));
        return a != null && a.lockedUntil() > System.currentTimeMillis();
    }

    public void recordFailure(String email) {
        attemptsByEmail.compute(key(email), (k, a) -> {
            int count = (a == null ? 0 : a.count()) + 1;
            long lockedUntil = count >= MAX_ATTEMPTS ? System.currentTimeMillis() + LOCKOUT_MILLIS : 0;
            return new Attempts(count, lockedUntil);
        });
    }

    public void recordSuccess(String email) {
        attemptsByEmail.remove(key(email));
    }

    private String key(String email) {
        return email.toLowerCase();
    }
}
