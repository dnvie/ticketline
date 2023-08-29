import {ChangeDetectorRef, Component, ElementRef, OnInit} from '@angular/core';
import {Sector, SectorType} from '../../dtos/sector';
import {Seatmap} from '../../dtos/seatmap';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {SeatmapService} from '../../services/seatmap.service';
import {ReserveOrBuyMode} from '../seatmap/sector/sector.component';
import {Ticket} from '../../dtos/ticket';
import {TicketService} from '../../services/ticket.service';
import {Title} from '@angular/platform-browser';
import {debounceTime, Subject, Subscription} from 'rxjs';
import {PerformanceService} from '../../services/performance.service';
import {PanZoomAPI, PanZoomConfig, PanZoomConfigOptions, PanZoomModel} from 'ngx-panzoom';
import {Rect} from 'ngx-panzoom/lib/types/rect';
import * as lodash from 'lodash';
import {AuthService} from '../../services/auth.service';
import {Seat} from '../../dtos/seat';

@Component({
  selector: 'app-seat-selection',
  templateUrl: './seat-selection.component.html',
  styleUrls: ['./seat-selection.component.scss']
})
export class SeatSelectionComponent implements OnInit {

  emptySector: Sector = {
    id: null,
    type: SectorType.regular,
    name: null,
    price: null,
    length: 1,
    width: 1,
  };

  seatmap: Seatmap;

  performanceId: number;

  reservations: Ticket[] = [];
  currentReservations: Ticket[] = [];
  shoppingCart;

  mode: ReserveOrBuyMode;

  loadSubject = new Subject();
  colorsAndPrices = [];
  canvasWidth = 1000;


