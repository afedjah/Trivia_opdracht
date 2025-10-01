package com.trivia.demo.exception;

public class QuestionNotFoundException extends RuntimeException{
    public QuestionNotFoundException(String questionText){
        super("Vraag niet gevonden: " + questionText);
    }
}
