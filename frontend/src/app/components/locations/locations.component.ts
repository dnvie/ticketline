import {Component, OnInit, ViewChild} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {LocationService} from '../../services/location.service';
import {Location} from '../../dtos/location';
import {Title} from '@angular/platform-browser';
import {MatPaginator} from '@angular/material/paginator';

@Component({
  selector: 'app-locations',
  templateUrl: './locations.component.html',
  styleUrls: ['./locations.component.scss']
})
export class LocationsComponent implements OnInit {

  @ViewChild(MatPaginator) paginator: MatPaginator;

  locations: Location[];

  filteredLocations: any[]; // Filtered array of locations based on search
  searchText: string;

  constructor(public authService: AuthService, private locationService: LocationService, private titleService: Title) {
    this.titleService.setTitle('Locations - Ticketline');
  }

  ngOnInit() {
    this.locationService.getAllLocations().subscribe({
      next: res => {
        this.locations = res;
        this.filteredLocations = this.locations;
      },
      error: err => {
        console.log(err);
      }
    });
  }

  performSearch() {
    this.filteredLocations = this.locations.filter(location =>
      location.name.toLowerCase().includes(this.searchText.toLowerCase())
    );
  }

}
