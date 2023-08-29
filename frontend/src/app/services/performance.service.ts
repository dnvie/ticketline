import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Performance} from '../dtos/performance';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';

@Injectable({
  providedIn: 'root'
})
export class PerformanceService {

  private eventsBaseUri: string = this.globals.backendUri + '/performance';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Loads specific performance from the backend
   *
   * @return specific performance
   * @param id the id of the performance to load
   */
  getPerformanceById(id: number): Observable<Performance> {
    return this.httpClient.get<Performance>(this.eventsBaseUri + '/' + id);
  }

  /**
   * Updates a performance
   *
   * @param performance the updated performance
   * @param id the id of the performance to update
   * @return the updated performance
   */
  updatePerformance(performance: Performance, id: number): Observable<Performance> {
    return this.httpClient.put<Performance>(this.eventsBaseUri + '/' + id, performance);
  }

  /**
   * Deletes a performance
   *
   * @param id the id of the performance to delete
   * @return the deleted performance
   */
  deletePerformance(id: number): Observable<Performance> {
    return this.httpClient.delete<Performance>(this.eventsBaseUri + '/' + id);
  }
}
