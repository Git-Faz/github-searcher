package com.fahad.githubsearcher.github;

import org.springframework.http.HttpStatus;

public class GithubApiException extends RuntimeException {
    private final HttpStatus status;

    public GithubApiException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus status() { return status; }
}