  panZoomConfigOptions: PanZoomConfigOptions = {
    zoomLevels: 10,
    scalePerZoomLevel: 2.0,
    zoomStepDuration: 0.2,
    freeMouseWheelFactor: 0.01,
    zoomToFitZoomLevelFactor: 0.9,
    dragMouseButton: 'middle'
  };
  panzoomConfig: PanZoomConfig;
  scale: number;
  panzoomModel: PanZoomModel;
  initialZoomHeight: number; // set in resetZoomToFit()
  initialZoomWidth = this.canvasWidth;
  initialised = false;
  protected readonly length = length;
  private panZoomAPI: PanZoomAPI;
  private apiSubscription: Subscription;
  private modelChangedSubscription: Subscription;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private service: SeatmapService,
    private performanceService: PerformanceService,
    private ticketService: TicketService,
    private titleService: Title,
    private el: ElementRef,
    private authService: AuthService,
    private changeDetector: ChangeDetectorRef) {
    this.titleService.setTitle('Seat Selection - Ticketline');
  }

  ngOnInit(): void {
    this.panzoomConfig = new PanZoomConfig(this.panZoomConfigOptions);
    this.initialZoomHeight = this.panzoomConfig.initialZoomToFit?.height;
    this.scale = this.getCssScale(this.panzoomConfig.initialZoomLevel);
    this.changeDetector.detectChanges();
    this.apiSubscription = this.panzoomConfig.api.subscribe(
      (api: PanZoomAPI) => this.panZoomAPI = api
    );
    this.loadSubject.pipe(debounceTime(500)).subscribe(() => {
      this.loadReservations();
    });

    this.route.data.subscribe(data => {
      this.mode = data.mode;
    });

    this.route.params.subscribe(params => {
      this.performanceId = params.performanceId;
      this.service.getSeatmapById(params.seatmapId).subscribe({
        next: data => {
          this.seatmap = data;
          this.sectorColorAndPrice();
        },
        error: err => {
          console.error('Error getting seatmap', err);
          const errorMessage = err.status === 0
            ? 'Is the backend up?'
            : err.message.message;
          const toDisplay = err.error;
          this.notification.error(errorMessage, 'Could Not Get Seatmap: ' + toDisplay);
          this.router.navigate(['']);
        }
      });
    });

    this.loadReservations();
    this.modelChangedSubscription = this.panzoomConfig.modelChanged.subscribe(
      (model: PanZoomModel) => this.onModelChanged(model)
    );
    this.initialised = !!this.panzoomConfig; // && initialZoomHeight
    this.changeDetector.detectChanges();
  }

  getInitialZoomToFit(): Rect {
    const width = this.el.nativeElement.clientWidth;
    const height = this.canvasWidth * this.el.nativeElement.clientHeight / width;
    return {
      x: 0,
      y: 0,
      width,
      height,
    };
  }

  onModelChanged(model: PanZoomModel): void {
    this.panzoomModel = lodash.cloneDeep(model);
    this.scale = this.getCssScale(this.panzoomModel.zoomLevel);
    this.changeDetector.markForCheck();
    this.changeDetector.detectChanges();
  }

  initLoad() {
    this.loadSubject.next(null);
  }

  sectorColorAndPrice() {
    this.performanceService.getPerformanceById(this.performanceId).subscribe({
      next: data => {
        const performancePrice = data.price;
        this.seatmap.sectors.forEach(sectorRow => {
          sectorRow.forEach(sector => {
            if (sector !== null) {
              this.colorsAndPrices.push({color: getColor(sector), price: (sector.price * performancePrice).toFixed(2)});
            }
          });
        });
      },
      error: err => {
        const errorMessage = err.status === 0
          ? 'Is the backend up?'
          : err.message.message;
        const toDisplay = err.error;
        this.notification.error(errorMessage, 'Could Not Get Performance for pricing: ' + toDisplay);
        this.router.navigate(['']);
      }
    });

    // eslint-disable-next-line prefer-arrow/prefer-arrow-functions
    function getColor(sector: Sector) {
      const sectorPriceFactor = sector.price;
      if (sectorPriceFactor < 1.1) {
        return '#7FFFD4';
      } else if (sectorPriceFactor < 1.3) {
        return '#007bff';
      } else if (sectorPriceFactor < 1.5) {
        return '#FF4040';
      } else if (sectorPriceFactor < 1.7) {
        return '#473C8B';
      } else if (sectorPriceFactor < 2) {
        return '#D2691E';
      } else if (sectorPriceFactor < 3) {
        return '#9A32CD';
      } else if (sectorPriceFactor < 4) {
        return '#8B4513';
      } else {
        return '#FFD700';
      }
    }
  }
  getCurrentReservations(){
    for (const ticket of this.reservations) {
      if (!ticket.order && ticket.reserved && this.authService.getTokenUserId() === ticket.forUser.id
        && !this.currentReservations.includes(ticket)) {
        this.currentReservations.push(ticket);
      }
    }
    for (const ticket of this.currentReservations) {
      if (!this.reservations.includes(ticket)) {
        this.currentReservations[this.currentReservations.indexOf(ticket)] = null;
      }
    }
    console.log(this.currentReservations);
    this.currentReservations = this.currentReservations.filter(ticket => ticket !== null);
  }


  loadReservations(): void {
    this.ticketService.getTicketsByPerformance(this.performanceId).subscribe({
      next: data => {
        this.reservations = data;
        this.getCurrentReservations();
      },
      error: err => {
        console.error('Error getting reservations', err);
        const errorMessage = err.status === 0
          ? 'Is the backend up?'
          : err.message.message;
        const toDisplay = err.error;
        this.notification.error(errorMessage, 'Could Not Get Reservations: ' + toDisplay);
      }
    });
  }
  getSeatName(seat: Seat) {
    if(seat.standingSector) {
      return 'standing';
    } else {
      return seat.number;
    }
  }

  private getCssScale(zoomLevel: any): number {
    return Math.pow(this.panzoomConfig.scalePerZoomLevel, zoomLevel - this.panzoomConfig.neutralZoomLevel);
  }


}
