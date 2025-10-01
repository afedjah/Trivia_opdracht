package com.trivia.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TriviaCategoriesResponse {
    private List<TriviaCategory> trivia_categories;
}
