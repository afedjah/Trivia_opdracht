package com.trivia.demo.service;

import com.trivia.demo.intefaces.IQuestionMap;
import com.trivia.demo.model.Question;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryQuestionMap implements IQuestionMap {
    private final Map<String, Map<String, Question>> questions = new ConcurrentHashMap<>();

    @Override
    public void addQuestion(String sessionId, Question question) {
        if(sessionId == null || question == null || question.getQuestion() == null){
            throw new IllegalArgumentException("sessionId en question mogen niet null zijn.");
        }
        Map<String, Question> questionsForUser = this.questions.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>());
        questionsForUser.put(question.getQuestion(), question);
    }

    @Override
    public Map<String, Question> getQuestionsForSession(String sessionId) {
        return this.questions.getOrDefault(sessionId, Collections.emptyMap());
    }
}
