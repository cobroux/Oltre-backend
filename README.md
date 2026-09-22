# Oltre-backend

## Intégration Garmin

Les données sportives viennent de Garmin Connect via le service REST
[`cobroux/garmin-mcp`](https://github.com/cobroux/garmin-mcp) (branche
`add-rest-api`), à lancer séparément :

```bash
git clone https://github.com/cobroux/garmin-mcp
cd garmin-mcp
git checkout add-rest-api
docker build -t garmin-mcp .
docker run --rm -p 8000:8000 -v garmin-mcp-data:/data garmin-mcp
```

Le backend appelle ce service via `garmin.service-url`
(`GARMIN_SERVICE_URL`), qui pointe par défaut sur `http://localhost:8000`.
