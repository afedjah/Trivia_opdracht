package com.trivia.demo.service;

import com.trivia.demo.model.CategoryStatistics;
import com.trivia.demo.model.TriviaCategoriesResponse;
import com.trivia.demo.model.TriviaQuestionsResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Component
public class OpenTriviaApiClient {
    private final WebClient webClient;
    private static final String TRIVIA_API_URL = "https://opentdb.com";

    public OpenTriviaApiClient(){
        this.webClient = WebClient.builder().baseUrl(TRIVIA_API_URL).build();
    }

    public Mono<TriviaCategoriesResponse> getCategories(){
        return this.webClient.get()
                .uri("/api_category.php")
                .retrieve()
                .bodyToMono(TriviaCategoriesResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .switchIfEmpty(Mono.error(new RuntimeException("Trivia API retourneerde een lege response bij het ophalen van de categorieen.")));
    }

    public Mono<TriviaQuestionsResponse> getQuestions(int amount, Long categoryId, String token){
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api.php")
                        .queryParam("amount", amount)
                        .queryParam("token", token)
                        .queryParamIfPresent("category", Optional.ofNullable(categoryId))
                        .build())
                .retrieve()
                .bodyToMono(TriviaQuestionsResponse.class)
                .switchIfEmpty(Mono.error(new RuntimeException("Trivia API retourneerde een lege response bij het ophalen van vragen.")))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)));
    }

    public Mono<Integer> getTotalAmountOfQuestionsForCategory(Long categoryId){
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api_count.php")
                        .queryParam("category", categoryId)
                        .build())
                .retrieve()
                .bodyToMono(CategoryStatistics.class)
                .map(stats -> stats.getCategory_question_count().getTotal_question_count())
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .switchIfEmpty(Mono.error((new RuntimeException("Trivia API retourneerde een lege response voor category statistieken."))));
    }

    public Mono<String> requestNewToken(){
        return this.webClient.get()
                .uri("/api_token.php?command=request")
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> (String) map.get("token"))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .switchIfEmpty(Mono.error((new RuntimeException(
                        "Trivia API retourneede een lege response voor het opvragen van een nieuwe token."))));
    }

    public Mono<String> resetToken(String token){
        return this.webClient.get()
                .uri("/api_token.php?command=reset&token={token}", token)
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> (String) map.get("token"))
                .retryWhen(Retry.backoff(3,Duration.ofSeconds(1)))
                .switchIfEmpty(Mono.error(new RuntimeException("Trivia API retourneerde een lege response voor token reset.")));
    }

}
