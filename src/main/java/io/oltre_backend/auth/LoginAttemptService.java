package io.oltre_backend.auth;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

// Verrouillage par couple (email, IP) : bloque le brute force depuis une
// même machine sans permettre à un tiers de verrouiller le compte de
// quelqu'un d'autre depuis la sienne (ce que faisait un verrou par email seul).
@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MILLIS = 15 * 60 * 1000;
    // Borne la mémoire : au-delà, on oublie les entrées non verrouillées.
    private static final int MAX_TRACKED = 10_000;

    private record Attempts(int count, long lockedUntil) {}

    private final ConcurrentHashMap<String, Attempts> attempts = new ConcurrentHashMap<>();

    public boolean isLocked(String email, String clientIp) {
        Attempts a = attempts.get(key(email, clientIp));
        return a != null && a.lockedUntil() > System.currentTimeMillis();
    }

    public void recordFailure(String email, String clientIp) {
        if (attempts.size() >= MAX_TRACKED) {
            long now = System.currentTimeMillis();
            attempts.values().removeIf(a -> a.lockedUntil() <= now);
        }
        attempts.compute(key(email, clientIp), (k, a) -> {
            int count = (a == null ? 0 : a.count()) + 1;
            long lockedUntil = count >= MAX_ATTEMPTS ? System.currentTimeMillis() + LOCKOUT_MILLIS : 0;
            return new Attempts(count, lockedUntil);
        });
    }

    public void recordSuccess(String email, String clientIp) {
        attempts.remove(key(email, clientIp));
    }

    private String key(String email, String clientIp) {
        return email.toLowerCase() + "|" + clientIp;
    }
}
