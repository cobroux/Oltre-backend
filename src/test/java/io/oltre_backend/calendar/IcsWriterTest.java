package io.oltre_backend.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.oltre_backend.appointment.appointment;

class IcsWriterTest {

    private static appointment appt(long id, String title, LocalTime start, LocalTime end) {
        appointment a = new appointment();
        a.setId(id);
        a.setTitle(title);
        a.setApptDate(LocalDate.of(2026, 11, 3));
        a.setApptTime(start);
        a.setApptEndTime(end);
        return a;
    }

    private static final Instant NOW = Instant.parse("2026-10-29T12:00:00Z");

    @Test
    void timedEvent_usesParisTimezoneAndEndTime() {
        String ics = IcsWriter.write(List.of(appt(7, "Dentiste", LocalTime.of(9, 30), LocalTime.of(10, 15))), NOW);

        assertTrue(ics.startsWith("BEGIN:VCALENDAR\r\n"));
        assertTrue(ics.endsWith("END:VCALENDAR\r\n"));
        assertTrue(ics.contains("UID:oltre-appt-7@oltre\r\n"));
        assertTrue(ics.contains("DTSTAMP:20261029T120000Z\r\n"));
        assertTrue(ics.contains("DTSTART;TZID=Europe/Paris:20261103T093000\r\n"));
        assertTrue(ics.contains("DTEND;TZID=Europe/Paris:20261103T101500\r\n"));
        assertTrue(ics.contains("BEGIN:VTIMEZONE\r\n"));
    }

    @Test
    void noTime_isAllDay_andMissingEnd_defaultsToOneHour() {
        String ics = IcsWriter.write(List.of(
                appt(1, "Journée off", null, null),
                appt(2, "Footing", LocalTime.of(18, 0), null)), NOW);

        assertTrue(ics.contains("DTSTART;VALUE=DATE:20261103\r\nDTEND;VALUE=DATE:20261104\r\n"));
        assertTrue(ics.contains("DTEND;TZID=Europe/Paris:20261103T190000\r\n"));
    }

    @Test
    void escapesSpecialCharacters() {
        assertEquals("a\\, b\\; c\\\\ d\\ne", IcsWriter.escape("a, b; c\\ d\ne"));
    }

    @Test
    void foldsLongLines_atMost75Octets_withoutSplittingUtf8() {
        String ics = IcsWriter.write(List.of(appt(1, "é".repeat(100), LocalTime.of(9, 0), null)), NOW);

        for (String l : ics.split("\r\n")) {
            assertTrue(l.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 75, l);
        }
        // Une fois dépliées, les lignes redonnent le titre intact.
        assertTrue(ics.replace("\r\n ", "").contains("SUMMARY:" + "é".repeat(100) + "\r\n"));
    }
}
