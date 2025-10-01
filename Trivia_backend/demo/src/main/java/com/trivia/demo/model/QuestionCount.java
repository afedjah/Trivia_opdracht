package com.trivia.demo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionCount {
    private int total_question_count;
    private int total_easy_question_count;
    private int total_medium_question_count;
    private int total_hard_question_count;

    public QuestionCount(int total_question_count, int total_easy_question_count, int total_medium_question_count,
                         int total_hard_question_count){
        this.total_question_count = total_question_count;
        this.total_easy_question_count = total_easy_question_count;
        this.total_medium_question_count = total_medium_question_count;
        this.total_hard_question_count = total_hard_question_count;
    }
}
