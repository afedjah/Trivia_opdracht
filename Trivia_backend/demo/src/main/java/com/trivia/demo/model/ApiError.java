package com.trivia.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String error;
    private String message;

    public ApiError(HttpStatus status, String message){
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
    }
}
