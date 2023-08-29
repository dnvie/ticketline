import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Message} from '../dtos/message';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import {Order} from '../dtos/order';
import {Address} from '../dtos/address';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private messageBaseUri: string = this.globals.backendUri + '/orders';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }



  getOrdersByUser(userId: string): Observable<Order[]> {
    return this.httpClient.get<Order[]>(this.messageBaseUri + '/users/' + userId);
  }

  createOrderByUser(userId: string, data): Observable<Message> {
    return this.httpClient.post<Message>(this.messageBaseUri + '/users/' + userId, data);
  }

  buyAllTicketsForOrder(orderId: string, data): Observable<Message> {
    return this.httpClient.put<Message>(this.messageBaseUri + '/buy/' + orderId, data);
  }

  cancelTickets(orderId: string, data): Observable<Order> {
    console.log(data);
    return this.httpClient.put<Order>(this.messageBaseUri + '/cancel/' + orderId, data);
  }
  findAddressOfLastOrder(userId: string): Observable<Address> {
    return this.httpClient.get<Address>(this.messageBaseUri + '/users/address/' + userId);
  }
}
