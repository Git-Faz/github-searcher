package com.fahad.githubsearcher.repository;

import com.fahad.githubsearcher.github.GithubRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JdbcGithubRepositoryStoreTest {
    @Test
    void writesUseConflictUpdateOnGitHubId() {
        var jdbc = mock(JdbcTemplate.class);
        var store = new JdbcGithubRepositoryStore(jdbc);
        var repository = new GithubRepository(9, "repo", "description", "owner", "Java", 4, 2,
                Instant.parse("2025-01-01T00:00:00Z"));

        store.upsertAll(List.of(repository));

        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbc).batchUpdate(sql.capture(), anyCollection(), anyInt(),
                org.mockito.ArgumentMatchers.<org.springframework.jdbc.core.ParameterizedPreparedStatementSetter<GithubRepository>>any());
        assertTrue(sql.getValue().contains("ON CONFLICT (id) DO UPDATE"));
        assertTrue(sql.getValue().contains("stars_count = EXCLUDED.stars_count"));
    }
}
