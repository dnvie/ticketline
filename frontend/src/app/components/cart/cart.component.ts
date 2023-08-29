import {Component, ComponentFactoryResolver, OnInit, ViewContainerRef} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { EventsService } from 'src/app/services/events.service';
import {Event} from '../../dtos/event';
import { OrderService } from 'src/app/services/order.service';
import {TicketService} from '../../services/ticket.service';
import {Ticket} from '../../dtos/ticket';
import {Title} from '@angular/platform-browser';
import {ConfirmationComponent} from './checkout/confirmation/confirmation.component';
import {ToastrService} from 'ngx-toastr';
import {Seat} from '../../dtos/seat';


@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss']
})
export class CartComponent implements OnInit{

  event: Event = {title: ' ', performers: [], type: '', performances: [], beginDate: new Date(), endDate: new Date(), image: undefined};
  eventId: number;
  events: Event[] = [];
  shoppingCart;
  totalPrice = 0;
  shoppingCartEmpty = false;
  checkoutViaReserve = false;
  ticketsByEventArray;
  paidOrders = [];

  constructor(private orderService: OrderService,
              private ticketService: TicketService,
              public authService: AuthService,
              private eventsService: EventsService,
              public route: ActivatedRoute,
              private titleService: Title,
              private notification: ToastrService,
              private viewContainerRef: ViewContainerRef,
              private componentFactoryResolver: ComponentFactoryResolver) {
    this.titleService.setTitle('Cart - Ticketline');
  }

  ngOnInit() {
    //get shopping cart from local storage
    this.shoppingCart = JSON.parse(localStorage.getItem('shoppingCart'));
    // check if shopping cart is empty
    if (this.shoppingCart === null || this.shoppingCart.length === 0) {
      this.shoppingCartEmpty = true;
    } else {
      const ticketsByEvent = new Map();
      for (const ticket of this.shoppingCart) {
        const event = ticket.forPerformance.event;
        if (ticketsByEvent.has(event.title)) {
          ticketsByEvent.get(event.title).push(ticket);
        } else {
          ticketsByEvent.set(event.title, [ticket]);
        }
      }
      this.ticketsByEventArray = Array.from(ticketsByEvent, ([event, tickets]) => ({ event, tickets }));
      console.log(this.ticketsByEventArray);
    }
  }

  getSeatName(seat: Seat) {
    if(seat.standingSector) {
      return 'standing';
    } else {
      return seat.number;
    }
  }

  formatPerformerNames(performers: string[]): string {
    let result = '';
    for (const performer of performers){
      if (performers.length > 0){
        if (performer === performers[performers.length - 1]){
          result += performer;
          break;
        }
        result += performer + ', ';
      }else{
        result += performer;
      }
    }
    return result;
  }

  formatSpecificTime(time: string): string {
    const indexOfT = time.indexOf('T');
    if (indexOfT !== -1){
      const restAfterT = time.substring(indexOfT + 1);
      const splitTime = restAfterT.split(':');
      if(splitTime.length >= 3){
        return splitTime[0] + ':' + splitTime[1];
      }
      return restAfterT;
    }else{
      return time;
    }
  }
  formatDateTime(date: string): string{
    const indexOfT = date.indexOf('T');
    if(indexOfT !== -1){
      const beforeT = date.substring(0, indexOfT);
      const splitDate = beforeT.split('-');
      if(splitDate.length >= 3){
        return splitDate[2] + '.' + splitDate[1] + '.' + splitDate[0];
      }
      return beforeT;
    }else{
      return date;
    }
  }

  public calculatePriceOfPerformance(performanceId: number): number{
    let price = 0;
    for(const event of this.events) {
      for (const performance of event.performances) {
        if(performanceId === performance.id) {
          price = performance.price;
        }
      }
    }
    return price;
  }
  public getAmount(performanceId: number) {
    let amount = 0;
    for (const entry of this.shoppingCart){
     if(entry.performanceId === performanceId){
       amount = entry.amount;
     }
    }
    return amount;
  }

  calculateTotalPrice(): number{
    let price = 0;
    for (const entry of this.shoppingCart) {
      price += entry.price;
    }
    return price;
  }
  formatPrice(price: number) {
    return price.toFixed(2).replace('.', ',');
  }

  public getTicketNumber(performanceId: number): number{
    const shoppingCart = JSON.parse(localStorage.getItem('shoppingCart'));
    if (shoppingCart === null){
      return 0;
    }
    for (const item of shoppingCart){
      if (item.eventId === this.eventId && item.performanceId === performanceId){
        return item.amount;
      }
    }
    return 0;
  }

  public getTotalPriceofOrder(orderId): number{
    //get the total price of the order from paidOrders
    const orders = this.paidOrders.find(order => order.id === orderId);
    return orders.totalPrice;
  }

  public removeFromCart(ticket: Ticket){
    this.ticketService.deleteTicket(ticket.id).subscribe(() => {
    });
    window.location.reload();
  }
  public reserveTickets() {
    const id = this.authService.getTokenUserId();
    const date = new Date();
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    const orderDate = `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
    const tickets = [];
    for (const item of this.shoppingCart) {
      tickets.push(item);
    }
    const data = {
      orderDate,
      totalPrice: this.calculateTotalPrice(),
      tickets,
      orderType: 1
    };
    this.orderService.createOrderByUser(id, data).subscribe({
      next: res => {
        localStorage.setItem('shoppingCart', JSON.stringify([]));
        const comp = this.createConfirmationComponent();
        comp.order = res;
        comp.mode = 1;
        this.checkoutViaReserve = true;
      },
      error: err => {
        this.notification.error(err.error);
      }
    });
  }
  createConfirmationComponent(): ConfirmationComponent {
    this.viewContainerRef.clear();
    const componentFactory = this.componentFactoryResolver.resolveComponentFactory(ConfirmationComponent);
    const componentRef = this.viewContainerRef.createComponent(componentFactory);
    return componentRef.instance;
  }
}
