package com.trivia.demo.service;

import com.trivia.demo.exception.CategoryNotFoundException;
import com.trivia.demo.exception.InvalidRequestException;
import com.trivia.demo.exception.QuestionNotFoundException;
import com.trivia.demo.exception.SessionNotFoundException;
import com.trivia.demo.intefaces.IQuestionMap;
import com.trivia.demo.model.*;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class TriviaQuestionService {

    private final OpenTriviaApiClient apiClient;
    private final TriviaSessionService sessionService;
    private final IQuestionMap questionMap;

    private static final int MAX_API_LIMIT = 50;

    public TriviaQuestionService(OpenTriviaApiClient apiClient,TriviaSessionService sessionService,IQuestionMap questionMap){
        this.apiClient = apiClient;
        this.sessionService = sessionService;
        this.questionMap = questionMap;
    }

    public Mono<List<TriviaCategory>> getCategories(){
        return apiClient.getCategories()
                .map(TriviaCategoriesResponse::getTrivia_categories)
                .onErrorMap(ex -> new RuntimeException("Kon category niet ophalen. Error: " + ex.getMessage()));
    }

    public Mono<List<QuestionDTO>> getQuestions(Integer amount, Long categoryId, String sessionId) {
        if(amount == null || amount < 1){
            throw new InvalidRequestException("Aantal vragen moet minimaal 1 zijn.");
        }
        return this.sessionService.getOrCreateSessionToken(sessionId)
                .flatMap(token -> this.apiClient.getTotalAmountOfQuestionsForCategory(categoryId)
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
                        return Mono.error(new RuntimeException("Trivia API retourneerde null"));
                    }
                    int code = response.getResponse_code();
                    if(code == 3 || code == 4){
                        return this.sessionService.ensureValidToken(sessionId,code)
                                .flatMap(newToken -> this.apiClient.getQuestions(amount,categoryId,newToken))
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
                .flatMap(token -> this.apiClient.getQuestions(amount,categoryId, token))
                .onErrorMap(ex -> new RuntimeException("Kon Trivia API niet benaderen. Error: " + ex.getMessage()));
    }


    public boolean checkAnswer(String sessionId,AnswerDTO answerDTO){
        Map<String,Question> questionsForUser = this.questionMap.getQuestionsForSession(sessionId);
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
        List<QuestionDTO> questionDTOs = new ArrayList<>();
        for(Question q : questions){
            if(q.getQuestion() == null){
                throw new IllegalArgumentException("Question text mag niet null zijn");
            }
            this.decodeHtmlForQuestion(q);
            this.questionMap.addQuestion(sessionId,q);
            questionDTOs.add(new QuestionDTO(q));
        }
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
}
