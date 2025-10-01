package com.trivia.demo.exception;

public class SessionNotFoundException extends RuntimeException{
    public SessionNotFoundException(String sessionId){
        super("SessionId: " + sessionId + " niet gevonden. Graag een valide sessionId gebruiken.");
    }
}
