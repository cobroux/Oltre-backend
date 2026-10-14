package io.oltre_backend.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    private final LoginAttemptService service = new LoginAttemptService();

    private void fail(String email, String ip, int times) {
        for (int i = 0; i < times; i++) service.recordFailure(email, ip);
    }

    @Test
    void locksAfterFiveFailuresFromSameIp() {
        fail("me@test.local", "1.1.1.1", 4);
        assertFalse(service.isLocked("me@test.local", "1.1.1.1"));

        fail("me@test.local", "1.1.1.1", 1);
        assertTrue(service.isLocked("me@test.local", "1.1.1.1"));
    }

    @Test
    void failuresFromAnotherIpDoNotLockTheOwner() {
        fail("me@test.local", "6.6.6.6", 10);

        assertTrue(service.isLocked("me@test.local", "6.6.6.6"));
        assertFalse(service.isLocked("me@test.local", "1.1.1.1"));
    }

    @Test
    void emailIsCaseInsensitive() {
        fail("Me@Test.local", "1.1.1.1", 5);
        assertTrue(service.isLocked("me@test.local", "1.1.1.1"));
    }

    @Test
    void successResetsTheCounter() {
        fail("me@test.local", "1.1.1.1", 4);
        service.recordSuccess("me@test.local", "1.1.1.1");
        fail("me@test.local", "1.1.1.1", 4);

        assertFalse(service.isLocked("me@test.local", "1.1.1.1"));
    }
}
