package com.trivia.demo.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TriviaSessionService {
    private final OpenTriviaApiClient apiClient;
    private final Map<String, String> sessionMap = new ConcurrentHashMap<>();

    public TriviaSessionService(OpenTriviaApiClient apiClient){
        this.apiClient = apiClient;
    }

    public Mono<String> getOrCreateSessionToken(String sessionId){
        return Mono.defer(() -> {
             String token = this.sessionMap.get(sessionId);
             if(token == null){
                 return this.apiClient.requestNewToken().doOnNext(newToken -> this.sessionMap.put(sessionId, newToken));
             }
             return Mono.just(token);
         });
    }

    public Mono<String> resetTokenForSession(String sessionId){
        String oldToken = this.sessionMap.get(sessionId);
        Mono<String> resetMono;
        if(oldToken != null){
            resetMono = this.apiClient.resetToken(oldToken);
        } else{
            resetMono = this.apiClient.requestNewToken();
        }
        return resetMono.doOnNext(newToken -> this.sessionMap.put(sessionId, newToken));
    }


    public Mono<String> ensureValidToken(String sessionId, int lastResponseCode){
        if(lastResponseCode == 3 || lastResponseCode == 4){
            return this.resetTokenForSession(sessionId);
        }
        return this.getOrCreateSessionToken(sessionId);
    }

}
