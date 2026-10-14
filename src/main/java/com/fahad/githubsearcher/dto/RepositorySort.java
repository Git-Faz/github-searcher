package com.fahad.githubsearcher.dto;

public enum RepositorySort {
    STARS("stars", "stars_count"),
    FORKS("forks", "forks_count"),
    UPDATED("updated", "last_updated");

    private final String value;
    private final String databaseColumn;

    RepositorySort(String value, String databaseColumn) {
        this.value = value;
        this.databaseColumn = databaseColumn;
    }

    public String value() { return value; }
    public String databaseColumn() { return databaseColumn; }
}
