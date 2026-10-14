# GitHub Repository Searcher

Spring Boot REST API that searches GitHub repositories, saves results in PostgreSQL, and exposes filters over the saved data.

## Requirements

- Java 25
- Docker Compose (for local PostgreSQL)
- Docker (optional, to build the deployment image locally)

## Run locally

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Configure the datasource values expected by `application.properties`, then start the application:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/github_searcher
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
./mvnw spring-boot:run
```

Set `GITHUB_API_TOKEN` if you want to authenticate outbound GitHub API requests and raise the unauthenticated rate limit. The app uses `https://api.github.com` by default.

## Deploy to Render with Neon PostgreSQL

The repository includes a multi-stage `Dockerfile` and a `render.yaml` Blueprint. The image build runs `mvn package`, including the test suite, and the runtime container runs as a non-root user. `.dockerignore` excludes `.env` and local build files from the image.

To deploy:

1. Push this repository to GitHub or another Git provider connected to Render.
2. In Render, create a new Blueprint from the repository and use the checked-in `render.yaml`.
3. Set the requested `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables from Neon, then deploy.

Use the Neon connection details to form the JDBC URL. For example, if Neon provides `postgresql://<user>:<password>@<neon-host>/<database>?sslmode=require`, set `DB_URL` to `jdbc:postgresql://<neon-host>/<database>?sslmode=require`, `DB_USERNAME` to the Neon role, and `DB_PASSWORD` to its password. For a pooled Neon connection, use its host and the exact user value Neon provides. The application runs `schema.sql` at startup, so the database role must be allowed to create the table.

Render supplies `PORT` automatically; Spring Boot uses it when present and falls back to port 8080 locally. The Blueprint health check uses `/swagger-ui/index.html`. You can optionally set `GITHUB_API_TOKEN` in the Render service environment to authenticate GitHub API calls.

After deployment, open `https://<render-service>.onrender.com/swagger-ui/index.html` to access Swagger, or append `/v3/api-docs` for the OpenAPI document. The free Render web service may spin down when idle and take time to respond to its next request.

Keep database credentials in Render's environment settings; do not commit them to the repository.

To build the image locally before pushing:

```bash
docker build -t github-searcher .
```

## Swagger and API examples

The endpoints can be called from Postman or curl. Locally, Swagger UI is at `http://localhost:8080/swagger-ui/index.html` and OpenAPI JSON is at `http://localhost:8080/v3/api-docs`.

## API

### Search GitHub

`POST /api/github/search`

```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```

```bash
curl -X POST http://localhost:8080/api/github/search \
  -H 'Content-Type: application/json' \
  -d '{"query":"spring boot","language":"Java","sort":"stars"}'
```

`query` is required. `language` and `sort` are optional. `sort` accepts exactly `stars`, `forks`, or `updated`; it determines GitHub's order for this search. The endpoint stores the first GitHub result page and upserts each repository by GitHub ID. A successful search with no matches returns HTTP 200 with `"repositories": []`.

### Retrieve saved repositories

`GET /api/github/repositories?language=Java&minStars=100&sort=stars`

```bash
curl 'http://localhost:8080/api/github/repositories?language=Java&minStars=100&sort=stars'
```

All parameters are optional. `minStars` must be zero or greater. `sort` accepts exactly `stars`, `forks`, or `updated`, and orders records already stored in PostgreSQL. The default is `stars`; sorting is descending, with GitHub ID as a stable tie-breaker.

Both endpoints return repository objects with `id`, `name`, `description`, `owner`, `language`, `stars`, `forks`, and `lastUpdated`.

## Error behavior

- Invalid request fields and unsupported sort values return HTTP 400 with a JSON `message` and, for body validation, field `details`.
- GitHub rate limiting returns HTTP 429 when GitHub identifies the rate limit as exhausted.
- Other GitHub errors or malformed responses return HTTP 502.
- An empty successful search is not an error.
