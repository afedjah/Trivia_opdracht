import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TriviaQuestionService } from '../../services/trivia.questions.service';
import { QuestionDTO } from '../../models/question.dto';
import { AnswerDTO } from '../../models/answer.dto';
import { TriviaCategory } from '../../models/category.dto';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-trivia',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './trivia.component.html',
  styleUrls: ['./trivia.component.css']
})
export class TriviaComponent implements OnInit {
  //Selectie
  categories: TriviaCategory[] = [];
  selectedCategory:number | null =null;
  amount: number = 10;
  isGameStarted = false;

  //Spel
  questions: QuestionDTO[] = [];
  currentIndex = 0;
  selectedAnswer: string | null = null;
  feedback: string | null = null;
  rightAnswersCount:number = 0;
  wrongAnswersCount:number = 0;

  constructor(private triviaService: TriviaQuestionService) {}

  ngOnInit(): void {
    this.triviaService.getCategories().subscribe({
      next: (data) => {
        this.categories = data;
      },
      error: (err) => console.error(err)
    });
  }

  startGame(){
    if(!this.amount || !this.selectedCategory) return;
    this.triviaService.getQuestions(this.amount, this.selectedCategory).subscribe({
      next: data =>{
        this.questions = data;
        this.isGameStarted = true;
        this.currentIndex = 0;
        this.selectedAnswer = null;
        this.feedback = null;
        this.wrongAnswersCount = 0;
        this.rightAnswersCount = 0;
      },
      error: err => console.error(err)
    });
  }

  selectAnswer(answer: string) {
    this.selectedAnswer = answer;
  }

  submitAnswer() {
    if (!this.selectedAnswer) return;

    const answerDTO: AnswerDTO = {
      question: this.questions[this.currentIndex].question,
      chosen_answer: this.selectedAnswer
    };
    this.triviaService.checkAnswer(answerDTO).subscribe({
      next: (isCorrect) => {
        if(isCorrect){
          this.feedback = '✔ Correct!';
          this.rightAnswersCount++;
        }
        else{
          this.feedback = '✖ Fout!';
          this.wrongAnswersCount++;
        }
      },
      error: (err) => console.error(err)
    });
  }

  nextQuestion() {
    this.currentIndex++;
    this.selectedAnswer = null;
    this.feedback = null;
    if(this.currentIndex >= this.questions.length){
      this.isGameStarted =false;
    }
  }
}