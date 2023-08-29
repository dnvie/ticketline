import {User} from './user';
import {Seat} from './seat';
import {Performance} from './performance';

export class Ticket {
id: number;
price: number;
forUser: User;
forPerformance: Performance;
seat: Seat;
reserved: boolean;
order: number;
}

export class TicketDto {
  forPerformance: number;
  forSeat: string;
  reserved: boolean;
}
