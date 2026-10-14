package com.fahad.githubsearcher.web;

import com.fahad.githubsearcher.dto.RepositoriesResponse;
import com.fahad.githubsearcher.dto.RepositoryResponse;
import com.fahad.githubsearcher.dto.RepositorySort;
import com.fahad.githubsearcher.dto.SearchRequest;
import com.fahad.githubsearcher.dto.SearchResponse;
import com.fahad.githubsearcher.service.GithubSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github")
@Validated
@Tag(name = "GitHub repositories")
public class GithubRepositoryController {
    private final GithubSearchService service;

    public GithubRepositoryController(GithubSearchService service) { this.service = service; }

    @PostMapping("/search")
    @Operation(summary = "Search repositories")
    @ApiResponse(responseCode = "200", description = "Search completed")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "429", description = "GitHub rate limit reached")
    @ApiResponse(responseCode = "502", description = "GitHub request failed")
    public SearchResponse search(@Valid @RequestBody SearchRequest request) {
        var sort = request.sort() == null ? null : RepositorySort.valueOf(request.sort().toUpperCase());
        var repositories = service.search(request.query().trim(), request.language(), sort).stream()
                .map(RepositoryResponse::from).toList();
        return new SearchResponse("Repositories fetched and saved successfully", repositories);
    }

    @GetMapping("/repositories")
    @Operation(summary = "Get saved repositories")
    @ApiResponse(responseCode = "200", description = "Repositories returned")
    @ApiResponse(responseCode = "400", description = "Invalid filter")
    public RepositoriesResponse repositories(
            @Parameter(description = "Programming language", example = "Java")
            @RequestParam(required = false) String language,
            @Parameter(description = "Minimum number of stars", example = "100")
            @RequestParam(required = false) @Min(value = 0, message = "minStars must be zero or greater") Long minStars,
            @Parameter(description = "Sort by stars, forks, or update time", schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "stars", allowableValues = {"stars", "forks", "updated"}))
            @RequestParam(defaultValue = "stars") @Pattern(regexp = "stars|forks|updated", message = "sort must be one of: stars, forks, updated") String sort) {
        var repositories = service.find(language, minStars, RepositorySort.valueOf(sort.toUpperCase())).stream()
                .map(RepositoryResponse::from).toList();

        var message = repositories.isEmpty()
                ? "No repositories found matchcing the specified filters"
                : "Repositories retrieved succesfully";
        return new RepositoriesResponse(message, repositories);
    }
}
