package com.fahad.githubsearcher.web;

import com.fahad.githubsearcher.github.GithubRepository;
import com.fahad.githubsearcher.dto.RepositorySort;
import com.fahad.githubsearcher.service.GithubSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GithubRepositoryController.class)
class GithubRepositoryControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean GithubSearchService service;

    @Test
    void successfulEmptySearchReturnsOkWithEmptyRepositories() throws Exception {
        when(service.search("absent", null, null)).thenReturn(List.of());

        mvc.perform(post("/api/github/search").contentType("application/json").content("{\"query\":\"absent\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories").isArray())
                .andExpect(jsonPath("$.repositories.length()").value(0));
    }

    @Test
    void postSortIsPassedToGithubSearch() throws Exception {
        when(service.search("spring", "Java", RepositorySort.FORKS)).thenReturn(List.of());

        mvc.perform(post("/api/github/search").contentType("application/json")
                        .content("{\"query\":\"spring\",\"language\":\"Java\",\"sort\":\"forks\"}"))
                .andExpect(status().isOk());
        verify(service).search("spring", "Java", RepositorySort.FORKS);
    }

    @Test
    void getSortAndFiltersArePassedToStoredRepositorySearch() throws Exception {
        when(service.find("Java", 100L, RepositorySort.UPDATED)).thenReturn(List.of(
                new GithubRepository(1, "repo", null, "owner", "Java", 200, 3,
                        Instant.parse("2024-01-01T00:00:00Z"))));

        mvc.perform(get("/api/github/repositories").queryParam("language", "Java")
                        .queryParam("minStars", "100").queryParam("sort", "updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories[0].stars").value(200));
        verify(service).find("Java", 100L, RepositorySort.UPDATED);
    }

    @Test
    void rejectsUnsupportedSortValue() throws Exception {
        mvc.perform(get("/api/github/repositories").queryParam("sort", "name"))
                .andExpect(status().isBadRequest());
    }
}
