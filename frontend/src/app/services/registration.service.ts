import { Injectable } from '@angular/core';
import { User } from '../dtos/user';
import {Observable, tap} from 'rxjs';
import {HttpClient, HttpHeaders, HttpParams, HttpStatusCode} from '@angular/common/http';
import {Globals} from '../global/globals';
import { AllUsersMode } from '../components/all-user/all-user.component';


@Injectable({
  providedIn: 'root'
})
export class RegistrationService {

  private baseUri = this.globals.backendUri + '/users';

  constructor(
    private httpClient: HttpClient,
    private globals: Globals,
  ) { }

  getUser(email: string): Observable<User> {
    return this.httpClient.get<User>(this.baseUri + '/' + email);
  }

  searchUsersWithUsernameAndLockedStatus(userName: string, locked: boolean): Observable<User[]> {
    const params = new HttpParams().set('username', userName).set('locked', locked.toString());
    return this.httpClient.get<User[]>(this.baseUri + '/search', {params});
  }


  getAllUsers(lockedUser: AllUsersMode): Observable<User[]> {
    const params = new HttpParams().set('locked', lockedUser !== AllUsersMode.all);
    return this.httpClient.get<User[]>(this.baseUri,{params});
  }

  register(user: User): Observable<User> {
    return this.httpClient.post<User>(
      this.baseUri,
      user
    );
  }

  sendResetEmail(email: string): Observable<string> {
    const emailObj = {email};
    return this.httpClient.post<string>(
      this.baseUri + '/reset-password',
      emailObj,
      {responseType: 'text' as 'json'}
    );
  }

  setNewPassword(email: string, password: string, token: string): Observable<string> {
    const data = {email, password, token};
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    headers.set('accept', 'text/plain');
    return this.httpClient.post<string>(
      this.baseUri + '/set-new-password',
      data,
      {headers, responseType: 'text' as 'json'}
    );
  }

  update(user: User): Observable<User> {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.getToken()}`);
    return this.httpClient.put<User>(
      this.baseUri,
      user,
      { headers }
    );
  }
  getToken() {
    return localStorage.getItem('token');
  }

  delete(id: string): Observable<HttpStatusCode> {
    return this.httpClient.delete<HttpStatusCode>(this.baseUri + '/' + id);
  }
}
