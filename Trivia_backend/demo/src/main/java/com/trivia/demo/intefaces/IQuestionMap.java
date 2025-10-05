package com.trivia.demo.intefaces;

import com.trivia.demo.model.Question;

import java.util.Map;

public interface IQuestionMap {
    void addQuestion(String sessionId, Question question);
    Map<String, Question> getQuestionsForSession(String sessionId);
}
