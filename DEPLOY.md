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
- `GARMIN_SERVICE_URL` / `GARMIN_SERVICE_TOKEN` — si tu utilises l'intégration Garmin

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
