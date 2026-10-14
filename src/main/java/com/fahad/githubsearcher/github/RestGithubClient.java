package com.fahad.githubsearcher.github;

import com.fahad.githubsearcher.dto.RepositorySort;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.List;

@Component
public class RestGithubClient implements GithubClient {
    private final RestClient restClient;

    public RestGithubClient(RestClient.Builder builder, @Value("${github.api.base-url:https://api.github.com}") String baseUrl,
                            @Value("${github.api.token:}") String token) {
        var configured = builder.baseUrl(baseUrl).defaultHeader("Accept", "application/vnd.github+json");
        if (!token.isBlank()) configured.defaultHeader("Authorization", "Bearer " + token);
        this.restClient = configured.build();
    }

    @Override
    public List<GithubRepository> search(String query, String language, RepositorySort sort) {
        var githubQuery = language == null || language.isBlank() ? query : query + " language:" + language;
        try {
            var response = restClient.get().uri(uri -> {
                var builder = uri.path("/search/repositories").queryParam("q", githubQuery);
                if (sort != null) builder.queryParam("sort", sort.value()).queryParam("order", "desc");
                return builder.build();
            }).retrieve().body(GithubSearchResponse.class);
            if (response == null) throw new GithubApiException("GitHub returned an invalid response", HttpStatus.BAD_GATEWAY, null);
            if (response.items() == null) throw new GithubApiException("GitHub returned an invalid response", HttpStatus.BAD_GATEWAY, null);
            return response.items().stream().map(this::toRepository).toList();
        } catch (RestClientResponseException exception) {
            var status = exception.getStatusCode().value() == 403 && "0".equals(exception.getResponseHeaders().getFirst("X-RateLimit-Remaining"))
                    ? HttpStatus.TOO_MANY_REQUESTS : HttpStatus.BAD_GATEWAY;
            throw new GithubApiException(status == HttpStatus.TOO_MANY_REQUESTS ? "GitHub rate limit exceeded" : "GitHub API request failed", status, exception);
        } catch (RestClientException exception) {
            throw new GithubApiException("GitHub API returned an invalid response", HttpStatus.BAD_GATEWAY, exception);
        }
    }

    private GithubRepository toRepository(GithubItem item) {
        if (item.id() <= 0 || item.name() == null || item.name().isBlank() || item.owner() == null
                || item.owner().login() == null || item.owner().login().isBlank() || item.lastUpdated() == null) {
            throw new GithubApiException("GitHub returned an invalid repository entry", HttpStatus.BAD_GATEWAY, null);
        }
        return new GithubRepository(item.id(), item.name(), item.description(), item.owner().login(), item.language(),
                item.stars(), item.forks(), item.lastUpdated());
    }

    private record GithubSearchResponse(List<GithubItem> items) { }
    private record GithubOwner(String login) { }
    private record GithubItem(long id, String name, String description, GithubOwner owner, String language,
                              @JsonProperty("stargazers_count") long stars,
                              @JsonProperty("forks_count") long forks,
                              @JsonProperty("updated_at") Instant lastUpdated) { }
}
