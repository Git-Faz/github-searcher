package com.fahad.githubsearcher.dto;

import java.util.List;

public record RepositoriesResponse(String message, List<RepositoryResponse> repositories) { }
