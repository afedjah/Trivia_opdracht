package com.trivia.demo.config;

import com.trivia.demo.intefaces.IQuestionMap;
import com.trivia.demo.service.InMemoryQuestionMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public IQuestionMap questionMap(){
        return new InMemoryQuestionMap();
    }
}
