import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {TopTenEvent} from '../../../dtos/topTenEvent';
import { Chart, CategoryScale, LinearScale, BarElement, Title, BarController } from 'chart.js';
import {AuthService} from '../../../services/auth.service';
import {EventsService} from '../../../services/events.service';
import {Title as HTMLTitle} from '@angular/platform-browser';
import {Router} from '@angular/router';

@Component({
  selector: 'app-top-ten-events',
  templateUrl: './top-ten-events.component.html',
  styleUrls: ['./top-ten-events.component.scss']
})
export class TopTenEventsComponent implements OnInit, AfterViewInit{
  @ViewChild('myChart', { static: false }) chartRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('categorySelect', { static: false }) categorySelect!: ElementRef<HTMLSelectElement>;

  events: TopTenEvent[];
  eventType = 'ALL';

  // eslint-disable-next-line max-len
  constructor(public authService: AuthService, private eventsService: EventsService, private titleService: HTMLTitle,  private router: Router) {
    this.titleService.setTitle('Top 10 Events - Ticketline');
  }

  ngOnInit() {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.eventsService.getTopTenEvents(this.eventType).subscribe({
      next: res => {
        this.events = res;
        console.log(this.events);
        this.createChart();
      },
      error: err => {
        console.log(err);
      }
    });
  }

  ngAfterViewInit() {
    this.categorySelect.nativeElement.addEventListener('change', (event) => {
      this.eventType = (event.target as HTMLSelectElement).value;
      this.eventsService.getTopTenEvents(this.eventType).subscribe({
        next: res => {
          this.events = res;
          console.log(this.events);
          this.createChart();
        },
        error: err => {
          console.log(err);
        }
      });
    });
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString().replace(/\//g, '.');
  }

  createChart() {
    Chart.register(CategoryScale, LinearScale, BarElement, Title, BarController);

    const canvas = this.chartRef.nativeElement;
    const ctx = canvas.getContext('2d');

    const existingChart = Chart.getChart(canvas);

    if (existingChart) {
      existingChart.data.labels = this.events.map(event => event.title);
      existingChart.data.datasets[0].data = this.events.map(event => event.ticketCount);
      existingChart.update();
    } else {
      const chart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: this.events.map(event => event.title),
          datasets: [{
            label: 'Sold Tickets',
            data: this.events.map(event => event.ticketCount),
            backgroundColor: '#784ADE60',
            borderColor: '#784ADE',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          scales: {
            y: {
              beginAtZero: true
            }
          },
          onClick: (event, elements) => {
            if (elements.length > 0) {
              const index = elements[0].index;
              const selectedEvent = this.events[index];
              this.router.navigate(['/events/details', selectedEvent.id]);
            }
          }
        }
      });
    }
  }
}
