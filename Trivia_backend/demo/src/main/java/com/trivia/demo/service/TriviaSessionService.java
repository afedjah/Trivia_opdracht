package com.trivia.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TriviaSessionService {
    private final WebClient webClient;
    private final Map<String, String> sessionMap = new ConcurrentHashMap<>();
    private static final String TRIVIA_API_URL = "https://opentdb.com";

    public TriviaSessionService(){
        this.webClient = WebClient.builder().baseUrl(TRIVIA_API_URL).build();
    }

    public Mono<String> getOrCreateSessionToken(String sessionId){
        String token = this.sessionMap.get(sessionId);

        if(token == null){
            return requestNewToken().map(newToken ->{
                this.sessionMap.put(sessionId, newToken);
                return newToken;
            });
        }
        return Mono.just(token);
    }

    private Mono<String> requestNewToken(){
        return this.webClient.get()
                .uri("/api_token.php?command=request")
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> (String) map.get("token"));
    }

    public Mono<String> resetToken(String token){
        return this.webClient.get()
                .uri("/api_token.php?command=reset&token=" + token)
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> (String) map.get("token"));
    }
}
