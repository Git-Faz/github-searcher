CREATE TABLE IF NOT EXISTS github_repositories (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_name VARCHAR(255) NOT NULL,
    language VARCHAR(100),
    stars_count BIGINT NOT NULL,
    forks_count BIGINT NOT NULL,
    last_updated TIMESTAMPTZ NOT NULL
);
