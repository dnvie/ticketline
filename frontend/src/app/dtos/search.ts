import {PerformanceSearch} from './performance';

export class SearchParams {
  searchBar?: string;
  street?: string;
  zip?: string;
  city?: string;
  country?: string;
  type?: string;
  start?: string;
  end?: string;
  performanceDate?: string;
  performanceTime?: string;
  eventName?: string;
  roomName?: string;
  minPrice?: string;
  maxPrice?: string;

}

export class SearchResults {
  artists: string[];
  locations: Location[];
  events: Event[];
  performances: PerformanceSearch[];

}
