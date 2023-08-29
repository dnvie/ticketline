import {Performance} from './performance';

export class Event {
  id?: number;
  title: string;
  type: string;
  beginDate: Date;
  endDate: Date;
  performers: string[];
  performances: Performance[];
  image: string;
}
