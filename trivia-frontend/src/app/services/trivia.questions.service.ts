import { Injectable } from "@angular/core"; 
import { HttpClient, HttpHeaders} from "@angular/common/http";
import { Observable } from "rxjs";
import { QuestionDTO } from "../models/question.dto";
import { AnswerDTO } from "../models/answer.dto";
import { TriviaCategory } from "../models/category.dto";
import { SessionService } from "./trivia.session.service";
import { environment } from "../environment/environment";

@Injectable({
    providedIn: 'root'
})
export class TriviaQuestionService{
    private apiUrl = environment.apiUrl;
    constructor(private http: HttpClient, private sessionService:SessionService){}

    getCategories() : Observable<TriviaCategory[]>{
        return this.http.get<TriviaCategory[]>(`${this.apiUrl}/categories`);
    }

    getQuestions(amount:number, categoryId?:number): Observable<QuestionDTO[]>{
        let url = `${this.apiUrl}/questions?amount=${amount}`;
        if(categoryId){
            url += `&categoryId=${categoryId}`;
        }
        return this.http.get<QuestionDTO[]>(url,{
            headers: this.sessionService.getHeaders()
        });
    }


    checkAnswer(answer: AnswerDTO): Observable<boolean>{
        return this.http.post<boolean>(`${this.apiUrl}/checkanswer`, answer,{
            headers : this.sessionService.getHeaders()
        });
    }
}