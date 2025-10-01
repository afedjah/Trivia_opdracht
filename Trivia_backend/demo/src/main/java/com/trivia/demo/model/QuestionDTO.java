package com.trivia.demo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class QuestionDTO {
    private String type;
    private String difficulty;
    private String category;
    private String question;
    private List<String> answers;

    public QuestionDTO(Question question){
        this.type = question.getType();
        this.difficulty = question.getDifficulty();
        this.category = question.getCategory();
        this.question = question.getQuestion();
        this.answers = new ArrayList<>(question.getIncorrect_answers());
        this.answers.add(question.getCorrect_answer());
        Collections.shuffle(answers);
    }
}


