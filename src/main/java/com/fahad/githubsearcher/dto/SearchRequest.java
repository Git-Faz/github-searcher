package com.fahad.githubsearcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Criteria for searching GitHub repositories")
public record SearchRequest(
        @Schema(description = "Search terms", example = "spring boot", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "query must not be blank") String query,
        @Schema(description = "Programming language filter", example = "Java") String language,
        @Schema(description = "Sort GitHub's search results", allowableValues = {"stars", "forks", "updated"})
        @Pattern(regexp = "stars|forks|updated", message = "sort must be one of: stars, forks, updated") String sort) { }
