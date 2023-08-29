import {
  AfterViewChecked,
  AfterViewInit, ChangeDetectorRef,
  Component,
  OnInit,
  ViewChild,
  ViewContainerRef
} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {PerformanceCreateEditComponent} from '../performance-create-edit/performance-create-edit.component';
import {Event} from '../../../dtos/event';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {EventsService} from '../../../services/events.service';
import {ToastrService} from 'ngx-toastr';
import {LocationService} from 'src/app/services/location.service';
import {SeatmapService} from '../../../services/seatmap.service';
import {forkJoin} from 'rxjs';
import {Title} from '@angular/platform-browser';

export enum EventCreateEditMode {
  create,
  edit,
}

@Component({
  selector: 'app-event-create-edit',
  templateUrl: './event-create-edit.component.html',
  styleUrls: ['./event-create-edit.component.scss']
})

export class EventCreateEditComponent implements AfterViewInit, AfterViewChecked, OnInit {

  @ViewChild('appendPerformances', {read: ViewContainerRef}) appendPerformances: ViewContainerRef;
  mode: EventCreateEditMode = EventCreateEditMode.create;
  id: number;
  heading = 'Add Event';
  event: Event = {
    title: '',
    type: '',
    beginDate: new Date(),
    endDate: new Date(),
    performers: [],
    performances: [],
    image: undefined
  };
  performanceCount = 0;
  totalPerformanceCount = 0;
  components = [];
  startDates = [];
  endDates = [];
  locations = [];
  seatMaps = [];
  performers = new Set<string>();
  allPerformancesValid = true;
  performer = '';
  inputFormControl = new FormControl('', [Validators.required]);
  eventForm = new FormGroup({
    title: new FormControl('', Validators.required),
    performers: new FormControl(''),
    type: new FormControl('', Validators.required)
  });

  constructor(
    private changeDetector: ChangeDetectorRef,
    private eventsService: EventsService,
    private locationService: LocationService,
    private seatMapService: SeatmapService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService, private titleService: Title) {
  }

  addPerformance() {
    if (this.performanceCount < 1) {
      const component = this.appendPerformances.createComponent(PerformanceCreateEditComponent);
      component.instance.id = this.totalPerformanceCount;
      component.instance.parent = this;
      component.instance.locations = this.locations;
      component.instance.seatMaps = this.seatMaps;
      this.components.push(component);
      this.performanceCount++;
      this.totalPerformanceCount++;
      if (this.mode === EventCreateEditMode.create) {
        this.collapsePrevious();
      }
      window.scrollTo(0, document.body.scrollHeight);
    } else {
      // eslint-disable-next-line max-len
      if (this.components[this.performanceCount - 1].instance.performanceForm.valid && this.components[this.performanceCount - 1].instance.locationForm.valid && this.components[this.performanceCount - 1].instance.formControl.valid) {
        const component = this.appendPerformances.createComponent(PerformanceCreateEditComponent);
        component.instance.id = this.totalPerformanceCount;
        component.instance.parent = this;
        component.instance.locations = this.locations;
        component.instance.seatMaps = this.seatMaps;
        this.components.push(component);
        this.performanceCount++;
        this.totalPerformanceCount++;
        if (this.mode === EventCreateEditMode.create) {
          this.collapsePrevious();
        }
        window.scrollTo(0, document.body.scrollHeight);
      } else {
        this.components[this.performanceCount - 1].instance.formControl.markAllAsTouched();
        this.components[this.performanceCount - 1].instance.locationForm.markAllAsTouched();
        this.components[this.performanceCount - 1].instance.performanceForm.markAllAsTouched();
      }
    }
  }

  addInitialPerformance() {
    const component = this.appendPerformances.createComponent(PerformanceCreateEditComponent);
    component.instance.id = this.totalPerformanceCount;
    component.instance.parent = this;
    this.components.push(component);
    this.performanceCount++;
    this.totalPerformanceCount++;
  }

  collapsePrevious() {
    this.components[this.performanceCount - 2].instance.onSubmit();
    this.components[this.performanceCount - 2].instance.changeTitle();
    this.components[this.performanceCount - 2].instance.toggleCollapsed();
    this.components[this.performanceCount - 2].instance.isLastPerformance = false;
  }

  imageUploaded() {
    const file = document.querySelector(
      'input[type=file]')['files'][0];

    const reader = new FileReader();

    reader.onload = () => {
      const base64String = reader.result;
      console.log(base64String);
      if (typeof base64String === 'string') {
        this.event.image = base64String;
      }
    };
    reader.readAsDataURL(file);
  }

