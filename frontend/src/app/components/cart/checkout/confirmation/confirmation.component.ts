import {Component} from '@angular/core';
import {AuthService} from '../../../../services/auth.service';
import {Router} from '@angular/router';
import { OrderPrintMode, PdfPrintService } from 'src/app/services/pdf-print.service';
import { OrderService } from 'src/app/services/order.service';

export enum ConfirmationMode {
  orderConfirmed,
  reservationConfirmed,
  cancellationConfirmed,
  askForConfirmation
}
@Component({
  selector: 'app-confirmation',
  templateUrl: './confirmation.component.html',
  styleUrls: ['./confirmation.component.scss']
})
export class ConfirmationComponent {
  order;
  mode: ConfirmationMode;
  constructor(public authService: AuthService, private router: Router, private pdfPrintService: PdfPrintService
    ,private orderService: OrderService) { }
  goToOrders() {
    if(this.mode === 2) {
      window.location.reload();
    }
    const id = this.authService.getTokenUserId();
    this.router.navigateByUrl('orders/'+id);
  }
  navigateToEvents() {
    this.router.navigate(['events']);
  }
  getImage() {
    if(this.mode === 0) {
      return '/assets/Oder_Confirm.svg';
    }
    if(this.mode === 1) {
      return '/assets/Reservation.svg';
    }
    if(this.mode === 2) {
      return '/assets/Canceled_Order.svg';
    }
  }
  getMessage() {
    if(this.mode === 0) {
      return 'Order Confirmed!';
    }
    if(this.mode === 1) {
      return 'Reservation Confirmed!';
    }
    if(this.mode === 2) {
      return 'Order Cancelled!';
    }
  }
  getFindMessage() {
    if(this.mode === 0) {
      return 'Tickets';
    }
    if(this.mode === 1) {
      return 'Reservation';
    }
    if(this.mode === 2) {
      return 'Cancellation';
    }
  }

  printInvoice(order) {

    // get orders by user id and filter by order id
    this.orderService.getOrdersByUser(this.authService.getTokenUserId()).subscribe({
      next: (res) => {
        const filteredOrders = res.filter((o) => o.id === order.id);
        // set print according to this.mode
        const printMode = this.mode === 0 ? OrderPrintMode.confirmed : this.mode === 1 ? OrderPrintMode.reserved : OrderPrintMode.canceled;
        this.pdfPrintService.printPdf(filteredOrders[0],printMode);
      },
      error: (err) => {
        console.log('err: ', err);
      },
    });
  }

}
