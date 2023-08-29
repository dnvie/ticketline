import {Component, OnInit, ViewContainerRef, ComponentFactoryResolver} from '@angular/core';
import {AuthService} from '../../../services/auth.service';
import {EventsService} from '../../../services/events.service';
import {ActivatedRoute, Router} from '@angular/router';
import {OrderService} from '../../../services/order.service';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {Event} from '../../../dtos/event';
import {ConfirmationComponent} from './confirmation/confirmation.component';
import { number, cardholderName, expirationDate, cvv} from 'card-validator';
import * as IBAN from 'iban';
import {Ticket} from '../../../dtos/ticket';
import {ToastrService} from 'ngx-toastr';
import {Seat} from '../../../dtos/seat';



export enum CheckoutMode {
  personalInfo,
  payment,
  overview,
  finished
}
export enum PaymentMode {
  notSpecified,
  creditCard,
  bankTransfer
}
@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss']
})
export class CheckoutComponent implements OnInit {
  mode: CheckoutMode = CheckoutMode.personalInfo;
  paymentMode: PaymentMode = PaymentMode.notSpecified;
  events: Event[] = [];
  orders: [];
  shoppingCartEmpty = false;
  reservationsEmpty = false;
  totalPrice = 0;
  shoppingCart;
  reservation;
  selectedPaymentMode = 'CREDITCARD';
  cardType = '';
  ticketsByEventArray;
  billingAddress: {
    firstName: string;
    lastName: string;
    country: string;
    city: string;
    zip: string;
    street: string;
  };
  inputFormControl = new FormControl('', [Validators.required]);
  billingForm = new FormGroup({
    firstName: new FormControl('', Validators.required),
    lastName: new FormControl('', Validators.required),
    country: new FormControl('', Validators.required),
    city: new FormControl('', Validators.required),
    zip: new FormControl('', Validators.required),
    street: new FormControl('', Validators.required),
    paymentMethod: new FormControl(''),
  });
  creditCardForm = new FormGroup({
    cardNumber: new FormControl('',[Validators.required, this.validateCardNumber]),
    cardName: new FormControl('', [Validators.required, this.validateCardName]),
    expiryDate: new FormControl('', [Validators.required, this.validateExpiryDate]),
    cvc: new FormControl('', [Validators.required, this.validateCVC]),
  });
  bankTransferForm = new FormGroup({
    iban: new FormControl('',[Validators.required, this.validateIban]),
    bic: new FormControl('', [Validators.required, this.validateBic]),
    bankCardName: new FormControl('', [Validators.required, this.validateCardName]),
  });
  constructor(public authService: AuthService, private eventsService: EventsService, public route: ActivatedRoute,
              private orderService: OrderService, private router: Router, private viewContainerRef: ViewContainerRef,
              private componentFactoryResolver: ComponentFactoryResolver, private notification: ToastrService) {}
  ngOnInit() {
    this.mode = CheckoutMode.personalInfo;
    this.paymentMode = PaymentMode.notSpecified;
    this.shoppingCart = JSON.parse(localStorage.getItem('shoppingCart'));
    this.reservation = JSON.parse(localStorage.getItem('reservations'));
    if(this.shoppingCart === null || this.shoppingCart.length === 0) {
      this.shoppingCartEmpty = true;
    }
    if(this.reservation === null || this.reservation.length === 0){
      this.reservationsEmpty = true;
    }
    if(this.reservationsEmpty && this.shoppingCartEmpty) {
      this.router.navigateByUrl('/events');
    } else {
      if(!this.reservationsEmpty) {
        const ticketsByEvent = new Map();
        for (const ticket of this.reservation.tickets) {
          const event = ticket.forPerformance.event;
          if (ticketsByEvent.has(event.title)) {
            ticketsByEvent.get(event.title).push(ticket);
          } else {
            ticketsByEvent.set(event.title, [ticket]);
          }
        }
        this.ticketsByEventArray = Array.from(ticketsByEvent, ([event, tickets]) => ({ event, tickets }));
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
      }
      this.billingForm.controls.firstName.setValue(this.ticketsByEventArray[0].tickets[0].forUser.firstName);
      this.billingForm.controls.lastName.setValue(this.ticketsByEventArray[0].tickets[0].forUser.lastName);
      this.getAddressOfLastOrder(this.ticketsByEventArray[0].tickets[0].forUser.id);
    }
  }
  getAddressOfLastOrder(id: string) {
    this.orderService.findAddressOfLastOrder(id).subscribe({
      next: res => {
        console.log(res);
        if (res != null) {
          this.billingForm.controls.street.setValue(res.street);
          this.billingForm.controls.city.setValue(res.city);
          this.billingForm.controls.country.setValue(res.country);
          this.billingForm.controls.zip.setValue(res.zip);
        }
      }
    });
  }
  getSeatName(seat: Seat) {
    if(seat.standingSector) {
      return 'standing';
    } else {
      return seat.number;
    }
  }
  changeModeToPayment() {
    this.mode = CheckoutMode.payment;
  }
  changeModeToOverview() {
    this.mode = CheckoutMode.overview;
  }
  changeModeToPersonalInfo() {
    this.mode = CheckoutMode.personalInfo;
  }
  checkIfBillingAddressSet() {
    if(this.billingForm.value.firstName != null && this.billingForm.value.lastName && this.billingForm.value.country
      && this.billingForm.value.city && this.billingForm.value.zip && this.billingForm.value.street) {
      return true;
    } else {
      return false;
    }
  }
  checkIfPaymentDetailsSetAndValidForCC() {
    if(this.creditCardForm.valid && this.creditCardForm.value.cardName != null && this.creditCardForm.value.cardNumber
      && this.creditCardForm.value.expiryDate && this.creditCardForm.value.cvc && this.billingForm.value.paymentMethod === 'CREDITCARD') {
      return true;
    } else {
      return false;
    }
  }
  checkIfPaymentDetailsSetAndValidForBT() {
    if(this.bankTransferForm.valid && this.bankTransferForm.value.bankCardName != null && this.bankTransferForm.value.bic
      && this.bankTransferForm.value.iban && this.billingForm.value.paymentMethod === 'BANKTRANSFER') {
      return true;
    } else {
      return false;
    }
  }
  calculateTotalPrice(): number{
    if(!this.reservationsEmpty) {
      let price = 0;
      for (const ticket of this.reservation.tickets) {
        if(!ticket.reserved) {
          price += ticket.price;
        }
      }
      return price;
    } else {
      let price = 0;
      for (const entry of this.shoppingCart) {
        price += entry.price;
      }
      return price;
    }
  }
  formatPrice(price: number) {
    return price.toFixed(2).replace('.', ',');
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
  formatDate(ticket: Ticket) {
    const formattedStartDate = this.formatDateTime(ticket.forPerformance.startTime);
    const formattedEndDate = this.formatDateTime(ticket.forPerformance.endTime);
    const startDate = new Date(ticket.forPerformance.startTime);
    const endDate = new Date(ticket.forPerformance.endTime);
    if (startDate.toDateString() === endDate.toDateString()) {
      return '' + formattedStartDate;
    } else {
      return formattedStartDate + ' - ' + formattedEndDate;
    }
  }
  validateCardNumber(control: FormControl) {
    const validationResult = number(control.value);
    if (!validationResult.isValid) {
      return { invalidCardNumber: true };
    }
    return null;
  }
  getCartType(cardNr: string) {
    const validationResult = number(cardNr);
    if (!validationResult.isValid) {
      if(validationResult.card) {
        return this.cardType = validationResult.card.niceType;
      }
    } else if (validationResult) {
      if(validationResult.card) {
        return this.cardType = validationResult.card.niceType;
      }
    }
    return null;
  }
  validateCardName(control: FormControl) {
    const validationResult = cardholderName(control.value);
    if (!validationResult.isValid) {
      return { invalidCardNumber: true };
    }
    return null;
  }
  validateExpiryDate(control: FormControl) {
    const validationResult = expirationDate(control.value);
    if (!validationResult.isValid) {
      return { invalidExpiryDate: true };
    }
    return null;
  }
  validateCVC(control: FormControl) {
    const validationResult = cvv(control.value);
    if (!validationResult.isValid) {
      return { invalidCVC: true };
    }
    return null;
  }
  validateIban(control: FormControl) {
    const value = control.value;
    if (!value) {
      return null;
    }
    if (!IBAN.isValid(value)) {
      return { invalidIBAN: true };
    }
    return null;
  }
  validateBic(control: FormControl) {
    //TODO only validates form of bic and checks if country code of bic and iban are the same,  not the whole content of the bic
    const value = control.value;
    if (!value) {
      return null; // No value entered, skip validation
    }
    // Validate length
    if (value.length < 8 || value.length > 11) {
      return {invalidBIC: true}; // Invalid BIC length
    }
    // Validate bank code (first 4 characters)
    if (!/^[A-Za-z]{4}$/.test(value.substr(0, 4))) {
      return {invalidBIC: true}; // Invalid bank code format
    }
    // Validate country code (next 2 characters)
    if (!/^[A-Za-z]{2}$/.test(value.substr(4, 2))) {
      return {invalidBIC: true}; // Invalid country code format
    }
    // Validate branch code (last 2 or 5 characters)
    if (value.length > 6 && !/^[A-Za-z0-9]{2,5}$/.test(value.substr(6))) {
      return {invalidBIC: true}; // Invalid branch code format
    }

    //check if iban and bic country codes match
    const ibanControl = control.parent?.get('iban');

    if (!ibanControl) {
      return null; // Unable to access the IBAN control, skip validation
    }
    const ibanValue = ibanControl.value;

    // Get country code from IBAN
    const ibanCountryCode = ibanValue?.substr(0, 2).toUpperCase();

    // Get country code from BIC
    const bicCountryCode = value.substr(4, 2).toUpperCase();

    // Compare country codes
    if (ibanCountryCode !== bicCountryCode) {
      return { mismatchedCountryCode: true }; // Mismatched country code
      return null; // Validation passed
    }
  }
  public buyTickets() {
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
    let paymentType = 0;
    let paymentNumber = '0';
    if(!(this.billingForm.value.paymentMethod === 'BANKTRANSFER')) {
      paymentType = 1;
      paymentNumber = this.creditCardForm.value.cardNumber.slice(0, -4).replace(/\d/g, '*')
        + this.creditCardForm.value.cardNumber.slice(-4);
    } else {
      paymentNumber = this.bankTransferForm.value.iban.slice(0, -4).replace(/\d/g, '*')
        + this.bankTransferForm.value.iban.slice(-4);
    }
    const data = {
      orderDate,
      totalPrice: this.calculateTotalPrice(),
      tickets,
      orderType: 0,
      street: this.billingForm.value.street,
      city: this.billingForm.value.city,
      country: this.billingForm.value.country,
      zip: this.billingForm.value.zip,
      paymentType,
      numberOfCard: paymentNumber
    };
    this.orderService.createOrderByUser(id, data).subscribe({
      next: res => {
        localStorage.setItem('shoppingCart', JSON.stringify([]));
        const comp = this.createConfirmationComponent();
        comp.order = res;
        comp.mode = 0;
      },
      error: err => {
        this.notification.error(err.error);
      }
    });
  }
  public buyAllReservatedTickets() {
    const date = new Date();
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    const orderDate = `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
    this.reservation.orderDate = orderDate;
    let paymentType = 0;
    let paymentNumber = '0';
    if(!(this.billingForm.value.paymentMethod === 'BANKTRANSFER')) {
      paymentType = 1;
      paymentNumber = this.creditCardForm.value.cardNumber.slice(0, -4).replace(/\d/g, '*')
        + this.creditCardForm.value.cardNumber.slice(-4);
    } else {
      paymentNumber = this.bankTransferForm.value.iban.slice(0, -4).replace(/\d/g, '*')
        + this.bankTransferForm.value.iban.slice(-4);
    }
    this.reservation.street = this.billingForm.value.street;
    this.reservation.city = this.billingForm.value.city;
    this.reservation.country = this.billingForm.value.country;
    this.reservation.zip = this.billingForm.value.zip;
    this.reservation.paymentType = paymentType;
    this.reservation.numberOfCard = paymentNumber;
    console.log(this.reservation);
    this.orderService.buyAllTicketsForOrder(this.reservation.id, this.reservation).subscribe({
      next: res => {
        localStorage.removeItem('reservations');
        const comp = this.createConfirmationComponent();
        comp.order = res;
        comp.mode = 0;
      },
      error: err => {
        this.notification.error(err.error);
      }
    });
  }

  createConfirmationComponent(): ConfirmationComponent {
    this.viewContainerRef.clear();
    this.mode = CheckoutMode.finished;
    const componentFactory = this.componentFactoryResolver.resolveComponentFactory(ConfirmationComponent);
    const componentRef = this.viewContainerRef.createComponent(componentFactory);
    return componentRef.instance;
  }
}
