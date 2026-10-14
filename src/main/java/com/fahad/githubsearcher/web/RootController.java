package com.fahad.githubsearcher.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "name", "GitHub Repository Searcher",
                "status", "running, but slow, due to free hosting",
                "documentation", "/swagger-ui/index.html"
        );
    }
}
