package com.trivia.demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryStatistics {
    private Long categoryId;
    private QuestionCount category_question_count;
}
