package com.fahad.githubsearcher.service;

import com.fahad.githubsearcher.github.GithubClient;
import com.fahad.githubsearcher.github.GithubRepository;
import com.fahad.githubsearcher.dto.RepositorySort;
import com.fahad.githubsearcher.repository.JdbcGithubRepositoryStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GithubSearchService {
    private final GithubClient githubClient;
    private final JdbcGithubRepositoryStore repositoryStore;

    public GithubSearchService(GithubClient githubClient, JdbcGithubRepositoryStore repositoryStore) {
        this.githubClient = githubClient;
        this.repositoryStore = repositoryStore;
    }

    public List<GithubRepository> search(String query, String language, RepositorySort sort) {
        var results = githubClient.search(query, language, sort);
        repositoryStore.upsertAll(results);
        return results;
    }

    @Transactional(readOnly = true)
    public List<GithubRepository> find(String language, Long minStars, RepositorySort sort) {
        return repositoryStore.find(language, minStars, sort);
    }
}
