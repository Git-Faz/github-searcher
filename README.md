# GitHub Repository Searcher

Spring Boot REST API that searches GitHub repositories, saves results in PostgreSQL, and exposes filters over the saved data.

## Requirements

- Java 25
- Docker Compose (for local PostgreSQL)

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

The endpoints can be called from Postman or with curl.

Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`. The OpenAPI JSON document is at `http://localhost:8080/v3/api-docs`.

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
