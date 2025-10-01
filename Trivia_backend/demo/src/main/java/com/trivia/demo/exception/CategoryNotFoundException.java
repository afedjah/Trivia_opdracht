package com.trivia.demo.exception;

public class CategoryNotFoundException extends RuntimeException{
    public CategoryNotFoundException(Long categoryId){
        super("Categorie met Id " + categoryId + " is niet gevonden.");
    }
}
