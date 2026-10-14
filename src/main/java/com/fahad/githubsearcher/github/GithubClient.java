package com.fahad.githubsearcher.github;

import com.fahad.githubsearcher.dto.RepositorySort;

import java.util.List;

public interface GithubClient {
    List<GithubRepository> search(String query, String language, RepositorySort sort);
}
