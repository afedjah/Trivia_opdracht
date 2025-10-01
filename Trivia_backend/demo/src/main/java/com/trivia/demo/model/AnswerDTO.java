package com.trivia.demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerDTO {
    private String question;
    private String chosen_answer;
}
