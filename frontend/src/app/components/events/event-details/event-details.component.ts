import {Component, OnInit} from '@angular/core';
import {EventsService} from '../../../services/events.service';
import {AuthService} from '../../../services/auth.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Event} from '../../../dtos/event';
import {Performance} from '../../../dtos/performance';
import {Title} from '@angular/platform-browser';

@Component({
  selector: 'app-event-details',
  templateUrl: './event-details.component.html',
  styleUrls: ['./event-details.component.scss']
})
export class EventDetailsComponent implements OnInit{

  event: Event = {title: '', performers: [], type: '', performances: [], beginDate: new Date(), endDate: new Date(), image: undefined};
  eventId: number;

  constructor(public authService: AuthService, private eventsService: EventsService, public route: ActivatedRoute,
              private titleService: Title, private router: Router) {
    this.titleService.setTitle('Event Details - Ticketline');
  }

  ngOnInit() {
    // für fertige implementierung im backend
    this.route.params.subscribe(params => {
      const id = params['id'];
      this.eventsService.getEventById(id).subscribe({
        next: res => {
          this.event = res;
          this.eventId = id;
          console.log(this.event);
        },
        error: err => {
          console.log(err);
        }

      });
    });
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
      console.log(time + indexOfT);
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

  formatPrice(performance: Performance) {
    return performance.price.toFixed(2).replace('.', ',');
  }

 public addToCart(performanceId: number){



    // check if there is already a shopping cart
    const shoppingCart = JSON.parse(localStorage.getItem('shoppingCart'));

    if(shoppingCart === null){
      localStorage.setItem('shoppingCart', JSON.stringify([{eventId: this.eventId, performanceId, amount: 1}]));
      return;
    }

    // check if the event is already in the shopping cart
    for (const item of shoppingCart){
      if(item.eventId === this.eventId && item.performanceId === performanceId){
        item.amount++;
        localStorage.setItem('shoppingCart', JSON.stringify(shoppingCart));
        return;
      }
    }

    // if the event is not in the shopping cart, add it
    shoppingCart.push({eventId: this.eventId, performanceId, amount: 1});
    localStorage.setItem('shoppingCart', JSON.stringify(shoppingCart));
  }

  selectSeats(performanceId: number, seatmapId: string): void {
    this.router.navigate(['/seat-selection/buy/' + performanceId + '/' + seatmapId]);
  }
}


