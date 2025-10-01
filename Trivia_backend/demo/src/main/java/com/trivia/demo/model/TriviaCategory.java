package com.trivia.demo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TriviaCategory {

    private Long id;
    private String name;

    public TriviaCategory(Long id, String name){
        this.id = id;
        this.name = name;
    }
}
