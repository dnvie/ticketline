import {Component, DoCheck, IterableDiffers, OnInit, ViewChild, ViewContainerRef} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {MatChipInputEvent} from '@angular/material/chips';
import {Observable} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {Location} from 'src/app/dtos/location';
import {Performance} from 'src/app/dtos/performance';
import {EventCreateEditComponent} from '../event-create-edit/event-create-edit.component';
import {Seatmap} from '../../../dtos/seatmap';

@Component({
  selector: 'app-performance-create-edit',
  templateUrl: './performance-create-edit.component.html',
  styleUrls: ['./performance-create-edit.component.scss']
})
export class PerformanceCreateEditComponent implements OnInit, DoCheck {

  @ViewChild('appendPerformances', {read: ViewContainerRef}) appendPerformances: ViewContainerRef;

  parent: EventCreateEditComponent;
  mode = 0;
  iterableDiffer: any;
  id: number;
  inputFormControl = new FormControl('', [Validators.required]);
  collapsed = false;
  isLastPerformance = true;
  isDeleted = false;
  heading = 'Add Performance';
  locations: Location[] = [new Location()];
  seatMaps: Seatmap[] = [new Seatmap()];
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
    price: new FormControl(null, Validators.required)
  });
  keywords = [];
  formControl = new FormControl([], Validators.minLength(1));

  locationForm = new FormControl('', Validators.required);
  seatMapForm = new FormControl('');
  filteredLocations: Observable<Location[]>;

  constructor(private iterableDiffers: IterableDiffers) {
    this.iterableDiffer = iterableDiffers.find([]).create(null);
    this.filteredLocations = this.locationForm.valueChanges.pipe(
      startWith(''),
      map(location => (location ? this._filterLocations(location) : this.locations.slice())),
    );
  }

  toggleCollapsed() {
    this.collapsed = !this.collapsed;
  }

  expand() {
    this.collapsed = false;
  }

  ngDoCheck(): void {
    const changes = this.iterableDiffer.diff(this.locations);
    if (changes) {
      this.filteredLocations = this.locationForm.valueChanges.pipe(
        startWith(''),
        map(location => (location ? this._filterLocations(location) : this.locations.slice())),
      );
    }

  }

  changeTitle() {
    document.getElementById(`title${this.id}`).innerText = `Performance #${this.id}`;
    document.getElementById(`title${this.id}`).style.color = 'darkgrey';
  }

  onSubmit() {
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

  delete() {
    if (confirm('Are you sure you want to delete this performance?')) {
      this.isDeleted = true;
      for (let i = this.parent.components.length - 1; i >= 0; --i) {
        if (this.parent.components[i].instance.id === this.id) {
          this.parent.components.splice(i, 1);
          break;
        }
      }
      this.parent.performanceCount--;
    }
  }

  setLocation(location: Location) {
    this.performance.location = location;
  }

  setSeatMap(seatMap: string) {
    this.performance.seatMap = seatMap;
  }

  setLocationInputField() {
    this.locationForm.setValue(this.performance.location.name);
  }

  setSeatMapInputField() {
    this.seatMapForm.setValue(this.performance.seatMap);
  }

  setPerformersInputField() {
    this.performance.performers.forEach(performer => {
      this.keywords.push(performer);
    });
  }

  ngOnInit(): void {
    if (this.parent !== undefined) {
      this.mode = this.parent.mode;
    }
  }

  private _filterLocations(value: string): Location[] {
    const filterValue = value.toLowerCase();
    return this.locations.filter(location => location.name.toLowerCase().includes(filterValue));
  }
}
