import {Component, ComponentFactoryResolver, OnInit, ViewContainerRef} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {ActivatedRoute, Router} from '@angular/router';
import {OrderService} from '../../services/order.service';
import {Order, OverviewOrder} from '../../dtos/order';
import {Ticket} from '../../dtos/ticket';
import {ToastrService} from 'ngx-toastr';
import {ConfirmationComponent} from '../cart/checkout/confirmation/confirmation.component';
import { OrderPrintMode, PdfPrintService } from 'src/app/services/pdf-print.service';
import {Seat} from '../../dtos/seat';


export enum OrderMode {
  showOrders,
  cancel
}
@Component({
  selector: 'app-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit{
  ordersExist = false;
  confirmedOrderExists = false;
  reservedOrderExists = false;
  canceledOrdersExists = false;
  mode: OrderMode = OrderMode.showOrders;
  orders: Order[] = [];
  confirmedOrders: OverviewOrder[] = [];
  reservedOrders: OverviewOrder[] = [];
  canceledOrders: OverviewOrder[] = [];

  constructor(public authService: AuthService, public route: ActivatedRoute, private orderService: OrderService,
              private router: Router, private notification: ToastrService, private viewContainerRef: ViewContainerRef,
              private componentFactoryResolver: ComponentFactoryResolver,
              private pdfPrintService: PdfPrintService) { }
  ngOnInit() {
    const userId = this.authService.getTokenUserId();
    this.mode = OrderMode.showOrders;
    this.orderService.getOrdersByUser(userId).subscribe({
      next: res => {
        if(res.length > 0){
          this.ordersExist = true;
        }
        this.orders = res;
        for (const order of this.orders) {
          order.showTickets = false;
          if (order.orderType === 'BUY') {
            this.confirmedOrders.push(order);
            this.confirmedOrderExists = true;
          } else if (order.orderType === 'RESERVATION') {
            this.reservedOrders.push(order);
            this.reservedOrderExists = true;
            console.log(order);
          } else if (order.orderType === 'STORNO') {
            this.canceledOrders.push(order);
            this.canceledOrdersExists = true;
          }
        }
      }
    });
  }
  selectConfirmedTicket(order: OverviewOrder, ticket: Ticket) {
    ticket.reserved = true;
    order.selectedTickets=true;
  }
  unselectConfirmedTicket(order: OverviewOrder, ticket: Ticket) {
    ticket.reserved = false;
    order.selectedTickets = false;
    for (const t of order.tickets) {
      if(t.reserved === true) {
        order.selectedTickets = true;
        break;
      }
    }
  }
  selectReservationTicket(order: OverviewOrder, ticket: Ticket) {
    ticket.reserved = false;
    order.selectedTickets=true;
  }
  unselectReservationTicket(order: OverviewOrder, ticket: Ticket) {
    ticket.reserved = true;
    order.selectedTickets = false;
    for (const t of order.tickets) {
      if(t.reserved === false) {
        order.selectedTickets = true;
        break;
      }
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
  formatPrice(price: number) {
    return price.toFixed(2).replace('.', ',');
  }
  collapseTickets(order: Order) {
    order.showTickets = !order.showTickets;
  }

  printInvoice(orderId: number,orderType: number) {
    // orderType = 1 -> confirmed orders, orderType = 2 -> reserved orders , orderType = 3 -> canceled orders
    const order = orderType === 1 ? this.confirmedOrders[orderId] : orderType === 2 ? this.reservedOrders[orderId]
    : this.canceledOrders[orderId];
    // set orderPrintMode from orderType
    const orderPrintMode = orderType === 1 ? OrderPrintMode.confirmed : orderType === 2 ? OrderPrintMode.reserved
    : OrderPrintMode.canceled;
     this.pdfPrintService.printPdf(order, orderPrintMode);
  }

  printTicket(orderId: number, ticketId: number) {
    this.pdfPrintService.printTicket(this.confirmedOrders[orderId], ticketId);
  }

  buyTickets(order) {
    localStorage.setItem('reservations', JSON.stringify(order));
    this.router.navigate(['/cart/checkout']);
  }
  buyAllTickets(order) {
    for (const ticket of order.tickets) {
      ticket.reserved = false;
    }
    localStorage.setItem('reservations', JSON.stringify(order));
    this.router.navigate(['/cart/checkout']);
  }
  cancelTickets(order, message) {
    if(confirm('Are you sure that you want to cancel '+message+' tickets of order '+order.orderNumber+'?')) {
      this.orderService.cancelTickets(order.id, order).subscribe({
        next: res => {
          console.log(res);
          const comp = this.createConfirmationComponent();
          comp.order = order;
          comp.mode = 2;
        },
        error: err => {
          this.notification.error(err.error);
        }
      });
    }
  }
  cancelAllConfirmedTickets(order) {
    for (const t of order.tickets) {
      t.reserved = true;
    }
    this.cancelTickets(order, 'all');
  }
  cancelAllReservatedTickets(order) {
    for (const t of order.tickets) {
      t.reserved = false;
    }
    this.cancelTickets(order, 'all');
  }
  createConfirmationComponent(): ConfirmationComponent {
    this.viewContainerRef.clear();
    this.mode = OrderMode.cancel;
    const componentFactory = this.componentFactoryResolver.resolveComponentFactory(ConfirmationComponent);
    const componentRef = this.viewContainerRef.createComponent(componentFactory);
    return componentRef.instance;
  }
  getSeatName(seat: Seat) {
    if(seat.standingSector) {
      return 'standing';
    } else {
      return seat.number;
    }
  }
}
