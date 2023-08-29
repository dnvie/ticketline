import {Component, OnInit, ViewChild} from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { EventsService } from '../../services/events.service';
import { SimpleEvent } from '../../dtos/simpleEvent';
import { Title } from '@angular/platform-browser';
import { MatPaginator, PageEvent } from '@angular/material/paginator';

@Component({
  selector: 'app-events',
  templateUrl: './events.component.html',
  styleUrls: ['./events.component.scss']
})
export class EventsComponent implements OnInit {

  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

  events: SimpleEvent[] = [];
  currentPage = 0;
  totalPages = 0;
  pageSize = 12;
  loaded = false;
  skeletons: any[] = Array(this.pageSize).fill({});

  constructor(
    public authService: AuthService,
    private eventsService: EventsService,
    private titleService: Title
  ) {
    this.titleService.setTitle('Events - Ticketline');
  }

  ngOnInit() {
    this.loaded = false;
    this.loadEvents();
  }

  loadEvents() {
    this.eventsService.getAllEvents(this.currentPage, this.pageSize).subscribe({
      next: res => {
        this.events = res.events;
        this.totalPages = res.totalCount;
        this.loaded = true;
      },
      error: err => {
        console.log(err);
      }
    });
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loaded = false;
    this.loadEvents();
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString().replace(/\//g, '.');
  }
}
