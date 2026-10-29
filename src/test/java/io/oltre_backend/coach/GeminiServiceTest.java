package io.oltre_backend.coach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GeminiServiceTest {

    private static final String OK_BODY = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"{}\"}]}}]}";

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final GeminiService service = new GeminiService("key", "model", "", builder, new long[] {0, 0});

    @Test
    void overloaded_thenOk_retriesAndSucceeds() {
        server.expect(times(2), method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(method(HttpMethod.POST)).andRespond(withSuccess(OK_BODY, MediaType.APPLICATION_JSON));

        assertEquals("{}", service.complete("sys", "user"));
        server.verify();
    }

    @Test
    void stillOverloaded_afterRetries_givesReadableMessage() {
        server.expect(times(3), method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        CoachGenerationException e = assertThrows(CoachGenerationException.class, () -> service.complete("sys", "user"));
        assertTrue(e.getMessage().contains("surchargé"), e.getMessage());
        server.verify();
    }

    @Test
    void primaryStillOverloaded_fallsBackToSecondModel() {
        RestClient.Builder b = RestClient.builder();
        MockRestServiceServer s = MockRestServiceServer.bindTo(b).build();
        GeminiService withFallback = new GeminiService("key", "main", "backup", b, new long[] {0, 0});
        s.expect(times(3), requestTo(org.hamcrest.Matchers.endsWith("/models/main:generateContent")))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        s.expect(requestTo(org.hamcrest.Matchers.endsWith("/models/backup:generateContent")))
                .andRespond(withSuccess(OK_BODY, MediaType.APPLICATION_JSON));

        assertEquals("{}", withFallback.complete("sys", "user"));
        s.verify();
    }

    @Test
    void otherServerError_isNotRetried() {
        server.expect(times(1), method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(CoachGenerationException.class, () -> service.complete("sys", "user"));
        server.verify();
    }
}
