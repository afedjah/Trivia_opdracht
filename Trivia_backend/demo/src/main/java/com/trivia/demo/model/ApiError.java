package com.trivia.demo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApiError {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;

    public ApiError(HttpStatus status, String message){
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
    }
}
