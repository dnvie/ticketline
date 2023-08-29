import { Injectable } from '@angular/core';
import { jsPDF } from 'jspdf';
import * as QRCode from 'qrcode';
import { AuthService } from './auth.service';
import { RegistrationService } from './registration.service';
import { User } from '../dtos/user';
import {  OverviewOrder } from '../dtos/order';
import { Ticket } from '../dtos/ticket';

export enum OrderPrintMode {
  confirmed,
  reserved,
  canceled,
}
@Injectable({
  providedIn: 'root',
})
export class PdfPrintService {
  user: User = {
    firstName: '',
    lastName: '',
    phoneNumber: '',
    email: '',
  };
  constructor(
    public authService: AuthService,
    public registrationService: RegistrationService
  ) {}

  async printPdf(order: OverviewOrder, orderType: OrderPrintMode){
    //get user data
    const userId = this.authService.getTokenUserId();

    this.registrationService.getUser(userId).subscribe({
      next: async (res) => {
        this.user.firstName = res.firstName;
        this.user.lastName = res.lastName;
        this.user.phoneNumber = res.phoneNumber;
        this.user.email = res.email;
        const doc = new jsPDF();

        doc.addImage('../assets/Tickeline_Logo.png', 'PNG', 50, -30, 115, 115);
        // Header
        doc.setFontSize(19);
        // set header depending on order type
        const header = orderType === OrderPrintMode.confirmed ? 'Rechnung' :
        orderType === OrderPrintMode.reserved ? 'Reservierungsbestätigung' : 'Stornobestätigung';

        doc.text(header+' für deine Ticketline Bestellung', 20, 55);

        // Leistender Unternehmer
        doc.setFontSize(12);
        doc.setFont('helvetica', 'bold');
        doc.text('Dickstoner GMBH', 20, 65);
        doc.setFont('helvetica', 'normal');
        doc.text('Adresse: ', 20, 75);
        doc.text('Resselgasse 4, 1040 Wien', 20, 80);

        // Leistungsempfänger
        doc.setFont('helvetica', 'bold');
        doc.text(
          'Kund*in: ' + this.user.firstName + ' ' + this.user.lastName,
          120,
          65
        );
        if(orderType !== OrderPrintMode.reserved){
        doc.setFont('helvetica', 'normal');
        doc.text('Adresse: ', 120, 75);
        doc.text(order.street + ',', 120, 80);
        doc.text(order.zip+ ' ' + order.city, 120, 85);
        }
        // Rechnungsdetails
        // set type of number depending on order type
        const orderNumber = orderType === OrderPrintMode.confirmed ? 'Rechnungs' :
        orderType === OrderPrintMode.reserved ? 'Reservierungs' : 'Storno';
        doc.setFont('helvetica', 'bold');
        doc.text(orderNumber+' Nr: ' +order.orderNumber, 20, 90);
          // format date to dd.mm.yyyy
        const date = new Date(order.orderDate);
        const day = date.getDate();
        const month = date.getMonth() + 1;
        const year = date.getFullYear();
        const formattedDate = day + '.' + month + '.' + year;
        doc.text(orderNumber+'datum: ' + formattedDate, 120, 90);

        doc.setFont('helvetica', 'bold');
        doc.text('Artikel', 20, 105);
        doc.text('Bezeichnung', 40, 105);
        doc.text('Menge', 100, 105);
        doc.text('Preis', 130, 105);
        doc.text('USt-Satz', 170, 105);
        doc.line(20, 108, 190, 108);

        // Tickets
        const tickets: Ticket[] = order.tickets;
        const yOffSet = tickets.length * 10 + 20;

        for(const [index,ticket] of tickets.entries()){
          doc.setFont('helvetica', 'normal');
          doc.text('Ticket', 20, 115 + index*10);
          // add event title and performance title as description, limit to 30 characters
          // if description is longer, cut it off and add '...'

          const description = ticket.forPerformance.event.title + ' - ' + ticket.forPerformance.title;
          if(description.length > 30){
            doc.text(description.substring(0,27) + '...', 40, 115 + index*10);
          }else{
            doc.text(description, 40, 115 + index*10);
          }
          doc.text('1', 100, 115 + index*10);
          // if order is canceled, show negative price
          const price = orderType === OrderPrintMode.canceled ? ticket.price * -1 : ticket.price;
          doc.text( price.toFixed(2) + ' €', 130, 115 + index*10);
          doc.text('10%', 170, 115 + index*10);
        }
        doc.setFont('helvetica', 'bold');
        const totalPrice = orderType === OrderPrintMode.canceled ? order.totalPrice * -1 : order.totalPrice;
        const totalPriceText = orderType === OrderPrintMode.canceled ? 'Gutschrift: ' : orderType === OrderPrintMode.reserved
        ? 'Zu zahlender Betrag ' : 'Summe ';
        if(orderType === OrderPrintMode.canceled){
          //just show total price if order is canceled
          doc.text(totalPriceText + totalPrice.toFixed(2) + ' €', 145, 105+yOffSet);
        }else{
        doc.text(totalPriceText +'netto: ' + (totalPrice *0.9).toFixed(2)+' €' , 130, 105+yOffSet);
        doc.text('USt: 10 %', 130, 110+yOffSet);
        doc.text(totalPriceText+'brutto: ' + totalPrice.toFixed(2) + ' €', 130, 115+yOffSet);
        }
        // Lieferdatum und Leistungszeitraum
        doc.setFont('helvetica', 'normal');
        const deliveryDate = new Date(order.orderDate);
        // convert to dd.mm.yyyy
        const deliveryDay = deliveryDate.getDate();
        const deliveryMonth = deliveryDate.getMonth() + 1;
        const deliveryYear = deliveryDate.getFullYear();
        const formattedDeliveryDate = deliveryDay + '.' + deliveryMonth + '.' + deliveryYear;
        doc.text('Lieferdatum: '+ formattedDeliveryDate, 20, 130+yOffSet);
        //if order type is confirmed, add next to the delivery date the buy method and the card number
        if(orderType === OrderPrintMode.confirmed){
          doc.text('Bezahlmethode: '+ order.paymentType, 20, 135+yOffSet);
         if(order.numberOfCard){
          doc.text('Kartennummer: '+ order.numberOfCard, 20, 140+yOffSet);
          }
        }
        // set file name depending on order type
        const fileName = orderType === OrderPrintMode.confirmed ? 'rechnung.pdf' :
        orderType === OrderPrintMode.reserved ? 'reservierungsbestaetigung.pdf' : 'stornobestaetigung.pdf';
        doc.save(fileName);
      },
      error: (err) => {
        console.log(err);
      },
    });
  }

