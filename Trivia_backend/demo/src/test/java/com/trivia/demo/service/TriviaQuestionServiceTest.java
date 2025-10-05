package com.trivia.demo.service;

import com.trivia.demo.exception.CategoryNotFoundException;
import com.trivia.demo.exception.InvalidRequestException;
import com.trivia.demo.exception.QuestionNotFoundException;
import com.trivia.demo.intefaces.IQuestionMap;
import com.trivia.demo.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TriviaQuestionServiceTest {

    @Mock
    private OpenTriviaApiClient apiClient;
    @Mock
    private TriviaSessionService sessionService;
    @Mock
    private IQuestionMap questionMap;
    @InjectMocks
    private TriviaQuestionService triviaQuestionService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.triviaQuestionService = new TriviaQuestionService(this.apiClient, this.sessionService, this.questionMap);
    }

    @Test
    void testGetCategories_returnsList() {
        //Arrange
        TriviaCategory category1 = new TriviaCategory(1L,"Wiskunde");
        TriviaCategory category2 = new TriviaCategory(2L,"Geschiedenis");
        TriviaCategory category3 = new TriviaCategory(3L,"Fietsen");
        TriviaCategory category4 = new TriviaCategory(4L,"Sport");
        TriviaCategoriesResponse response = new TriviaCategoriesResponse();
        response.setTrivia_categories(Arrays.asList(category1,category2,category3,category4));

        when(this.apiClient.getCategories()).thenReturn(Mono.just(response));

        //Act
        Mono<List<TriviaCategory>> resultMono = this.triviaQuestionService.getCategories();

        //Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(list ->
                        list.size() == 4
                        && list.getFirst().getName().equals("Wiskunde")
                        && list.get(1).getName().equals("Geschiedenis")
                        && list.get(2).getName().equals("Fietsen")
                        && list.get(3).getName().equals("Sport"))
                .verifyComplete();
    }

    @Test
    void testGetQuestions_validAmount_returnsQuestionDTOs(){
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
        int amount = 2;
        Long categoryId = 1L;

        TriviaQuestionsResponse triviaQuestionsResponse = new TriviaQuestionsResponse();
        triviaQuestionsResponse.setResponse_code(0);
        triviaQuestionsResponse.setResults(Arrays.asList(question1,question2));

        when(this.sessionService.getOrCreateSessionToken(sessionId)).thenReturn(Mono.just("testToken"));
        when(this.apiClient.getTotalAmountOfQuestionsForCategory(categoryId)).thenReturn(Mono.just(10));
        when(this.apiClient.getQuestions(amount,categoryId,"testToken")).thenReturn(Mono.just(triviaQuestionsResponse));
        doNothing().when(this.questionMap).addQuestion(anyString(),any(Question.class));

        //Act
        Mono<List<QuestionDTO>> resultMono = this.triviaQuestionService.getQuestions(amount,categoryId,sessionId);

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
    }


    @Test
    void testGetQuestions_amountExceedsAvailable_appliesMax(){
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
        Long categoryId = 1L;
        int requestedAmount = 150;
        int availableAmount = 30;
        String token = "testToken";

        TriviaQuestionsResponse triviaQuestionsResponse = new TriviaQuestionsResponse();
        triviaQuestionsResponse.setResults(Arrays.asList(question1, question2));
        triviaQuestionsResponse.setResponse_code(0);

        when(this.sessionService.getOrCreateSessionToken(sessionId)).thenReturn(Mono.just(token));
        when(this.apiClient.getTotalAmountOfQuestionsForCategory(categoryId)).thenReturn(Mono.just(availableAmount));

        when(this.apiClient.getQuestions(30,categoryId,token)).thenReturn(Mono.just(triviaQuestionsResponse));
        doNothing().when(this.questionMap).addQuestion(anyString(), any(Question.class));

        //Act
        Mono<List<QuestionDTO>> resultMono = this.triviaQuestionService.getQuestions(requestedAmount,categoryId,sessionId);
        //Assert
        StepVerifier.create(resultMono)
                .assertNext(list ->{
                    assertEquals(2,list.size());
                    assertEquals("Welke planeet zit het dichtst bij de aarde?",list.getFirst().getQuestion());
                    assertTrue(list.getFirst().getAnswers().contains("Mars"));
                    assertEquals("Welke planeet ligt het verst van de aarde vandaan?", list.get(1).getQuestion());
                    assertTrue(list.get(1).getAnswers().contains("Mercurius"));
                })
                .verifyComplete();
    }

    @Test
    void testGetQuestions_invalidAmount_throwsInvalidRequestException(){
        //Arrange
        String sessionId = "1y8924bC#EB314";
        Long categoryId = 1L;

        //Act
        //Assert
        assertThrows(InvalidRequestException.class, () -> this.triviaQuestionService.getQuestions(0,categoryId,sessionId));
        assertThrows(InvalidRequestException.class, () -> this.triviaQuestionService.getQuestions(-5,categoryId,sessionId));
    }

    @Test
    void testGetQuestions_nonExistingCategory_throwsCategoryNotFoundException(){
        //Arrange
        String sessionId = "nqejicbiq";
        Long categoryId = 99999L;
        int amount = 10;

        when(this.sessionService.getOrCreateSessionToken(sessionId)).thenReturn(Mono.just("testToken"));
        when(this.apiClient.getTotalAmountOfQuestionsForCategory(categoryId)).thenReturn(Mono.just(0));

        //Act
        Mono<List<QuestionDTO>> resultMono = this.triviaQuestionService.getQuestions(amount,categoryId,sessionId);

        //Assert
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof CategoryNotFoundException &&
                        throwable.getMessage().contains("Categorie met Id 99999 is niet gevonden."))
                .verify();
    }

    @Test
    void getQuestions_withInvalidToken_retriesWithNewToken(){
        //Arrange
        String sessionId = "Kwn1ecnejcn";
        Long categoryId = 1L;
        int amount = 1;
        String oldToken = "oldToken";
        String newToken = "newTOken";

        when(this.sessionService.getOrCreateSessionToken(sessionId)).thenReturn(Mono.just(oldToken));
        when(this.apiClient.getTotalAmountOfQuestionsForCategory(categoryId)).thenReturn(Mono.just(10));

        TriviaQuestionsResponse invalidResponse = new TriviaQuestionsResponse();
        invalidResponse.setResponse_code(4);
        invalidResponse.setResults(Collections.emptyList());

        Question question1 = new Question();
        question1.setQuestion("Welke planeet zit het dichtst bij de aarde?");
        question1.setCorrect_answer("Mars");
        question1.setIncorrect_answers(Arrays.asList("Jupiter","Mercurius","Saturnus"));
        question1.setCategory("Wetenschap");

        TriviaQuestionsResponse validResponse = new TriviaQuestionsResponse();
        validResponse.setResults(Arrays.asList(question1));
        validResponse.setResponse_code(0);

        when(this.apiClient.getQuestions(amount,categoryId,oldToken)).thenReturn(Mono.just(invalidResponse));
        when(this.sessionService.ensureValidToken(sessionId,4)).thenReturn(Mono.just(newToken));
        when(this.apiClient.getQuestions(amount,categoryId,newToken)).thenReturn(Mono.just(validResponse));
        doNothing().when(this.questionMap).addQuestion(anyString(), any(Question.class));

        //Act
        Mono<List<QuestionDTO>> resultMono = this.triviaQuestionService.getQuestions(amount,categoryId, sessionId);

        //Assert
        StepVerifier.create(resultMono)
                .assertNext(list -> {
                    assertEquals(1, list.size());
                    assertEquals("Welke planeet zit het dichtst bij de aarde?", list.getFirst().getQuestion());
                    assertTrue(list.getFirst().getAnswers().contains("Mars"));
                })
                .verifyComplete();
    }

    @Test
    void testCheckAnswers_correctAnswer_returnsTrue(){
        //Arrange
        Question question = new Question();
        question.setQuestion("Hoeveel is 50 + 50?");
        question.setCorrect_answer("100");
        String sessionId = "Session123je12i";

        Map<String, Question> questionMap = new HashMap<>();
        questionMap.put(question.getQuestion(),question);

        when(this.questionMap.getQuestionsForSession(sessionId)).thenReturn(questionMap);

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

        Map<String, Question> questionMap = new HashMap<>();
        questionMap.put(question.getQuestion(),question);

        when(this.questionMap.getQuestionsForSession(sessionId)).thenReturn(questionMap);

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

        Map<String, Question> questionMap = new HashMap<>();
        questionMap.put(question.getQuestion(),question);

        when(this.questionMap.getQuestionsForSession(sessionId)).thenReturn(questionMap);

        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setQuestion("Deze vraag staat niet in de lijst.");
        answerDTO.setChosen_answer("Automerk mercedes");

        //Act
        //Assert
        assertThrows(QuestionNotFoundException.class,
                () -> this.triviaQuestionService.checkAnswer(sessionId,answerDTO));
    }

}
