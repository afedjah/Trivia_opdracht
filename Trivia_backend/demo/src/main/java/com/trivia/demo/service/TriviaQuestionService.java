package com.trivia.demo.service;

import com.trivia.demo.exception.CategoryNotFoundException;
import com.trivia.demo.exception.InvalidRequestException;
import com.trivia.demo.exception.QuestionNotFoundException;
import com.trivia.demo.exception.SessionNotFoundException;
import com.trivia.demo.model.*;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TriviaQuestionService {

    private final WebClient webClient;
    private final Map<String, Map<String, Question>> questions = new ConcurrentHashMap<>();
    private final TriviaSessionService sessionService;

    private static final String TRIVIA_API_URL = "https://opentdb.com";
    private static final Integer MAX_API_LIMIT = 50;

    @Autowired
    public TriviaQuestionService(TriviaSessionService sessionService){
        this.webClient = WebClient.builder().baseUrl(TRIVIA_API_URL).build();
        this.sessionService = sessionService;
    }

    public TriviaQuestionService(TriviaSessionService sessionService,WebClient webClient){
        this.sessionService = sessionService;
        this.webClient = webClient;
    }

    public Mono<List<TriviaCategory>> getCategories(){
        return webClient.get()
                .uri("/api_category.php")
                .retrieve()
                .bodyToMono(TriviaCategoriesResponse.class)
                .map(TriviaCategoriesResponse::getTrivia_categories)
                .onErrorMap(ex -> new RuntimeException("Kon categorieen niet ophalen. Error: " + ex.getMessage()));
    }

    public Mono<List<QuestionDTO>> getQuestions(Integer amount, Long categoryId, String sessionId) {
        if(amount == null || amount < 1){
            throw new InvalidRequestException("Aantal vragen moet minimaal 1 zijn.");
        }
        return this.sessionService.getOrCreateSessionToken(sessionId)
                .flatMap(token ->
                this.getTotalAmountOfQuestionsPerCategory(categoryId)
                .flatMap(total -> {
                    if(total == null || total == 0){
                        throw new CategoryNotFoundException(categoryId);
                    }
                    int fixedAmount = Math.min(amount, MAX_API_LIMIT);
                    int finalAmount = Math.min(fixedAmount,total);
                    return this.fetchQuestionsWithAutoReset(sessionId,finalAmount,categoryId);
                    }));
    }

    private Mono<List<QuestionDTO>> fetchQuestionsWithAutoReset(String sessionId, Integer amount, Long categoryId){
        return this.fetchQuestionsFromAPI(sessionId,amount,categoryId)
                .flatMap(response -> {
                    if(response == null){
                        return Mono.error(new RuntimeException("Trivia API returned null"));
                    }
                    int code = response.getResponse_code();
                    if(code == 3 || code == 4){
                        return this.sessionService.resetToken(sessionId)
                                .flatMap(newToken -> this.fetchQuestionsFromAPI(newToken, amount,categoryId))
                                .map(r -> mapQuestions(sessionId,r.getResults()));
                    }
                    else{
                        return Mono.just(this.mapQuestions(sessionId,response.getResults()));
                    }
                })
                .onErrorMap(ex -> new RuntimeException("Error bij het fetchen van vragen. Error: " + ex.getMessage()));
    }

    private Mono<TriviaQuestionsResponse> fetchQuestionsFromAPI(String sessionId, Integer amount, Long categoryId){
        return this.sessionService.getOrCreateSessionToken(sessionId)
                .flatMap(token ->this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api.php")
                        .queryParam("amount",amount)
                        .queryParam("token", token)
                        .queryParamIfPresent("category",Optional.ofNullable(categoryId))
                        .build())
                .retrieve()
                .bodyToMono(TriviaQuestionsResponse.class))
                .switchIfEmpty(Mono.error(new RuntimeException("Trivia API retourneerde een empty response.")))
                .onErrorMap(ex -> new RuntimeException("Kon Trivia API niet benaderen. Error: " + ex.getMessage()));
    }

    public boolean checkAnswer(String sessionId,AnswerDTO answerDTO){
        Map<String,Question> questionsForUser = this.questions.get(sessionId);
        if(questionsForUser == null){
            throw new SessionNotFoundException(sessionId);
        }
        Question question = questionsForUser.get(answerDTO.getQuestion());
        if(question == null){
            throw new QuestionNotFoundException(answerDTO.getQuestion());
        }
        return question.getCorrect_answer().equalsIgnoreCase(answerDTO.getChosen_answer());
    }

    private List<QuestionDTO> mapQuestions(String sessionId,List<Question> questions){
        Map<String, Question> questionsForUser = new ConcurrentHashMap<>();
        List<QuestionDTO> questionDTOs = new ArrayList<>();
        for(Question q : questions){
            if(q.getQuestion() == null){
                throw new IllegalArgumentException("Question text mag niet null zijn");
            }
            decodeHtmlForQuestion(q);
            questionsForUser.put(q.getQuestion(),q);
            questionDTOs.add(new QuestionDTO(q));
        }
        this.questions.put(sessionId,questionsForUser);
        return questionDTOs;
    }

    private void decodeHtmlForQuestion(Question question){
        question.setQuestion(StringEscapeUtils.unescapeHtml4(question.getQuestion()));
        question.setCorrect_answer(StringEscapeUtils.unescapeHtml4(question.getCorrect_answer()));
        if(question.getIncorrect_answers() != null){
            List<String> incorrectAnswersDecoded = question.getIncorrect_answers().stream()
                    .map(StringEscapeUtils::unescapeHtml4)
                    .toList();
            question.setIncorrect_answers(incorrectAnswersDecoded);
        }
    }

    private Mono<Integer> getTotalAmountOfQuestionsPerCategory(Long categoryId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api_count.php")
                        .queryParam("category", categoryId)
                        .build()
                ).retrieve()
                .bodyToMono(CategoryStatistics.class)
                .map(categoryStatistics -> categoryStatistics.getCategory_question_count().getTotal_question_count())
                .onErrorMap(ex -> new RuntimeException("Kon category niet fetchen. Error: " + ex.getMessage()));
    }

    public Map<String,Map<String, Question>> getQuestionsMap(){
        return Collections.unmodifiableMap(this.questions);
    }

    public void addQuestionForTest(String sessionId,Question question){
        if(sessionId == null){
            throw new IllegalArgumentException("sessionId mag niet null zijn");
        }
        Map<String,Question> questionsForUser = this.questions.computeIfAbsent(sessionId,k ->new ConcurrentHashMap<>());
        questionsForUser.put(question.getQuestion(),question);
    }
}
