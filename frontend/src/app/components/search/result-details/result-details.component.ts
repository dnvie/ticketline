import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../../services/auth.service';
import {EventsService} from '../../../services/events.service';
import {ActivatedRoute, Router} from '@angular/router';
import {SimpleEvent} from '../../../dtos/simpleEvent';
import {Title} from '@angular/platform-browser';

export enum EventsMode {
  artist,
  location
}
@Component({
  selector: 'app-result-details',
  templateUrl: './result-details.component.html',
  styleUrls: ['./result-details.component.scss']
})
export class ResultDetailsComponent implements OnInit {
  mode: EventsMode;
  correctText = '';
  events: SimpleEvent[] = [];
  constructor(public authService: AuthService,private eventsService: EventsService,public route: ActivatedRoute, private router: Router,
              private titleService: Title) {
    this.titleService.setTitle('Search Results - Ticketline');
  }
  ngOnInit() {
    this.route.data.subscribe(data => {
      this.mode = data.mode;
    });
    if(this.mode === EventsMode.artist) {
      this.route.params.subscribe(params => {
        console.log(100);
        const name = params['name'];
        this.correctText = 'from '+name;
        this.eventsService.getEventsByArtistName(name).subscribe({
          next: res => {
            this.events = res;
            console.log(this.events);
          },
          error: err => {
            console.log(err);
          }
        });
      });
    } else if(this.mode === EventsMode.location) {
      console.log(200);
      this.route.params.subscribe(params => {
        const name = params['name'];
        this.correctText = 'at '+name;
        this.eventsService.getEventsByLocation(name).subscribe({
          next: res => {
            this.events = res;
            console.log(this.events);
          },
          error: err => {
            console.log(err);
          }
        });
      });
    }
  }
  formatDate(date: string): string {
    return new Date(date).toLocaleDateString().replace(/\//g, '.');
  }
  redirectToEventDetails(eventId: string) {
    this.router.navigateByUrl(`/events/details/${eventId}`);
  }

}
