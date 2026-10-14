package com.fahad.githubsearcher.github;

import java.time.Instant;

public record GithubRepository(long id, String name, String description, String owner, String language,
                               long stars, long forks, Instant lastUpdated) { }
