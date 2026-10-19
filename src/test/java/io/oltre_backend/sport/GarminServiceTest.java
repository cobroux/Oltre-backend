package io.oltre_backend.sport;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

// Un sidecar Garmin éteint ou en erreur doit donner une
// GarminServiceUnavailableException (502), jamais une exception brute (500).
class GarminServiceTest {

    private static int closedPort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    @Test
    void sidecarDown_isConnectedDisconnectAndActivities_throwUnavailable() throws IOException {
        GarminService service = new GarminService("http://localhost:" + closedPort(), "t");

        assertThrows(GarminServiceUnavailableException.class, () -> service.isConnected(1L));
        assertThrows(GarminServiceUnavailableException.class, () -> service.disconnect(1L));
        assertThrows(GarminServiceUnavailableException.class, () -> service.getActivities(1L, null, 10));
    }

    @Test
    void sidecarReturns500_throwsUnavailable() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", ex -> {
            ex.sendResponseHeaders(500, -1);
            ex.close();
        });
        server.start();
        try {
            GarminService service = new GarminService("http://localhost:" + server.getAddress().getPort(), "t");
            assertThrows(GarminServiceUnavailableException.class, () -> service.isConnected(1L));
        } finally {
            server.stop(0);
        }
    }
}
