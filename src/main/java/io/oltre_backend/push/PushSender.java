package io.oltre_backend.push;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.Subscription;

// Enveloppe fine autour de la lib web-push (chiffrement RFC 8291 + JWT VAPID) : mockée dans les tests.
@Component
public class PushSender {

    private static final Logger log = LoggerFactory.getLogger(PushSender.class);

    private final String publicKey;
    private final nl.martijndwars.webpush.PushService client;

    public PushSender(@Value("${push.vapid.public-key:}") String publicKey,
                      @Value("${push.vapid.private-key:}") String privateKey,
                      @Value("${push.vapid.subject:}") String subject) {
        nl.martijndwars.webpush.PushService c = null;
        if (!publicKey.isBlank() && !privateKey.isBlank()) {
            try {
                Security.addProvider(new BouncyCastleProvider());
                c = new nl.martijndwars.webpush.PushService(publicKey, privateKey, subject);
            } catch (Exception e) { // clés mal formées : push désactivé plutôt que démarrage refusé
                log.warn("Clés VAPID invalides, notifications push désactivées", e);
            }
        }
        this.client = c;
        this.publicKey = c == null ? null : publicKey;
    }

    public boolean isConfigured() { return client != null; }

    public String publicKey() { return publicKey; }

    /** Code HTTP du service push (201 = ok, 404/410 = abonnement mort), ou -1 si l'envoi a échoué. */
    public int send(PushSubscription sub, String payload) {
        try {
            var response = client.send(new Notification(
                    new Subscription(sub.getEndpoint(), new Subscription.Keys(sub.getP256dh(), sub.getAuth())), payload));
            return response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            log.warn("Échec d'envoi push vers l'abonnement {}", sub.getId(), e);
            return -1;
        }
    }
}
