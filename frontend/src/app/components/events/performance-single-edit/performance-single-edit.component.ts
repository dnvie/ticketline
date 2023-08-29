import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {Location} from '../../../dtos/location';
import {Performance} from '../../../dtos/performance';
import {Observable} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {MatChipInputEvent} from '@angular/material/chips';
import {ToastrService} from 'ngx-toastr';
import {LocationService} from '../../../services/location.service';
import {PerformanceService} from 'src/app/services/performance.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Seatmap} from '../../../dtos/seatmap';
import {SeatmapService} from '../../../services/seatmap.service';
import {Title} from '@angular/platform-browser';

@Component({
  selector: 'app-performance-single-edit',
  templateUrl: './performance-single-edit.component.html',
  styleUrls: ['./performance-single-edit.component.scss']
})
export class PerformanceSingleEditComponent implements OnInit {

  id: number;
  inputFormControl = new FormControl('', [Validators.required]);
  locations: Location[];
  seatMaps: Seatmap[];
  performance: Performance = {
    title: '',
    startTime: '',
    endTime: '',
    performers: [],
    location: undefined,
    price: undefined,
    seatMap: ''
  };
  performanceForm = new FormGroup({
    title: new FormControl('', Validators.required),
    startTime: new FormControl('', Validators.required),
    endTime: new FormControl('', Validators.required),
    performers: new FormControl(''),
    price: new FormControl(null, Validators.required),
    seatMap: new FormControl('')
  });
  keywords = [];
  formControl = new FormControl(['angular']);
  locationForm = new FormControl('', Validators.required);
  filteredLocations: Observable<Location[]>;

  constructor(
    private notification: ToastrService,
    private locationService: LocationService,
    private performanceService: PerformanceService,
    private router: Router,
    private route: ActivatedRoute,
    private seatMapService: SeatmapService,
    private titleService: Title) {
    this.titleService.setTitle('Edit Performance - Ticketline');
  }

  onSubmit() {
    if (this.performanceForm.valid && this.locationForm.valid) {
      this.performance.title = this.performanceForm.value.title;
      this.performance.startTime = this.performanceForm.value.startTime;
      this.performance.endTime = this.performanceForm.value.endTime;
      if (this.performanceForm.value.startTime.length <= 16) {
        this.performance.startTime += ':00';
      }
      if (this.performanceForm.value.endTime.length <= 16) {
        this.performance.endTime += ':00';
      }
      this.performance.performers = this.keywords;
      this.performance.price = +this.performanceForm.value.price;
      this.performance.seatMap = this.performanceForm.value.seatMap;
      this.performanceService.updatePerformance(this.performance, this.id).subscribe(() => {
        this.notification.success('Successfully updated performance');
        this.router.navigate(['/events']);
      }, error => {
        this.notification.error('Performance could not be updated');
      });
    } else {
      this.performanceForm.markAllAsTouched();
      this.locationForm.markAllAsTouched();
    }
  }

  removeKeyword(keyword: string) {
    const index = this.keywords.indexOf(keyword);
    if (index >= 0) {
      this.keywords.splice(index, 1);
    }
  }

  add(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();
    if (value) {
      this.keywords.push(value);
    }
    event.chipInput?.clear();
  }

  setLocation(location: Location) {
    this.performance.location = location;
  }

  setLocationInputField() {
    this.locationForm.setValue(this.performance.location.name);
  }

  setPerformersInputField() {
    this.performance.performers.forEach(performer => {
      this.keywords.push(performer);
    });
  }

  _filterLocations(value: string): Location[] {
    const filterValue = value.toLowerCase();
    return this.locations.filter(location => location.name.toLowerCase().includes(filterValue));
  }

  deletePerformance() {
    if (confirm('Are you sure you want to delete this performance?')) {
      this.performanceService.deletePerformance(this.id).subscribe((event) => {
          this.notification.success('Performance deleted successfully');
          this.router.navigate(['/events']);
        },
        error => {
          this.notification.error('Performance could not be deleted');
          console.log(error);
        });
    }
  }

  ngOnInit(): void {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.id = +this.route.snapshot.paramMap.get('id');
    this.locationService.getAllLocations().subscribe((locations) => {
        this.locations = locations;
        this.filteredLocations = this.locationForm.valueChanges.pipe(
          startWith(''),
          map(location => (location ? this._filterLocations(location) : this.locations.slice())),
        );
      },
      error => {
        this.notification.error('Could not load locations');
      });
    this.performanceService.getPerformanceById(this.id).subscribe((performance) => {
        this.performance = performance;
        this.setLocationInputField();
        this.setPerformersInputField();
        this.performanceForm.patchValue({
          title: this.performance.title,
          startTime: this.performance.startTime,
          endTime: this.performance.endTime,
          price: this.performance.price,
          seatMap: this.performance.seatMap
        });
      },
      error => {
        this.notification.error('Could not load Performance');
      });
    this.seatMapService.getAllSimpleSeatMaps(0, 0).subscribe({
      next: res => {
        this.seatMaps = res.seatMaps;
      },
      error: err => {
        console.error(err);
      }
    });
  }
}
