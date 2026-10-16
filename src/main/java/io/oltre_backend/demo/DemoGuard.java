package io.oltre_backend.demo;

import java.io.IOException;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** En démo : inscription et toute modification du compte (mot de passe, profil, suppression, Garmin) renvoient 403. */
@Component
@Profile("demo")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DemoGuard extends OncePerRequestFilter {

    private static final Set<String> BLOCKED = Set.of(
            "POST /auth/register",
            "PUT /api/users/me",
            "PUT /api/users/me/password",
            "DELETE /api/users/me",
            "POST /api/garmin/connect",
            "DELETE /api/garmin/connect");

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        // getServletPath : décodé et sans ";param", sinon "/api/users/me;x=1" contournerait la liste
        String path = req.getServletPath().replaceAll("/+$", "");
        if (BLOCKED.contains(req.getMethod() + " " + path)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"message\":\"Désactivé en mode démo\"}");
            return;
        }
        chain.doFilter(req, res);
    }
}
