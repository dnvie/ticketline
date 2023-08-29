import {Location} from './location';
import {Event} from './event';
export class Performance {
  id?: number;
  title: string;
  startTime: string;
  endTime: string;
  performers: string[];
  location: Location;
  price: number;
  event?: Event;
  seatMap?: string;
}

export class PerformanceSearch {
  id: number;
  title: string;
  startTime: string;
  endTime: string;
  performers: string[];
  location: Location;
  event: Event;
}


