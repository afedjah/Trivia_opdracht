import { HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";

@Injectable({
    providedIn: 'root'
})
export class SessionService{
    private sessionIdKey = 'x-sessionId';

    getSessionId():string{
        let sessionId = localStorage.getItem(this.sessionIdKey);
        if(!sessionId){
            sessionId = crypto.randomUUID();
            localStorage.setItem(this.sessionIdKey, sessionId);
        }
        return sessionId;
    }

    getHeaders():HttpHeaders{
        return new HttpHeaders({'x-sessionId':this.getSessionId()});
    }
}