import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Location} from '../dtos/location';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';

@Injectable({
  providedIn: 'root'
})
export class LocationService {

  private locationsBaseUri: string = this.globals.backendUri + '/locations';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Loads all locations from the backend
   *
   * @return all locations
   */
  getAllLocations(): Observable<Location[]> {
    return this.httpClient.get<Location[]>(this.locationsBaseUri);
  }

  /**
   * Loads all locations from the backend (paginated by page number and size)
   *
   * @param page number of the page to load
   * @param size of the page
   */
  getAllLocationsPaged(page: number, size: number): Observable<Location[]> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.httpClient.get<Location[]>(this.locationsBaseUri, {params});
  }

  /**
   * Loads specified location per id from the backend
   *
   * @param id of the location to load
   */
  getLocationById(id: number): Observable<Location> {
    return this.httpClient.get<Location>(this.locationsBaseUri + '/' + id);
  }

  /**
   * Creates a new Location
   *
   * @param location to create
   */
  createLocation(location: Location): Observable<Location> {
    return this.httpClient.post<Location>(this.locationsBaseUri, location);
  }

  /**
   * Edit a location by id
   *
   * @param location to edit
   */
  editLocation(location: Location): Observable<Location> {
    return this.httpClient.put<Location>(this.locationsBaseUri + '/' + location.id, location);
  }

  /**
   * Deletes a location by id
   *
   * @param id the id of the location to delete
   */
  deleteLocation(id: any) {
    return this.httpClient.delete(this.locationsBaseUri + '/' + id);
  }
}
