import {Ticket} from './ticket';
import {User} from './user';
export class Order {
  id: number;
  orderBy: User;
  totalPrice: number;
  orderDate: string;
  tickets: Ticket[];
  showTickets?: boolean;
  orderType: string;
  street: string;
  city: string;
  country: string;
  zip: string;
  paymentType: string;
  numberOfCard: string;
}
export class OverviewOrder {
  id: number;
  orderBy: User;
  totalPrice: number;
  orderDate: string;
  tickets: Ticket[];
  showTickets?: boolean;
  orderType: string;
  selectedTickets?: boolean;
  orderNumber?: string;
  street: string | string[];
  zip: string;
  city: string;
  paymentType: string;
  numberOfCard: any;
}
