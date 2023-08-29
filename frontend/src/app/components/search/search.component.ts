import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {EventsService} from '../../services/events.service';
import {SearchParams} from '../../dtos/search';
import {debounceTime, Subject} from 'rxjs';
import {PerformanceSearch} from '../../dtos/performance';
import {Title} from '@angular/platform-browser';
import {TopTenEvent} from '../../dtos/topTenEvent';

export enum ResultMode {
  artist,
  location,
  event,
  performance,
  nothingFound,
  showTopTen
}
@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit {

  mode: ResultMode = ResultMode.event;
  search: SearchParams = {};
  searched = false;
  advancedSearchCollapsed = true;
  locationParamsCollapsed = true;
  eventParamsCollapsed = true;
  performanceParamsCollapsed = true;
  artists: string[] = [];
  locations: Location[] = [];
  events: Event[] = [];
  topTen: TopTenEvent[] = [];
  performances: PerformanceSearch[] = [];
  changeObserver = new Subject<any>();
  constructor(public authService: AuthService, private eventsService: EventsService, private titleService: Title) {
  this.titleService.setTitle('Search - Ticketline');
}
  ngOnInit() {
    this.restoreData();
    this.changeObserver.pipe(
      debounceTime(500),).subscribe({
      next:this.onSearch.bind(this),
    });
  }
  restoreData() {
    const results = localStorage.getItem('searchResults');
    const params = localStorage.getItem('searchParams');
    if (results && params) {
      const data = JSON.parse(results);
      console.log(data);
      this.search = JSON.parse(params);
      this.artists = data.artists;
      this.locations = data.locations;
      console.log(this.locations);
      this.events = data.events;
      this.performances = data.performances;
      this.searched = true;
      if(this.events === null || this.events === undefined || this.events.length === 0) {
        this.events = null;
      }
      if(this.locations === null|| this.locations === undefined || this.locations.length === 0) {
        this.locations = null;
      }
      if(this.artists === null|| this.artists === undefined || this.artists.length === 0) {
        this.artists = null;
      }
      if(this.performances === null|| this.performances === undefined || this.performances.length === 0) {
        this.performances = null;
      }
      if(this.events !== null) {
        this.mode = ResultMode.event;
      } else if(this.performances !== null) {
        this.mode = ResultMode.performance;
      } else if(this.artists !== null) {
        this.mode = ResultMode.artist;
      } else if(this.locations !== null) {
        this.mode = ResultMode.location;
      } else {
        this.mode = ResultMode.nothingFound;
      }
    } else {
      this.eventsService.getTopTenEvents('ALL').subscribe({
        next: res => {
          this.topTen = res;
          this.mode = ResultMode.showTopTen;
        },
        error: err => {
          console.log(err);
        }
      });
    }
  }
  onSearchDebounced(){
    this.changeObserver.next('');
  }
  onSearch() {
    localStorage.removeItem('searchParams');
    localStorage.removeItem('searchResults');
    this.searched = false;
    this.artists = null;
    this.locations = null;
    this.events = null;
    this.performances = null;
    const searchParams: SearchParams = {};
    for (const key in this.search) {
      if (this.search[key]) {
        let value = this.search[key];
        if (typeof value === 'string') {
          value = value.trim();
        }
        if (value !== '') {
          if (key === 'type') {
            searchParams[key] = value.toUpperCase();
          } else if (['start', 'end', 'performanceDate'].includes(key)) {
            searchParams[key] = this.formatDateForSearch(value);
          } else {
            searchParams[key] = value;
          }
        }
      }
    }
    if(Object.keys(searchParams).length === 0) {
      this.searched = false;
      this.mode = ResultMode.showTopTen;
    } else {
      this.eventsService.search(searchParams)
        .subscribe({
          next: data => {
            let artists;
            let locations;
            let events;
            let performances;
            if(data.artists) {
              artists = data.artists.slice(0,20);
            }
            if(data.events) {
              events = data.events.slice(0,20);
            }
            if( data.locations) {
              locations = data.locations.slice(0,20);
            }
            if(data.performances) {
              performances = data.performances.slice(0,20);
            }
            const resultsToStore = {
              artists,
              locations,
              events,
              performances
            };
            localStorage.setItem('searchParams', JSON.stringify(this.search));
            localStorage.setItem('searchResults', JSON.stringify(resultsToStore));
            this.artists = data.artists;
            this.locations = data.locations;
            this.events = data.events;
            this.performances = data.performances;
            this.searched = true;
            if(this.events === null || this.events.length === 0) {
              this.events = null;
            }
            if(this.locations === null || this.locations.length === 0) {
              this.locations = null;
            }
            if(this.artists === null || this.artists.length === 0) {
              this.artists = null;
            }
            if(this.performances === null || this.performances.length === 0) {
              this.performances = null;
            }
            if(this.events !== null) {
              this.mode = ResultMode.event;
            } else if(this.performances !== null) {
              this.mode = ResultMode.performance;
            } else if(this.artists !== null) {
              this.mode = ResultMode.artist;
            } else if(this.locations !== null) {
              this.mode = ResultMode.location;
            } else {
              this.mode = ResultMode.nothingFound;
            }
          }
        });
    }
  }
  collapseAdvancedSearch() {
    this.advancedSearchCollapsed = !this.advancedSearchCollapsed;
  }
  collapseLocationParams() {
    this.locationParamsCollapsed = !this.locationParamsCollapsed;
  }
  collapseEventParams() {
    this.eventParamsCollapsed = !this.eventParamsCollapsed;
  }
  collapsePerformanceParams() {
    this.performanceParamsCollapsed = !this.performanceParamsCollapsed;
  }
  formatDate(date: string): string {
    return new Date(date).toLocaleDateString().replace(/\//g, '.');
  }
  formatDateTime(dateTime: string): string {
    const date = new Date(dateTime);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = String(date.getFullYear());
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const formattedDate = `${day}.${month}.${year}, ${hours}:${minutes}`;
    return formattedDate;
  }
  formatDateForSearch(dateStr: string): string {
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
  clearValues() {
    this.search = {};
    this.onSearch();
    this.mode = ResultMode.showTopTen;
    localStorage.removeItem('searchParams');
    localStorage.removeItem('searchResults');
  }
  setModeToEvent() {
    this.mode = ResultMode.event;
  }
  setModeToLocation() {
    this.mode = ResultMode.location;
  }
  setModeToArtist() {
    this.mode = ResultMode.artist;
  }
  setModeToPerformance() {
    this.mode = ResultMode.performance;
  }

}