  savePerformances() {
    if (this.eventForm.valid) {
      this.event.title = this.eventForm.value.title;
      this.event.type = this.eventForm.value.type;
      if (this.event.image === undefined) {
        // eslint-disable-next-line max-len
        this.event.image = 'data:image/png;base64,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';
      }
      this.allPerformancesValid = true;
      this.components.forEach((component) => {
        component.instance.onSubmit();
        // eslint-disable-next-line max-len
        this.startDates.push(new Date(component.instance.performance.startTime.substring(0, 16)));
        // eslint-disable-next-line max-len
        this.endDates.push(new Date(component.instance.performance.endTime.substring(0, 16)));
        component.instance.performance.performers.forEach(item => this.performers.add(item));
      });
      this.components.forEach((component) => {
        if (component.instance.performanceForm.invalid) {
          component.instance.expand();
          component.instance.performanceForm.markAllAsTouched();
          this.allPerformancesValid = false;
        }
      });

      this.event.performances = [];
      if (this.allPerformancesValid) {
        this.components.forEach((component) => {
          this.event.performances.push(component.instance.performance);
        });
      }
      this.event.beginDate = new Date(Math.min.apply(null, this.startDates));
      this.event.endDate = new Date(Math.max.apply(null, this.endDates));
      this.event.performers = Array.from(this.performers);
      if (this.mode === EventCreateEditMode.create) {
        this.eventsService.createEvent(this.event).subscribe((event) => {
            this.notification.success('Event created successfully');
            this.router.navigate(['/events']);
          },
          error => {
            this.notification.error(error.error, 'Event could not be created');
          });
      } else {
        this.eventsService.updateEvent(this.event, this.id).subscribe((event) => {
            this.notification.success('Event updated successfully');
            this.router.navigate(['/events']);
          },
          error => {
            this.notification.error(error.error, 'Event could not be updated');
            console.log(error);
          });
      }
    } else {
      this.eventForm.markAllAsTouched();
      this.components[this.performanceCount - 1].instance.formControl.markAllAsTouched();
      this.components[this.performanceCount - 1].instance.locationForm.markAllAsTouched();
      this.components[this.performanceCount - 1].instance.performanceForm.markAllAsTouched();
      this.startDates = [];
      this.endDates = [];
      this.performers.clear();
    }
  }

  deleteEvent() {
    if (confirm('Are you sure you want to delete this event?')) {
      this.eventsService.deleteEvent(this.id).subscribe((event) => {
          this.notification.success('Event deleted successfully');
          this.router.navigate(['/events']);
        },
        error => {
          this.notification.error(error.error, 'Event could not be deleted');
          console.log(error);
        });
    }
  }

  ngOnInit(): void {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.route.data.subscribe(data => {
      this.mode = data.mode;
      if (this.mode === 0) {
        this.titleService.setTitle('Create Event - Ticketline');
      } else {
        this.titleService.setTitle('Edit Event - Ticketline');
      }
      if (this.mode === EventCreateEditMode.edit) {
        this.heading = 'Edit Event';
        this.id = +this.route.snapshot.paramMap.get('id');
      }
    });
    if (this.mode === EventCreateEditMode.edit) {
      this.eventsService.getEventById(this.id).subscribe((event) => {
        this.event = event;
        this.eventForm.patchValue({
          title: this.event.title,
          type: this.event.type
        });

        this.event.performances.forEach((performance) => {
          const component = this.appendPerformances.createComponent(PerformanceCreateEditComponent);
          component.instance.performance = performance;
          component.instance.id = this.totalPerformanceCount;
          component.instance.parent = this;
          component.instance.setPerformersInputField();
          component.instance.performanceForm.patchValue({
            title: performance.title,
            startTime: performance.startTime.substring(0, 16),
            endTime: performance.endTime.substring(0, 16),
            price: performance.price
          });
          component.instance.performance.location = performance.location;
          component.instance.performance.seatMap = performance.seatMap;
          component.instance.setLocationInputField();
          component.instance.setSeatMapInputField();
          component.instance.heading = component.instance.performanceForm.value.title;
          this.components.push(component);
          this.performanceCount++;
          this.totalPerformanceCount++;
          component.instance.toggleCollapsed();
        });
      });
    }
  }

  ngAfterViewInit() {
    const getAllSimpleSeatMaps$ = this.seatMapService.getAllSimpleSeatMaps(0, 0);
    const getAllLocations$ = this.locationService.getAllLocations();

    if (this.mode === EventCreateEditMode.create) {
      this.addInitialPerformance();
      console.log('Added initial performance');
    }
    forkJoin([getAllSimpleSeatMaps$, getAllLocations$]).subscribe({
      next: ([seatMaps, locations]) => {
        this.seatMaps = seatMaps.seatMaps;
        this.locations = locations;
        console.log('Loaded seat maps and locations');

        if (this.mode === EventCreateEditMode.create) {
          //this.addInitialPerformance();
          this.components.forEach((component) => {
            component.instance.locations = this.locations;
            component.instance.seatMaps = this.seatMaps;
          });
          console.log('Added locations and seatmaps');
        } else if (this.mode === EventCreateEditMode.edit) {
          this.components.forEach((component) => {
            component.instance.locations = this.locations;
            component.instance.seatMaps = this.seatMaps;
          });
        }
      },
      error: (err) => {
        this.notification.error(err.error, 'Could not load seat maps and locations');
      }
    });
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }
}
