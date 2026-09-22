# Oltre-backend

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
