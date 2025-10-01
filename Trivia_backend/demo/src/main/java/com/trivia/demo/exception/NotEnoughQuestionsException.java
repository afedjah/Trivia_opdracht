package com.trivia.demo.exception;

public class NotEnoughQuestionsException extends RuntimeException{
    public NotEnoughQuestionsException(Long categoryId, int requested, int available){
        super("Category " + categoryId + " heeft maar " + available +
                " vragen, maar " + requested + " werden gevraagd.");
    }
}