  async printTicket(order: OverviewOrder, ticketId: number) {
    const userId = this.authService.getTokenUserId();

    this.registrationService.getUser(userId).subscribe({
      next: async (res) => {
        this.user.firstName = res.firstName;
        this.user.lastName = res.lastName;
        this.user.phoneNumber = res.phoneNumber;
        this.user.email = res.email;

        // unique ticket id
        const qrCodeData =  order.orderBy.id.substring(0,4) + '-' +order.id + '-' + ticketId; // Daten für den QR-Code
        const doc = new jsPDF();
        // QR-Code erstellen
        const qrCodeOptions = {
          margin: 2,
          scale: 4,
          width: 50,
          color: {
            dark: '#000000', // Farbe der dunklen Module des QR-Codes
            light: '#ffffff', // Farbe der hellen Module des QR-Codes
          },
        };

          const qrCodeDataURL = await QRCode.toDataURL(
            qrCodeData,
            qrCodeOptions
          );

        doc.addImage('../assets/Tickeline_Logo.png', 'PNG', 50, -30, 115, 115);
        // Header
        doc.setFontSize(19);
        doc.text('Ticket für deine Ticketline Bestellung', 75, 55, {
          align: 'center',
        });

        // Leistender Unternehmer
        doc.setFontSize(12);
        doc.setFont('helvetica', 'bold');
        doc.text('Dickstoner GMBH', 20, 65);
        doc.setFont('helvetica', 'normal');
        doc.text('Adresse: ', 20, 75);
        doc.text('Resselgasse 4, 1040 Wien', 20, 80);

        // Leistungsempfänger
        doc.setFont('helvetica', 'bold');

        doc.text(
          'Kund*in: ' + this.user.firstName + ' ' + this.user.lastName,
          120,
          65
        );
        doc.setFont('helvetica', 'normal');
        doc.text('Adresse: ', 120, 75);
        doc.text(order.street + ',', 120, 80);
        doc.text(order.zip+ ' ' + order.city, 120, 85);

        // Ticketdetails
        doc.setFont('helvetica', 'bold');
        doc.text('Ticket Nr: ' +order.orderNumber +'-'+ticketId, 20, 90);
        doc.line(20, 93, 190, 93);
        // set event title and performance title as subheader
        doc.setFont('helvetica', 'bold');
        doc.setFontSize(14);
        doc.text('Event: ' + order.tickets[ticketId].forPerformance.event.title, 20, 100);
        // if Performance title is longer than 20 characters, cut it off and add '...'
        const performanceTitle = order.tickets[ticketId].forPerformance.title;
        if(performanceTitle.length > 20){
          doc.text('Performance: '+performanceTitle.substring(0,17) + '...', 20, 105);
        }else{
          doc.text('Performance: '+ performanceTitle, 20, 105);
        }

        // iterate over the performers and add them to the ticket
        doc.text('Künstler*innen: ', 20, 110);
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(12);
        let yOffSet = 0;
        for(const [index,performer] of order.tickets[ticketId].forPerformance.performers.entries()){
          doc.text(performer, 20, 115 + index*5);
          yOffSet = index*5;
        }

        // next to the event title and performance title, add the date, location and description
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(12);
        // get start time using timeformatting
        const startTime = this.formatTime(order.tickets[ticketId].forPerformance.startTime);
        const endTime = this.formatTime(order.tickets[ticketId].forPerformance.endTime);
        doc.text('Start: ' + startTime, 120, 100);
        doc.text('Ende: ' +endTime, 120, 105);
        doc.text('Ort: ' + order.tickets[ticketId].forPerformance.location.name, 120, 110);

        //check if ticket is a seat ticket or a standing ticket
        if(order.tickets[ticketId].seat.standingSector=== true){
          doc.text('Stehplatz', 120, 115);
        }else{
          // print sector and seat number
          // print the text bold
          doc.setFont('helvetica', 'bold');
          doc.text('Sektor: ' + order.tickets[ticketId].seat.sector, 120, 115);
          doc.text('Sitzplatz: ' + order.tickets[ticketId].seat.number, 120, 120);
        }
        // place qr code in the middle of the ticket, below the event details
        doc.addImage(qrCodeDataURL, 'PNG', 65, 140+yOffSet, 80, 80);
        // Lieferdatum
        doc.setFont('helvetica', 'normal');
        const deliveryDate = new Date(order.orderDate);
        // convert to dd.mm.yyyy
        const deliveryDay = deliveryDate.getDate();
        const deliveryMonth = deliveryDate.getMonth() + 1;
        const deliveryYear = deliveryDate.getFullYear();
        const formattedDeliveryDate = deliveryDay + '.' + deliveryMonth + '.' + deliveryYear;

        doc.text('Lieferdatum: '+ formattedDeliveryDate, 20, 260);

        doc.save('ticket.pdf');
      },
      error: (err) => {
        console.log(err);
      }
    });
  }

  /**
   * format time from yyyy-mm-ddThh:mm:ss to dd.mm.yyyy hh:mm
   *
   * @param time  time to format
   * @returns  formatted time
   */
  private formatTime(time: string): string {
    const indexOfT = time.indexOf('T');
    if (indexOfT !== -1) {
      const beforeT = time.substring(0, indexOfT);
      const splitDate = beforeT.split('-');
      if (splitDate.length >= 3) {
        // also add hh:mm
        const afterT = time.substring(indexOfT + 1, time.length);
        const splitTime = afterT.split(':');
        return splitDate[2] + '.' + splitDate[1] + '.' + splitDate[0] + ' ' + splitTime[0] + ':' + splitTime[1]+' Uhr';
      }
      return beforeT;
    } else {
      return time;
    }
  }

}
