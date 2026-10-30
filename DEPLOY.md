# Déploiement en production

Suppose un VPS Linux avec Docker + Docker Compose installés, et `oltre-frontend`
cloné à côté de ce repo (même layout que pour le dev local, voir `docker-compose.yaml`).

## 1. Domaine

Achète un nom de domaine et pointe un enregistrement DNS `A` vers l'IP du VPS.
Attends la propagation (`dig app.example.com` doit renvoyer l'IP du VPS) avant
de démarrer Caddy à l'étape 4 — sinon la demande de certificat Let's Encrypt
échoue.

## 2. Configuration

```bash
cp .env.example .env
```

Remplis au minimum :
- `DOMAIN` — ton nom de domaine réel
- `MYSQL_ROOT_PASSWORD` / `MYSQL_PASSWORD` — génère avec `openssl rand -hex 24`
- `JWT_SECRET` — génère avec `openssl rand -hex 32`
- `VAPID_PUBLIC_KEY` / `VAPID_PRIVATE_KEY` / `VAPID_SUBJECT` — notifications push (optionnel ; vides = push désactivé).
  Génère la paire avec `npx web-push generate-vapid-keys` ; `VAPID_SUBJECT` = `mailto:ton@email`.
  Ne change plus la paire ensuite : les abonnements existants deviendraient invalides.
- `GARMIN_SERVICE_URL` / `GARMIN_SERVICE_TOKEN` — si tu utilises l'intégration Garmin
- `MAIL_HOST` / `MAIL_PORT` (587, STARTTLS) / `MAIL_USERNAME` / `MAIL_PASSWORD` / `MAIL_FROM` (ex. `Oltre <no-reply@ton-domaine>`)
  — email « mot de passe oublié », via le SMTP de n'importe quel fournisseur (Brevo, Resend, Mailjet…).
  `MAIL_HOST` vide = non configuré : rien n'est envoyé, un WARN est loggé. `APP_BASE_URL` = URL publique du front
  (le lien est `<APP_BASE_URL>/reset-password?token=…`). Chez le fournisseur, configure SPF et DKIM pour ton domaine
  d'envoi (un domaine `sslip.io` ne convient pas). Sur la VM partagée, ajoute ces variables à la main dans
  `/opt/oltre/docker-compose.vm.yaml`.

## 3. Firewall

Ouvre les ports 80 et 443 (HTTP/HTTPS) sur le VPS. Ferme tout le reste côté
applicatif — MySQL/backend/frontend ne sont plus exposés directement sur
l'hôte avec `docker-compose.prod.yaml`, seul Caddy l'est.

## 4. Lancement

```bash
docker compose -f docker-compose.prod.yaml up -d --build
```

Caddy obtient et renouvelle automatiquement le certificat Let's Encrypt pour
`$DOMAIN` au premier démarrage — ça peut prendre une minute.

## 5. Vérification

```bash
docker compose -f docker-compose.prod.yaml ps
docker compose -f docker-compose.prod.yaml logs -f caddy
```

Puis ouvre `https://ton-domaine` dans un navigateur.

## Mettre à jour après un nouveau commit

```bash
git pull
docker compose -f docker-compose.prod.yaml up -d --build
```

## 6. Sauvegardes

Les données MySQL vivent dans un volume Docker — sans sauvegarde, tout est
perdu si le VPS meurt. Mets en place une sauvegarde quotidienne via cron :

```bash
crontab -e
# Ajouter :
0 3 * * * cd /chemin/vers/Oltre-backend && ./scripts/backup-mysql.sh >> /var/log/oltre-backup.log 2>&1
```

Ça écrit des dumps gzippés dans `./backups/` (ignoré par git) et supprime
automatiquement ceux de plus de 14 jours (`RETENTION_DAYS` dans le script).

Pour restaurer :

```bash
./scripts/restore-mysql.sh backups/oltre-20260101-030000.sql.gz
```

⚠️ Pense à copier `./backups/` ailleurs régulièrement (un autre serveur, un
stockage objet...) — une sauvegarde qui vit sur la même machine que les
données originales ne protège pas contre la perte du VPS lui-même.
