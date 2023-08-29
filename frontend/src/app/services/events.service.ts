import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {SimpleEvent} from '../dtos/simpleEvent';
import {Event} from '../dtos/event';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import {SearchParams, SearchResults} from '../dtos/search';
import {TopTenEvent} from '../dtos/topTenEvent';
import {EventsWithCount} from '../dtos/eventsWithCount';

@Injectable({
  providedIn: 'root'
})
export class EventsService {

  private eventsBaseUri: string = this.globals.backendUri + '/events';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Loads all events from the backend (paginated by page number and size)
   *
   * @param page number of the page to load
   * @param size of the page
   * @return all events
   */
  getAllEvents(page: number, size: number): Observable<EventsWithCount> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.httpClient.get<EventsWithCount>(this.eventsBaseUri, { params });
  }

  /**
   * Loads specific event from the backend
   *
   * @param id of the event to load
   * @return specific event
   */
  getEventById(id: number): Observable<Event> {
    return this.httpClient.get<Event>(this.eventsBaseUri + '/' + id);
  }

  /**
   * Loads specific events from the backend where a artist with a certain name performs
   *
   * @param name of the artist
   * @return specific events
   */
  getEventsByArtistName(name: string): Observable<SimpleEvent[]> {
    return this.httpClient.get<SimpleEvent[]>(this.eventsBaseUri + '/find/artist/' + name);
  }

  /**
   * Loads specific events from the backend where a performance takes place at a certain location
   *
   * @param name of the location
   * @return specific events
   */
  getEventsByLocation(name: string): Observable<SimpleEvent[]> {
    return this.httpClient.get<SimpleEvent[]>(this.eventsBaseUri + '/find/location/' + name);
  }

  /**
   * Creates a new event
   *
   * @param event the event to create
   * @return the created event
   */
  createEvent(event: Event): Observable<Event> {
    console.log(event);
    return this.httpClient.post<Event>(this.eventsBaseUri, event);
  }

  /**
   * Update a new event
   *
   * @param event the updated event
   * @param id the id of the event to update
   * @return the updated event
   */
  updateEvent(event: Event, id: number): Observable<Event> {
    return this.httpClient.put<Event>(this.eventsBaseUri + '/' + id, event);
  }

  /**
   * Delete a new event
   *
   * @param id the id of the event to delete
   * @return the deleted event
   */
  deleteEvent(id: number): Observable<Event> {
    return this.httpClient.delete<Event>(this.eventsBaseUri + '/' + id);
  }

  /**
   * Search for Events, Locations, Performances and Artists
   *
   * @param searchParams the params to search with
   * @return the found entities
   */
  search(searchParams: SearchParams): Observable<SearchResults> {
    const params = new HttpParams({fromObject: {...searchParams}});
    console.log(params);
    return this.httpClient.get<SearchResults>(this.eventsBaseUri+'/search', { params });
  }

  /**
   * Get top ten events
   *
   * @param type the type (category) of the top ten events
   * @return the top ten events
   */

  getTopTenEvents(type: string): Observable<TopTenEvent[]> {
    return this.httpClient.get<TopTenEvent[]>(this.eventsBaseUri + '/top10?type=' + type);
  }
}
