import {Component, ElementRef, OnInit, ViewChild, ViewChildren} from '@angular/core';
import {Title} from '@angular/platform-browser';
import {Seatmap} from '../../../dtos/seatmap';
import {SeatmapService} from '../../../services/seatmap.service';
import {ToastrService} from 'ngx-toastr';
import {AuthService} from '../../../services/auth.service';
import {Sector, SectorType} from '../../../dtos/sector';
import {SectorComponent} from '../sector/sector.component';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {Router} from '@angular/router';
import * as lodash from 'lodash';
import {MatSnackBar} from '@angular/material/snack-bar';
import {forkJoin, Observable} from 'rxjs';

@Component({
  selector: 'app-seatmap-overview',
  templateUrl: './seatmap-overview.component.html',
  styleUrls: ['./seatmap-overview.component.scss']
})
export class SeatmapOverviewComponent implements OnInit {
  @ViewChildren('secId') sectorComponents: SectorComponent[];
  @ViewChild(MatPaginator) paginator: MatPaginator;

  seatMaps: Seatmap[];
  seatmap: Sector[][];
  seatMapToEdit: Seatmap;
  currentPage = 0;
  totalPages = 0;
  pageSize = 3;
  fetchedSeatMapInstances: Seatmap[] = [];
  toggleChecked = false;
  loaded = false;
  skeletons: any[] = Array(this.pageSize).fill({});

  emptySector: Sector = {
    id: null,
    type: SectorType.regular,
    name: null,
    price: null,
    length: 1,
    width: 1,
  };


  constructor(private titleService: Title,
              private seatmapService: SeatmapService,
              private notification: ToastrService,
              public authService: AuthService,
              private router: Router,
              private snackBar: MatSnackBar,
              private elementRef: ElementRef,
  ) {
    titleService.setTitle('Seatmap Overview - Ticketline');
  }

  ngOnInit(): void {
    this.loaded = false;
    this.loadSeatMaps();
  }

  loadSeatMaps() {
    this.fetchedSeatMapInstances = [];
    this.seatmapService.getAllSimpleSeatMaps(this.currentPage, this.pageSize).subscribe({
      next: seatMaps => {
        this.seatMaps = seatMaps.seatMaps;
        this.totalPages = seatMaps.totalCount;
        const allSeatMaps = this.seatMaps.map(seatMap => this.seatmapService.getSeatmapById(seatMap.id));
        this.fetchSeatMaps(allSeatMaps);
        this.loaded = true;
      },
      error: err => {
        console.log(err);
        this.notification.error(err.error, 'Seat Maps could not be loaded');
      }
    });
  }


  fetchSeatMaps(seatMaps: Observable<Seatmap>[]) {
    forkJoin(seatMaps).subscribe({
      next: data => {
        for (const element of data) {
          for (const item of this.seatMaps) {
            if (element.id === item.id) {
              element.numberOfSeats = item.numberOfSeats;
              element.isUsed = item.isUsed;
              element.numberOfSectors = item.numberOfSectors;
              this.fetchedSeatMapInstances.push(element);
            }
          }
        }
        if (this.toggleChecked) {
          this.filterSeatMaps();
        }
        this.applySeatMapScaleTransform();
        this.loaded = true;
      }
    });
  }

  deleteSeatMap(seatMap: Seatmap) {
    if (seatMap.isUsed) {
      this.snackBar.open('Seat map cannot be deleted because it is already in use', 'Dismiss', {
        duration: 3500,
        panelClass: 'snackbar-error'
      });
    } else {
      this.seatmapService.deleteSeatMapById(seatMap.id).subscribe({
        next: data => {
          this.notification.success('Seat map successfully deleted', 'Seat map deleted');
          this.fetchedSeatMapInstances = this.fetchedSeatMapInstances.filter(seatmap => seatmap.id !== seatMap.id);
        },
        error: err => {
          this.notification.error(err.error, 'Seat map: ' + seatMap.name + ' could not be deleted');
        }
      });
    }
  }

