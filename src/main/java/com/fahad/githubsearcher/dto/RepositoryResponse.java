package com.fahad.githubsearcher.dto;

import com.fahad.githubsearcher.github.GithubRepository;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Repository details returned by the search and retrieval APIs")
public record RepositoryResponse(long id, String name, String description, String owner, String language,
                                 long stars, long forks,
                                 @Schema(description = "Last update timestamp in UTC") Instant lastUpdated) {
    public static RepositoryResponse from(GithubRepository repository) {
        return new RepositoryResponse(repository.id(), repository.name(), repository.description(), repository.owner(),
                repository.language(), repository.stars(), repository.forks(), repository.lastUpdated());
    }
}
