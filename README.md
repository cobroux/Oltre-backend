# Oltre-backend

## Lancer avec Docker (backend + frontend + MySQL)

Clone les deux repos comme dossiers voisins :

```
mon-dossier/
  Oltre-backend/     (ce repo)
  oltre-frontend/
```

Puis, dans `Oltre-backend/` :

```bash
cp .env.example .env
# édite .env : GARMIN_SERVICE_URL et GARMIN_SERVICE_TOKEN

docker compose up --build
```

- Frontend : http://localhost:4200
- Backend : http://localhost:8080
- MySQL : localhost:3306

**Sur un réseau d'entreprise qui intercepte le HTTPS** (proxy Palo Alto,
Zscaler, Netskope...) : les appels du backend vers le sidecar Garmin
échoueront avec une erreur `PKIX path building failed` tant que le
certificat racine de l'entreprise n'est pas connu de la JVM. Dépose-le
dans `certs/` (voir `certs/README.md`) avant de builder.

`docker-compose.yaml` construit et lance les 3 services ensemble ; toutes
les variables (dont `GARMIN_SERVICE_URL`/`GARMIN_SERVICE_TOKEN`) sont
injectées via `.env` (jamais committé — voir `.env.example`).

## Intégration Garmin

Les données sportives viennent de Garmin Connect via le service REST
[`cobroux/garmin-mcp`](https://github.com/cobroux/garmin-mcp), à lancer
séparément (déployé sur Railway en prod) :

```bash
git clone https://github.com/cobroux/garmin-mcp
cd garmin-mcp
docker build -t garmin-mcp .
docker run --rm -p 8000:8000 -e GARMIN_API_TOKEN=un-secret-fort -v garmin-mcp-data:/data garmin-mcp
```

Le backend appelle ce service via deux propriétés :
- `garmin.service-url` (`GARMIN_SERVICE_URL`) — défaut `http://localhost:8000`
- `garmin.service-token` (`GARMIN_SERVICE_TOKEN`) — doit correspondre au
  `GARMIN_API_TOKEN` configuré sur le service garmin-mcp, sinon tous les
  appels échouent en 401.