  filterSeatMaps() {
    this.loaded = false;
    if (!this.toggleChecked) {
      this.fetchedSeatMapInstances = [];
      this.seatmapService.getAllSimpleSeatMaps(this.currentPage, this.pageSize).subscribe({
        next: seatMaps => {
          this.seatMaps = seatMaps.seatMaps;
          const allSeatMaps = this.seatMaps.map(seatMap => this.seatmapService.getSeatmapById(seatMap.id));
          this.fetchSeatMaps(allSeatMaps);
        },
        error: err => {
          console.log(err);
          this.notification.error(err.error, 'Seat Maps could not be loaded');
        }
      });
      return;
    }
    if (this.seatMaps !== null && this.seatMaps !== undefined) {
      this.seatMaps = this.seatMaps.filter(seatmap => seatmap.isUsed === false);
      if (this.fetchedSeatMapInstances !== null && this.fetchedSeatMapInstances !== undefined) {
        this.fetchedSeatMapInstances = this.fetchedSeatMapInstances.filter(
          seatmap => this.seatMaps.find(seatmap2 => seatmap2.id === seatmap.id));
          this.loaded = true;
      }
    }
  }


  editSeatMap(seatMap: Seatmap) {
    if (seatMap.isUsed) {
      //find seatmap inside fetchedSeatMapInstances
      if (confirm('Seat map cannot be edited directly. Do you want to create a copy of it?')) {
        const seatMapToCopy = this.fetchedSeatMapInstances.find(seatMapInstance => seatMapInstance.id === seatMap.id);
        this.deepCopySeatMap(seatMapToCopy);
        this.applySeatMapScaleTransform();
      }
    } else {
      this.router.navigate(['seatmap/edit/' + seatMap.id]);
    }
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loaded = false;
    this.loadSeatMaps();
    this.applySeatMapScaleTransform();
  }

  applySeatMapScaleTransform() {
    setTimeout(() => {
      const seatMapElements = document.querySelectorAll('[id^=outerContainer]');
      console.log(seatMapElements);
      console.log('applyTransform');
      Array.from(seatMapElements).forEach((seatMap: HTMLElement) => {
        seatMap.parentElement.parentElement.parentElement.parentElement.style.visibility = 'hidden';
        console.log(seatMap);
        const currentHeight = seatMap.offsetHeight;
        const currentWidth = seatMap.offsetWidth;
        if (currentHeight > 250 || currentWidth > 250) {
          const heightFactor = 250 / currentHeight;
          const widthFactor = 250 / currentWidth;
          const scaleFactor = Math.min(heightFactor, widthFactor);
          seatMap.style.transform = `scale(${scaleFactor})`;
          const tranformedSector = seatMap.getBoundingClientRect();
          const transformedHeight = tranformedSector.height;
          const transformedWidth = tranformedSector.width;
          //set margins to align/justify sector top left
          seatMap.style.marginLeft = `-${(currentWidth - transformedWidth) / 2}px`;
          seatMap.style.marginRight = `-${(currentWidth - transformedWidth) / 2}px`;
          seatMap.style.marginTop = `-${(currentHeight + 50 - transformedHeight) / 2}px`;
          seatMap.style.marginBottom = `-${(currentHeight - transformedHeight) / 2}px`;
        }
        seatMap.parentElement.parentElement.parentElement.parentElement.style.visibility = 'visible';
      });
    }, 1);
  }

  private deepCopySeatMap(seatMap: Seatmap) {
    const copiedSeatMap = lodash.cloneDeep(seatMap);
    copiedSeatMap.id = null;
    this.seatmapService.createSeatmap(copiedSeatMap).subscribe({
      next: data => {
        this.notification.success('Seat map successfully copied', 'Seat map copied');
        this.seatMapToEdit = data;
        if (this.seatMapToEdit !== null && this.seatMapToEdit !== undefined) {
          this.router.navigate(['seatmap/edit/' + this.seatMapToEdit.id]);
        }
      },
      error: err => {
        this.notification.error(err.error, 'Seat map: ' + seatMap.name + ' could not be copied');
      }
    });
  }
}
