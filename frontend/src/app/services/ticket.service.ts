import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Globals} from '../global/globals';
import {Observable} from 'rxjs';
import {Ticket, TicketDto} from '../dtos/ticket';

@Injectable({
  providedIn: 'root'
})
export class TicketService {

  private baseUri = this.globals.backendUri + '/tickets';

  constructor(
    private httpClient: HttpClient,
    private globals: Globals,
  ) {
  }

  getTicketsByUser(): Observable<Ticket[]> {
    return this.httpClient.get<Ticket[]>(this.baseUri);
  }

  getTicketsByPerformance(performanceId: number): Observable<Ticket[]> {
    return this.httpClient.get<Ticket[]>(this.baseUri + '/performance/' + performanceId);
  }

  reserveTicket(performance: number, seat: string): Observable<Ticket> {
    const ticket = new TicketDto();
    ticket.forPerformance = performance;
    ticket.forSeat = seat;
    ticket.reserved = true;
    return this.httpClient.post<Ticket>(this.baseUri, ticket);
  }

  deleteTicket(id: number) {
    this.removeFromCart(id);
    return this.httpClient.delete(this.baseUri + '/' + id);
  }

  getCart(): Ticket[] {
    const cart = JSON.parse(localStorage.getItem('shoppingCart'));
    if (!cart) {
      return [];
    }
    return cart;
  }

  removeFromCart(id: number): Ticket[] {
    const cart = JSON.parse(localStorage.getItem('shoppingCart'));

    const ticket = cart.find(t => t.id === id);

    if (!ticket) {
      return cart;
    }

    const index = cart.indexOf(ticket);
    cart.splice(index, 1);

    localStorage.setItem('shoppingCart', JSON.stringify(cart));

    return cart;
  }

  sizeOfCart(): number {
    const cart = JSON.parse(localStorage.getItem('shoppingCart'));
    if (!cart) {
      return 0;
    }
    return cart.length;
  }

  addTicketToCart(ticket: Ticket): Ticket[] {
    const cart = JSON.parse(localStorage.getItem('shoppingCart'));

    if (!cart) {
      localStorage.setItem('shoppingCart', JSON.stringify([ticket]));
      return [ticket];
    }

    cart.push(ticket);

    localStorage.setItem('shoppingCart', JSON.stringify(cart));

    return cart;
  }

  clearCart() {
    this.getCart().forEach(t => this.deleteTicket(t.id));
    localStorage.removeItem('shoppingCart');
  }
}

