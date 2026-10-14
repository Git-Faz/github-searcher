package com.fahad.githubsearcher.service;

import com.fahad.githubsearcher.github.GithubClient;
import com.fahad.githubsearcher.github.GithubRepository;
import com.fahad.githubsearcher.dto.RepositorySort;
import com.fahad.githubsearcher.repository.JdbcGithubRepositoryStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubSearchServiceTest {
    @Mock GithubClient githubClient;
    @Mock JdbcGithubRepositoryStore repositoryStore;
    @InjectMocks GithubSearchService service;

    @Test
    void searchSendsSortToGithubAndPersistsReturnedRepositories() {
        var repository = new GithubRepository(7L, "sample", "description", "owner", "Java", 12, 3,
                Instant.parse("2025-01-01T00:00:00Z"));
        when(githubClient.search("spring", "Java", RepositorySort.FORKS)).thenReturn(List.of(repository));

        var results = service.search("spring", "Java", RepositorySort.FORKS);

        assertEquals(List.of(repository), results);
        verify(githubClient).search("spring", "Java", RepositorySort.FORKS);
        verify(repositoryStore).upsertAll(List.of(repository));
    }

    @Test
    void emptySuccessfulSearchReturnsEmptyListAndDoesNotWrite() {
        when(githubClient.search("no-match", null, RepositorySort.STARS)).thenReturn(List.of());

        assertEquals(List.of(), service.search("no-match", null, RepositorySort.STARS));
        verify(repositoryStore).upsertAll(List.of());
    }

    @Test
    void retrievalSortIsPassedToDatabaseStoreIndependently() {
        when(repositoryStore.find(null, 5L, RepositorySort.UPDATED)).thenReturn(List.of());

        assertEquals(List.of(), service.find(null, 5L, RepositorySort.UPDATED));
        verify(repositoryStore).find(null, 5L, RepositorySort.UPDATED);
    }
}
