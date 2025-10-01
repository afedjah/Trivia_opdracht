package com.trivia.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trivia.demo.exception.CategoryNotFoundException;
import com.trivia.demo.exception.InvalidRequestException;
import com.trivia.demo.exception.QuestionNotFoundException;
import com.trivia.demo.model.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TriviaQuestionServiceTest {

    private static MockWebServer mockWebServer;
    private TriviaQuestionService triviaQuestionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUp() throws IOException{
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException{
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize(){
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

        TriviaSessionService sessionService = new TriviaSessionService(){
            @Override
            public Mono<String> getOrCreateSessionToken(String sessionId){
                return Mono.just(sessionId);
            }
        };
        this.triviaQuestionService = new TriviaQuestionService(sessionService, webClient);
    }

    @Test
    void testGetCategories_returnsList() throws Exception{
        //Arrange
        TriviaCategory category1 = new TriviaCategory(1L,"Wiskunde");
        TriviaCategory category2 = new TriviaCategory(2L,"Geschiedenis");
        TriviaCategory category3 = new TriviaCategory(3L,"Fietsen");
        TriviaCategory category4 = new TriviaCategory(4L,"Sport");
        TriviaCategoriesResponse response = new TriviaCategoriesResponse();
        response.setTrivia_categories(Arrays.asList(category1,category2,category3,category4));

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(response))
                .addHeader("Content-Type", "application/json"));

        //Act
        Mono<List<TriviaCategory>> resultMono = this.triviaQuestionService.getCategories();

        //Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(list -> list.size() == 4
                        && list.getFirst().getName().equals("Wiskunde")
                        && list.get(1).getName().equals("Geschiedenis")
                        && list.get(2).getName().equals("Fietsen")
                        && list.get(3).getName().equals("Sport"))
                .verifyComplete();
    }

    @Test
    void testGetQuestions_validAmount_returnsQuestionDTOs() throws Exception{
        //Arrange
        Question question1 = new Question();
        question1.setQuestion("Welke planeet zit het dichtst bij de aarde?");
        question1.setCorrect_answer("Mars");
        question1.setIncorrect_answers(Arrays.asList("Jupiter","Mercurius","Saturnus"));
        question1.setCategory("Wetenschap");
        Question question2 = new Question();
        question2.setQuestion("Welke planeet ligt het verst van de aarde vandaan?");
        question2.setCorrect_answer("Mercurius");
        question2.setIncorrect_answers(Arrays.asList("Jupiter","Mars","Saturnus"));
        question2.setCategory("Wetenschap");
        String sessionId = "ABC123584!";

        TriviaQuestionsResponse triviaQuestionsResponse = new TriviaQuestionsResponse();
        triviaQuestionsResponse.setResults(Arrays.asList(question1,question2));

        CategoryStatistics stats = new CategoryStatistics();
        stats.setCategory_question_count(new QuestionCount(2,1,1,0));

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(stats))
                .addHeader("Content-Type", "application/json"));

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(triviaQuestionsResponse))
                .addHeader("Content-Type","application/json"));

        //Act
        Mono<List<QuestionDTO>> resultMono = this.triviaQuestionService.getQuestions(2,1L,sessionId);

        //Assert
        StepVerifier.create(resultMono)
                .assertNext(list -> {
                    assertEquals(2,list.size());
                    assertEquals("Welke planeet zit het dichtst bij de aarde?",list.get(0).getQuestion());
                    assertTrue(list.get(0).getAnswers().contains("Mars"));

                    assertEquals("Welke planeet ligt het verst van de aarde vandaan?", list.get(1).getQuestion());
                    assertTrue(list.get(1).getAnswers().contains("Mercurius"));
                })
                .verifyComplete();

        //questions map is geupdated
        assertTrue(this.triviaQuestionService.getQuestionsMap()
                .get(sessionId)
                .containsKey("Welke planeet zit het dichtst bij de aarde?"));
        assertTrue(this.triviaQuestionService.getQuestionsMap()
                .get(sessionId)
                .containsKey("Welke planeet ligt het verst van de aarde vandaan?"));
    }


    @Test
    void testGetQuestions_amountExceedsMax_appliesMax() throws Exception{
        //Arrange
        Question question1 = new Question();
        question1.setQuestion("Welke planeet zit het dichtst bij de aarde?");
        question1.setCorrect_answer("Mars");
        question1.setIncorrect_answers(Arrays.asList("Jupiter","Mercurius","Saturnus"));
        question1.setCategory("Wetenschap");
        Question question2 = new Question();
        question2.setQuestion("Welke planeet ligt het verst van de aarde vandaan?");
        question2.setCorrect_answer("Mercurius");
        question2.setIncorrect_answers(Arrays.asList("Jupiter","Mars","Saturnus"));
        question2.setCategory("Wetenschap");

        String sessionId = "BSXAj1u!@WCEd";

        CategoryStatistics stats = new CategoryStatistics();
        stats.setCategory_question_count(new QuestionCount(30,10,10,10));

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(stats))
                .addHeader("Content-Type", "application/json"));

        TriviaQuestionsResponse triviaQuestionsResponse = new TriviaQuestionsResponse();
        triviaQuestionsResponse.setResults(Arrays.asList(question1, question2));

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(triviaQuestionsResponse))
                .addHeader("Content-Type","application/json"));

        //Act
        Mono<List<QuestionDTO>> resultMono = this.triviaQuestionService.getQuestions(100,1L,sessionId);
        //Assert
        StepVerifier.create(resultMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testGetQuestions_invalidAmount_throwsException(){
        String sessionId = "1y8924bC#EB314";
        assertThrows(InvalidRequestException.class, () -> this.triviaQuestionService.getQuestions(0,1L,sessionId));
        assertThrows(InvalidRequestException.class, () -> this.triviaQuestionService.getQuestions(-5,1L,sessionId));
    }

    @Test
    void testGetQuestions_categoryNotFound_throwsException() throws Exception{
        //Arrange
        CategoryStatistics stats = new CategoryStatistics();
        stats.setCategory_question_count(new QuestionCount(0,0,0,0));

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(stats))
                .addHeader("Content-Type", "application/json"));

        //Act
        Mono<List<QuestionDTO>> resultMono = this.triviaQuestionService.getQuestions(5,999L,"bwqyhcfbrqyg341");

        //Assert
        StepVerifier.create(resultMono)
                .expectError(CategoryNotFoundException.class)
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void testCheckAnswers_correctAnswer_returnsTrue(){
        //Arrange
        Question question = new Question();
        question.setQuestion("Hoeveel is 50 + 50?");
        question.setCorrect_answer("100");
        String sessionId = "Session123je12i";
        this.triviaQuestionService.addQuestionForTest(sessionId,question);

        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setQuestion("Hoeveel is 50 + 50?");
        answerDTO.setChosen_answer("100");

        //Act
        //Assert
        assertTrue(this.triviaQuestionService.checkAnswer(sessionId,answerDTO));
    }

    @Test
    void testCheckAnswers_wrongAnswer_returnsFalse(){
        //Arrange
        Question question = new Question();
        question.setQuestion("Bevindt Nederland zich in Europa?");
        question.setCorrect_answer("Ja");
        String sessionId = "Session123je12i";
        this.triviaQuestionService.addQuestionForTest(sessionId,question);

        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setQuestion("Bevindt Nederland zich in Europa?");
        answerDTO.setChosen_answer("Nee");

        //Act
        //Assert
        assertFalse(this.triviaQuestionService.checkAnswer(sessionId,answerDTO));
    }

    @Test
    void testCheckAnswer_questionNotFound_returns404(){
        //Arrange
        Question question = new Question();
        question.setQuestion("Is banaan een fruitsoort?");
        question.setCorrect_answer("Ja");
        String sessionId = "Session123je12i";
        this.triviaQuestionService.addQuestionForTest(sessionId,question);

        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setQuestion("Deze vraag staat niet in de lijst.");
        answerDTO.setChosen_answer("Automerk mercedes");

        //Act
        //Assert
        assertThrows(QuestionNotFoundException.class,
                () -> this.triviaQuestionService.checkAnswer(sessionId,answerDTO));
    }

}
