import {ChangeDetectorRef, Component, NgZone, OnInit, ViewChild} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {EventsService} from '../../../services/events.service';
import {LocationService} from '../../../services/location.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {HttpStatusCode} from '@angular/common/http';
import {countries} from 'countries-list';
import {Observable} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {CdkTextareaAutosize} from '@angular/cdk/text-field';
import {Location} from '../../../dtos/location';
import {Title} from '@angular/platform-browser';

export enum LocationCreateEditMode {
  create,
  edit
}

@Component({
  selector: 'app-location-create-edit',
  templateUrl: './location-create-edit.component.html',
  styleUrls: ['./location-create-edit.component.scss']
})
export class LocationCreateEditComponent implements OnInit {
  @ViewChild('autosize') autosize: CdkTextareaAutosize;
  mode: LocationCreateEditMode = LocationCreateEditMode.create;
  countryList: string[] = Object.values(countries).map(country => country.name)
    .sort((a, b) => a.localeCompare(b)); // Sort the country names alphabetically;
  filteredCountries: Observable<string[]>;
  location: Location = {
    name: '',
    country: '',
    city: '',
    postalCode: '',
    street: '',
    description: ''
  };
  inputFormControl = new FormControl('', [Validators.required]);
  countryForm = new FormControl('', [Validators.required]);
  locationForm = new FormGroup({
    name: new FormControl('', Validators.required),
    city: new FormControl('', Validators.required),
    postalCode: new FormControl('', Validators.required),
    street: new FormControl('', Validators.required),
    description: new FormControl('')
  });

  constructor(
    private changeDetector: ChangeDetectorRef,
    private eventsService: EventsService,
    private locationService: LocationService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private _ngZone: NgZone,
    private titleService: Title) {
  }

  private get modeActionFinished(): string {
    switch (this.mode) {
      case LocationCreateEditMode.create:
        return 'created';
      case LocationCreateEditMode.edit:
        return 'saved';
      default:
        return '?';
    }
  }

  private get modeActionError(): string {
    switch (this.mode) {
      case LocationCreateEditMode.create:
        return 'creating';
      case LocationCreateEditMode.edit:
        return 'saving';
      default:
        return '?';
    }
  }


  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data.mode;
      if (this.mode === 0) {
        this.titleService.setTitle('Create Location - Ticketline');
      } else {
        this.titleService.setTitle('Edit Location - Ticketline');
      }
    });
    if (this.mode !== LocationCreateEditMode.create) {
      this.route.params.subscribe(params => {
        this.locationService.getLocationById(params.id).subscribe({
          next: response => {
            this.location = response;
            this.locationForm.patchValue({
              name: this.location.name,
              city: this.location.city,
              postalCode: this.location.postalCode,
              street: this.location.street,
              description: this.location.description
            });
            this.countryForm.patchValue(this.location.country);
          },
          error: error => {
            console.error('Error fetching location', error);
            if (error.status === HttpStatusCode.NotFound) {
              this.notification.error('Error location cannot be found');
              this.router.navigate(['/locations']);
            } else {
              this.notification.error('Error fetching location: ' + error.error.errors);
            }
          }
        });
      });
    }
    this.searchCountry();
  }

  searchCountry() {
    this.filteredCountries = this.countryForm.valueChanges.pipe(
      startWith(''),
      map(value => this._filterCountries(value))
    );
  }

  deleteLocation() {
    //ask if user really wants to delete
    if (confirm('Are you sure to delete ' + this.location.name)) {
      this.locationService.deleteLocation(this.route.snapshot.params.id).subscribe({
          next: () => {
            this.notification.success(`Location ${this.location.name} successfully deleted.`);
            this.router.navigate(['/locations']);
          },
          error: error => {
            console.error('Error deleting the location', error);
            this.notification.error(`Error deleting the location: ` + error.error);
          }
        }
      );
    }
  }

  saveLocation() {
    if (this.locationForm.valid && this.countryForm.valid) {
      this.location.name = this.locationForm.value.name;
      this.location.country = this.countryForm.value;
      this.location.city = this.locationForm.value.city;
      this.location.postalCode = this.locationForm.value.postalCode;
      this.location.street = this.locationForm.value.street;
      if (this.locationForm.value.description === '') {
        delete this.locationForm.value.description;
      }
      this.location.description = this.locationForm.value.description;
      let observable: Observable<Location>;
      switch (this.mode) {
        case LocationCreateEditMode.create:
          observable = this.locationService.createLocation(this.location);
          break;
        case LocationCreateEditMode.edit:
          this.location.id = this.route.snapshot.params.id;
          observable = this.locationService.editLocation(this.location);
          break;
        default:
          console.error('Unknown LocationCreateEditMode', this.mode);
          return;
      }
      observable.subscribe({
        next: () => {
          this.notification.success(`Location ${this.location.name} successfully ${this.modeActionFinished}.`);
          this.router.navigate(['/locations']);
        },
        error: error => {
          console.error('Error creating location', error);
          let content = error.error;
          content = content.substring(content.indexOf('[') + 1, content.indexOf(']'));
          this.notification.error(`Error ${this.modeActionError} location: ` + content);
        }
      });
    } else {
      this.locationForm.markAllAsTouched();
      this.countryForm.markAsTouched();
    }
  }

  private _filterCountries(value: string): string[] {
    const filterValue = value.toLowerCase();
    return this.countryList.filter(country => country.toLowerCase().includes(filterValue));
  }


}
