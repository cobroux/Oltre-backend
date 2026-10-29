package io.oltre_backend.calendar;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import io.oltre_backend.appointment.appointment;

// Génère un VCALENDAR (RFC 5545) à partir des rendez-vous d'un utilisateur.
final class IcsWriter {

    // shortcut: fuseau fixe Europe/Paris (pas de fuseau par utilisateur), à rendre configurable si l'appli sort de France.
    static final String TZID = "Europe/Paris";

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private IcsWriter() {}

    static String write(List<appointment> appointments, Instant now) {
        StringBuilder sb = new StringBuilder();
        line(sb, "BEGIN:VCALENDAR");
        line(sb, "VERSION:2.0");
        line(sb, "PRODID:-//Oltre//Calendrier//FR");
        line(sb, "CALSCALE:GREGORIAN");
        line(sb, "METHOD:PUBLISH");
        line(sb, "X-WR-CALNAME:Oltre");
        line(sb, "X-WR-TIMEZONE:" + TZID);
        // Indication de fréquence de rafraîchissement (Apple la respecte, Google l'ignore).
        line(sb, "REFRESH-INTERVAL;VALUE=DURATION:PT1H");
        line(sb, "X-PUBLISHED-TTL:PT1H");
        for (String l : PARIS_VTIMEZONE) line(sb, l);

        String stamp = DATE_TIME.format(LocalDateTime.ofInstant(now, ZoneOffset.UTC)) + "Z";
        for (appointment a : appointments) {
            line(sb, "BEGIN:VEVENT");
            line(sb, "UID:oltre-appt-" + a.getId() + "@oltre");
            line(sb, "DTSTAMP:" + stamp);
            if (a.getApptTime() == null) {
                line(sb, "DTSTART;VALUE=DATE:" + DATE.format(a.getApptDate()));
                line(sb, "DTEND;VALUE=DATE:" + DATE.format(a.getApptDate().plusDays(1)));
            } else {
                LocalDateTime start = a.getApptDate().atTime(a.getApptTime());
                // Fin absente ou avant le début : durée d'1 h par défaut.
                LocalDateTime end = a.getApptEndTime() != null && a.getApptEndTime().isAfter(a.getApptTime())
                        ? a.getApptDate().atTime(a.getApptEndTime())
                        : start.plusHours(1);
                line(sb, "DTSTART;TZID=" + TZID + ":" + DATE_TIME.format(start));
                line(sb, "DTEND;TZID=" + TZID + ":" + DATE_TIME.format(end));
            }
            line(sb, "SUMMARY:" + escape(a.getTitle()));
            if (a.getDescription() != null && !a.getDescription().isBlank()) line(sb, "DESCRIPTION:" + escape(a.getDescription()));
            if (a.getLocation() != null && !a.getLocation().isBlank()) line(sb, "LOCATION:" + escape(a.getLocation()));
            line(sb, "END:VEVENT");
        }
        line(sb, "END:VCALENDAR");
        return sb.toString();
    }

    static String escape(String text) {
        return text.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,")
                .replace("\r\n", "\\n").replace("\n", "\\n").replace("\r", "\\n");
    }

    // Lignes de 75 octets max, suite précédée d'un espace, sans couper un caractère UTF-8.
    private static void line(StringBuilder sb, String content) {
        int bytes = 0;
        for (int i = 0; i < content.length(); ) {
            int cp = content.codePointAt(i);
            int len = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8).length;
            if (bytes + len > 75) {
                sb.append("\r\n ");
                bytes = 1;
            }
            sb.appendCodePoint(cp);
            bytes += len;
            i += Character.charCount(cp);
        }
        sb.append("\r\n");
    }

    private static final List<String> PARIS_VTIMEZONE = List.of(
            "BEGIN:VTIMEZONE",
            "TZID:" + TZID,
            "BEGIN:DAYLIGHT",
            "TZOFFSETFROM:+0100",
            "TZOFFSETTO:+0200",
            "TZNAME:CEST",
            "DTSTART:19700329T020000",
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU",
            "END:DAYLIGHT",
            "BEGIN:STANDARD",
            "TZOFFSETFROM:+0200",
            "TZOFFSETTO:+0100",
            "TZNAME:CET",
            "DTSTART:19701025T030000",
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU",
            "END:STANDARD",
            "END:VTIMEZONE");
}
