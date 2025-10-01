package com.trivia.demo.controller;

import com.trivia.demo.model.*;
import com.trivia.demo.service.TriviaQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class TriviaController {

    private final TriviaQuestionService triviaQuestionService;

    @Autowired
    public TriviaController(TriviaQuestionService triviaQuestionService){
        this.triviaQuestionService = triviaQuestionService;
    }

    @GetMapping("/categories")
    public Mono<List<TriviaCategory>> getCategories(){
        return triviaQuestionService.getCategories();
    }

    @GetMapping("/questions")
    public Mono<List<QuestionDTO>> getQuestions(@RequestParam(name = "amount", defaultValue = "10") Integer amount,
                                                @RequestParam(name = "categoryId", required = false) Long categoryId,
                                                @RequestHeader(value = "x-sessionId") String sessionId){
        return triviaQuestionService.getQuestions(amount,categoryId, sessionId);
    }

    @PostMapping("/checkanswer")
    public ResponseEntity<Boolean> checkAnswer(@RequestHeader("x-sessionId") String sessionId,@RequestBody AnswerDTO answerDTO){
        return ResponseEntity.ok(triviaQuestionService.checkAnswer(sessionId,answerDTO));
    }

}
