import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Message} from '../dtos/message';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import {MessagesWithCount} from '../dtos/messagesWithCount';

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  private messageBaseUri: string = this.globals.backendUri + '/messages';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Loads all messages from the backend
   */
  getMessage(page: number, size: number): Observable<MessagesWithCount> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.httpClient.get<MessagesWithCount>(this.messageBaseUri, { params });
  }

  /**
   * Loads specific message from the backend
   *
   * @param id of message to load
   */
  getMessageById(id: number): Observable<Message> {
    return this.httpClient.get<Message>(this.messageBaseUri + '/' + id);
  }

  /**
   * Persists message to the backend
   *
   * @param message to persist
   */
  createMessage(message: Message): Observable<Message> {
    return this.httpClient.post<Message>(this.messageBaseUri, message);
  }

  /**
   * Update a news entry
   *
   * @param message the updated news entry
   * @param id the id of the news entry to update
   * @return the updated news entry
   */
  updateMessage(message: Message, id: number): Observable<Message> {
    return this.httpClient.put<Message>(this.messageBaseUri + '/' + id, message);
  }

  /**
   * Delete a specific news entry
   *
   * @param id of the news entry to delete
   * @return the deleted news entry
   */
  deleteMessage(id: number): Observable<Message> {
    return this.httpClient.delete<Message>(this.messageBaseUri + '/' + id);
  }

  /**
   * Loads all messages from the backend, including those that have been seen by the user
   *
   * @param page number of the page to load
   * @param size of the page
   * @return all messages
   */
  getSeenNews(page: number, size: number): Observable<MessagesWithCount> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.httpClient.get<MessagesWithCount>(this.messageBaseUri + '?showSeen=true', { params });
  }
}
