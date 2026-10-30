package io.oltre_backend.sport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.List;

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

    private static String run(String date, double meters, double seconds) {
        return "{\"activityId\":1,\"activityType\":{\"typeKey\":\"running\"},\"startTimeLocal\":\""
                + date + " 08:00:00\",\"distance\":" + meters + ",\"duration\":" + seconds + "}";
    }

    // Page 1 = 200 sorties de cette année, page 2 = un vieux 5 km plus rapide :
    // il doit apparaître dans "depuis toujours" mais pas dans "cette année".
    // Une sortie de 14 km ne doit pas compter comme un record 15 km.
    @Test
    void records_paginateSidecarAndUseStrictBrackets() throws IOException {
        String thisYear = Year.now() + "-03-01";
        String firstPage = "[" + String.join(",", java.util.Collections.nCopies(199, run(thisYear, 5010, 1800)))
                + "," + run(thisYear, 14060, 4200) + "]";
        String secondPage = "[" + run("2020-05-01", 5000, 1500) + "]";
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", ex -> {
            String query = ex.getRequestURI().getQuery();
            byte[] body = (query.contains("start=0") ? firstPage : query.contains("start=200") ? secondPage : "[]")
                    .getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.start();
        try {
            GarminService service = new GarminService("http://localhost:" + server.getAddress().getPort(), "t");
            GarminRecordsResponseDTO records = service.getRecords(1L);

            assertEquals(1500, fiveKm(records.getAllTime().getRunningRecords()).getDurationSeconds());
            assertEquals(1800, fiveKm(records.getThisYear().getRunningRecords()).getDurationSeconds());
            assertEquals(0, records.getAllTime().getRunningRecords().stream()
                    .filter(r -> r.getLabel().equals("15 km")).count());
        } finally {
            server.stop(0);
        }
    }

    private static RunningRecordDTO fiveKm(List<RunningRecordDTO> records) {
        return records.stream().filter(r -> r.getLabel().equals("5 km")).findFirst().orElseThrow();
    }
}
