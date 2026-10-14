package com.fahad.githubsearcher.repository;

import com.fahad.githubsearcher.github.GithubRepository;
import com.fahad.githubsearcher.dto.RepositorySort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcGithubRepositoryStore {
    private static final String UPSERT_SQL = """
            INSERT INTO github_repositories (id, name, description, owner_name, language, stars_count, forks_count, last_updated)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
              name = EXCLUDED.name,
              description = EXCLUDED.description,
              owner_name = EXCLUDED.owner_name,
              language = EXCLUDED.language,
              stars_count = EXCLUDED.stars_count,
              forks_count = EXCLUDED.forks_count,
              last_updated = EXCLUDED.last_updated
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcGithubRepositoryStore(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @Transactional
    public void upsertAll(List<GithubRepository> repositories) {
        if (repositories.isEmpty()) return;
        jdbcTemplate.batchUpdate(UPSERT_SQL, repositories, repositories.size(), (statement, repository) -> {
            statement.setLong(1, repository.id());
            statement.setString(2, repository.name());
            statement.setString(3, repository.description());
            statement.setString(4, repository.owner());
            statement.setString(5, repository.language());
            statement.setLong(6, repository.stars());
            statement.setLong(7, repository.forks());
            statement.setTimestamp(8, Timestamp.from(repository.lastUpdated()));
        });
    }

    @Transactional(readOnly = true)
    public List<GithubRepository> find(String language, Long minStars, RepositorySort sort) {
        var sql = new StringBuilder("SELECT id, name, description, owner_name, language, stars_count, forks_count, last_updated FROM github_repositories WHERE 1=1");
        var parameters = new ArrayList<Object>();
        if (language != null && !language.isBlank()) {
            sql.append(" AND LOWER(language) = LOWER(?)");
            parameters.add(language);
        }
        if (minStars != null) {
            sql.append(" AND stars_count >= ?");
            parameters.add(minStars);
        }
        sql.append(" ORDER BY ").append(sort.databaseColumn()).append(" DESC, id ASC");
        return jdbcTemplate.query(sql.toString(), (rs, row) -> new GithubRepository(
                rs.getLong("id"), rs.getString("name"), rs.getString("description"), rs.getString("owner_name"),
                rs.getString("language"), rs.getLong("stars_count"), rs.getLong("forks_count"),
                rs.getTimestamp("last_updated").toInstant()), parameters.toArray());
    }
}
