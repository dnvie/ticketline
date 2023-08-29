import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Seat} from '../../../dtos/seat';
import {Sector, SectorType} from '../../../dtos/sector';
import {AuthService} from '../../../services/auth.service';
import {ToastrService} from 'ngx-toastr';
import {Router} from '@angular/router';
import {TicketService} from '../../../services/ticket.service';
import {Ticket} from '../../../dtos/ticket';
import {PerformanceService} from '../../../services/performance.service';

export enum ReserveOrBuyMode {
  reserve,
  buy
}

@Component({
  selector: 'app-sector',
  templateUrl: './sector.component.html',
  styleUrls: ['./sector.component.scss']
})
export class SectorComponent {

  @Output() refreshReservations = new EventEmitter<void>();

  @Output() refreshSector = new EventEmitter<Sector>();

  sector: Sector;

  sectorToShow: Sector;

  _reservations: Ticket[] = [];

  _performanceId: number;

  _mode: ReserveOrBuyMode;

  _canDelete: boolean;

  changeSectorTop = this.changeSectorSizeTop;
  changeSectorBottom = this.changeSectorSizeBottom;
  changeSectorLeft = this.changeSectorSizeHor;
  changeSectorRight = this.changeSectorSizeHor;

  constructor(
    private authService: AuthService,
    private notification: ToastrService,
    private ticketService: TicketService,
    private router: Router,
    private performanceService: PerformanceService,
  ) {
  }

  @Input() set sectorToBeCreated(sector: Sector) {
    this.sector = sector;
    this.sectorToShow = new Sector();
    if (!this.sector.seats) {
      this.loadSector();
    }
    this.performRotation();
    this.changeFunctions();
  }

  @Input() set performanceId(id: number) {
    this._performanceId = id;
    this.loadReservations();
  }

  @Input() set mode(mode: ReserveOrBuyMode) {
    this._mode = mode;
  }

  @Input() set reservations(reservations: Ticket[]) {
    this._reservations = reservations;
  }

  @Input() set canDelete(canDelete: boolean) {
    this._canDelete = canDelete;
  }

  getSeatColour(seat: Seat): string {
    const sectorPriceFactor = +this.sector.price;
    if (this._mode === undefined || this._mode === null || this._performanceId === undefined || this._performanceId === null) {
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
    if (this.isReserved(seat)) {
      if (this.authService.isLoggedIn() && this.isReservedByUser(seat)) {
        return '#90EE90';
      } else {
        return '#888888';
      }
    } else {
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

  isReserved(seat: Seat): boolean {
    return this._reservations.some(t => t.seat && t.seat.id === seat.id);
  }

  isReservedByUser(seat: Seat): boolean {
    return this._reservations.some(t => t.seat && t.seat.id === seat.id && t.reserved && !t.order)
      && this.authService.getTokenUserId()
      === this._reservations.find(t => t.seat && t.seat.id === seat.id && t.reserved && !t.order).forUser.id;
  }

  loadReservations(): void {
    this.refreshReservations.emit();
  }

  loadSector(): void {
    this.sector.seats = [];

    if (this.sector.type === SectorType.regular) {
      this.generateRegularSector();
    } else if (this.sector.type === SectorType.grandstandCurveLeft) {
      this.generateGrandstandCurveLeftSector();
    } else if (this.sector.type === SectorType.grandstandCurveRight) {
      this.generateGrandstandCurveRightSector();
    } else if (this.sector.type === SectorType.lodge) {
      this.generateLodgeSector(this.sector.lodgeSize);
    }
  }

  generateRegularSector(): void {
    for (let i = 0; i < this.sector.length; i++
    ) {
      this.sector.seats[i] = [];
      for (let j = 0; j < this.sector.width; j++) {
        this.addSeat(i * this.sector.width + j + 1, i, j);
      }
    }
  }

  generateGrandstandCurveLeftSector(): void {
    let base = this.sector.width;
    let number = 1;
    for (let i = 0; i < this.sector.length; i++) {
      this.sector.seats[i] = [];
      for (let j = 0; j < base; j++) {
        this.addSeat(number, i, j);
        number++;
      }
      base++;
    }
  }

  generateGrandstandCurveRightSector(): void {
    let base = this.sector.width;
    let offset = this.sector.length - 1;
    let number = 1;
    for (let i = 0; i < this.sector.length; i++) {
      this.sector.seats[i] = [];
      for (let j = offset; j < offset + base; j++) {
        this.addSeat(number, i, j);
        number++;
      }
      base++;
      offset--;
    }
  }

  generateLodgeSector(lodgeSize: number): void {
    let number = 1;
    if (this.sector.width % (lodgeSize + 1) !== 0) {
      this.sector.width = this.sector.width + (lodgeSize + 1) - (this.sector.width % (lodgeSize + 1));
    }
    for (let i = 0; i < this.sector.length; i++) {
      this.sector.seats[i] = [];
      for (let j = 0; j < this.sector.width; j++) {
        if (j % (lodgeSize + 1) !== 0) {
          this.addSeat(number, i, j);
          number++;
        }
      }
    }
  }

  rotateSector(): void {
    if (!this.sector.orientation) {
      this.sector.orientation = 0;
    } else if (this.sector.orientation === 270) {
      this.sector.orientation = 0;
      this.performRotation();
      this.changeFunctions();
      return;
    }
    this.sector.orientation += 90;
    this.performRotation();
    this.changeFunctions();
  }

  performRotation(): void {
    if (!this.sector.orientation) {
      this.sector.orientation = 0;
    }
    if (this.sector.orientation === 90) {
      this.sectorToShow.seats =
        this.perform90DegreeRotation(
          this.sector.seats
        );
    } else if (this.sector.orientation === 180) {
      this.sectorToShow.seats =
        this.perform90DegreeRotation(
          this.perform90DegreeRotation(
            this.sector.seats
          )
        );
    } else if (this.sector.orientation === 270) {
      this.sectorToShow.seats =
        this.perform90DegreeRotation(
          this.perform90DegreeRotation(
            this.perform90DegreeRotation(
              this.sector.seats
            )
          )
        );
    } else {
      this.sectorToShow.seats = this.sector.seats;
    }
  }

  perform90DegreeRotation(seats: Seat[][]): Seat[][] {

    let length = 0;
    for (const row of seats) {
      if (row.length > length) {
        length = row.length;
      }
    }

    const rotated = [];
    for (let i = 0; i < length; i++) {
      rotated[i] = [];
    }

    for (let i = 0; i < seats.length; i++) {
      for (let j = 0; j < length; j++) {
        rotated[j][seats.length - 1 - i] = seats[i][j];
      }
    }
    return rotated;
  }

  changeFunctions(): void {
    if (this.sector.orientation === 90) {
      this.changeSectorTop = this.changeSectorSizeHor;
      this.changeSectorLeft = this.changeSectorSizeBottom;
      this.changeSectorBottom = this.changeSectorSizeHor;
      this.changeSectorRight = this.changeSectorSizeTop;
    } else if (this.sector.orientation === 180) {
      this.changeSectorTop = this.changeSectorSizeBottom;
      this.changeSectorBottom = this.changeSectorSizeTop;
      this.changeSectorLeft = this.changeSectorSizeHor;
      this.changeSectorRight = this.changeSectorSizeHor;
    } else if (this.sector.orientation === 270) {
      this.changeSectorTop = this.changeSectorSizeHor;
      this.changeSectorRight = this.changeSectorSizeBottom;
      this.changeSectorBottom = this.changeSectorSizeHor;
      this.changeSectorLeft = this.changeSectorSizeTop;
    } else {
      this.changeSectorTop = this.changeSectorSizeTop;
      this.changeSectorBottom = this.changeSectorSizeBottom;
      this.changeSectorLeft = this.changeSectorSizeHor;
      this.changeSectorRight = this.changeSectorSizeHor;
    }
  }

  changeSectorSizeTop(rows: number): void {
    if (rows === 0) {
      return;
    }
    if (rows > 0) {
      this.growSectorTop(rows);
    }
    if (rows < 0) {
      this.shrinkSectorTop(-rows);
    }
    this.enumerateSeats();
    this.performRotation();
  }

  changeSectorSizeBottom(rows: number): void {
    if (rows === 0) {
      return;
    }
    if (rows > 0) {
      this.growSectorBottom(rows);
    }
    if (rows < 0) {
      this.shrinkSectorBottom(-rows);
    }
    this.enumerateSeats();
    this.performRotation();
  }

  changeSectorSizeHor(cols: number): void {
    if (cols === 0) {
      return;
    }
    if (cols > 0) {
      this.growSectorHor(cols);
    }
    if (cols < 0) {
      this.shrinkSectorHor(-cols);
    }
    this.enumerateSeats();
    this.performRotation();
  }

  enumerateSeats(): void {
    let number = 1;
    for (let i = 0; i < this.sector.seats.length; i++) {
      for (let j = 0; j < this.sector.seats[i].length; j++) {
        if (this.sector.seats[i][j]) {
          this.sector.seats[i][j].number = number;
          this.sector.seats[i][j].seatRow = i;
          this.sector.seats[i][j].seatColumn = j;
          number++;
        }
      }
    }
  }

  addSeat(number: number, row: number, col: number): void {
    const seat: Seat = new Seat();
    seat.seatRow = row;
    seat.seatColumn = col;
    seat.sector = this.sector.name;
    seat.number = number;
    this.sector.seats[row][col] = seat;
  }

  removeSeat(row: number, col: number): void {
    this.sector.seats[row][col] = null;
  }

  growSectorTop(rows: number): void {
    if (this.sector.type === SectorType.regular) {
      for (let i = 0; i < rows; i++) {
        this.sector.seats.unshift([]);
        for (let j = 0; j < this.sector.width; j++) {
          this.addSeat(i * this.sector.width + j + 1, 0, j);
        }
      }
    } else if (this.sector.type === SectorType.grandstandCurveLeft) {
      if (this.sector.width === 1) {
        return;
      }
      let base = this.sector.width - 1;
      for (let i = 0; i < rows && base > 0; i++) {
        this.sector.seats.unshift([]);
        for (let j = 0; j < base; j++) {
          this.addSeat(i * this.sector.width + j + 1, 0, j);
        }
        base--;
      }
      this.sector.width = base + 1;
    } else if (this.sector.type === SectorType.grandstandCurveRight) {
      if (this.sector.width === 1) {
        return;
      }
      let base = this.sector.width - 1;
      let offset = this.sector.length;
      for (let i = 0; i < rows; i++) {
        this.sector.seats.unshift([]);
        for (let j = offset; j < offset + base; j++) {
          this.addSeat(i * this.sector.width + j + 1, 0, j);
        }
        base--;
        offset++;
      }
      this.sector.width = base + 1;
    } else if (this.sector.type === SectorType.lodge) {
      for (let i = 0; i < rows; i++) {
        this.sector.seats.unshift([]);
        for (let j = 0; j < this.sector.width; j++) {
          if (j % (this.sector.lodgeSize + 1) !== 0) {
            this.addSeat(i * this.sector.width + j + 1, 0, j);
          }
        }
      }
    }
    this.sector.length += rows;
  }

  shrinkSectorTop(rows: number): void {
    if (this.sector.seats.length <= rows) {
      return;
    }
    for (let i = 0; i < rows; i++) {
      this.sector.seats.shift();
    }
    this.sector.length -= rows;
    if (this.sector.type === SectorType.grandstandCurveLeft || this.sector.type === SectorType.grandstandCurveRight) {
      this.sector.width += rows;
    }
  }

  growSectorBottom(rows: number): void {
    if (this.sector.type === SectorType.regular) {
      for (let i = 0; i < rows; i++) {
        this.sector.seats.push([]);
        for (let j = 0; j < this.sector.width; j++) {
          this.addSeat(this.sector.length * this.sector.width + j + 1, this.sector.length + i, j);
        }
      }
    } else if (this.sector.type === SectorType.grandstandCurveLeft) {
      let last = this.sector.width + this.sector.length;
      for (let i = 0; i < rows; i++) {
        this.sector.seats.push([]);
        for (let j = 0; j < last; j++) {
          this.addSeat(this.sector.length * this.sector.width + j + 1, this.sector.length + i, j);
        }
        last++;
      }
    } else if (this.sector.type === SectorType.grandstandCurveRight) {
      for (const row of this.sector.seats) {
        for (let i = 0; i < rows; i++) {
          row.unshift(null);
        }
      }
      let last = this.sector.width + this.sector.length;
      let offset = rows - 1;
      for (let i = 0; i < rows; i++) {
        this.sector.seats.push([]);
        for (let j = offset; j < offset + last; j++) {
          this.addSeat(this.sector.length * this.sector.width + j + 1, this.sector.length + i, j);
        }
        offset--;
        last++;
      }
    } else if (this.sector.type === SectorType.lodge) {
      for (let i = 0; i < rows; i++) {
        this.sector.seats.push([]);
        for (let j = 0; j < this.sector.width; j++) {
          if (j % (this.sector.lodgeSize + 1) !== 0) {
            this.addSeat(this.sector.length * this.sector.width + j + 1, this.sector.length + i, j);
          }
        }
      }
    }
    this.sector.length += rows;
  }

  shrinkSectorBottom(rows: number): void {
    if (this.sector.seats.length <= rows) {
      return;
    }
    for (let i = 0; i < rows; i++) {
      this.sector.seats.pop();
    }
    this.sector.length -= rows;
    if (this.sector.type === SectorType.grandstandCurveRight) {
      for (const row of this.sector.seats) {
        for (let i = 0; i < rows; i++) {
          row.shift();
        }
      }
    }
  }

  growSectorHor(cols: number): void {
    if (this.sector.type !== SectorType.lodge) {
      for (const row of this.sector.seats) {
        for (let i = 0; i < cols; i++) {
          const seat = new Seat();
          seat.sector = this.sector.name;
          if (this.sector.type === SectorType.grandstandCurveRight) {
            row.push(seat);
          } else {
            row.unshift(seat);
          }
        }
      }
      this.sector.width += cols;
    } else {
      for (const row of this.sector.seats) {
        for (let i = this.sector.width; i < this.sector.width + cols * (this.sector.lodgeSize + 1); i++) {
          if (i % (this.sector.lodgeSize + 1) !== 0) {
            const seat = new Seat();
            seat.sector = this.sector.name;
            row[i] = seat;
          }
        }
      }
      this.sector.width += cols * (this.sector.lodgeSize + 1);
    }
  }

  shrinkSectorHor(cols: number): void {
    if (this.sector.type !== SectorType.lodge) {
      if (this.sector.width <= cols) {
        return;
      }
      for (const row of this.sector.seats) {
        for (let i = 0; i < cols; i++) {
          if (this.sector.type === SectorType.grandstandCurveLeft) {
            row.shift();
          } else {
            row.pop();
          }
        }
      }
      this.sector.width -= cols;
    } else {
      if (this.sector.width <= cols * (this.sector.lodgeSize + 1)) {
        cols = Math.floor(this.sector.width / (this.sector.lodgeSize + 1)) - 1;
      }
      for (const row of this.sector.seats) {
        for (let i = 0; i < cols * (this.sector.lodgeSize + 1); i++) {
          row.pop();
        }
      }
      this.sector.width -= cols * (this.sector.lodgeSize + 1);
    }
  }

  reserveOrBuySeat(seat: Seat): void {
    if (this._mode === undefined || this._mode === null || this._performanceId === undefined || this._performanceId === null) {
      return;
    }
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    if (this.isReserved(seat) && !this.isReservedByUser(seat)) {
      return;
    }

    this.loadReservations();
    if (this._mode === ReserveOrBuyMode.reserve) {
      this.reserveOrUnreserveSeat(seat);
    } else {
      this.buyOrUnbuySeat(seat);
    }
  }

  reserveOrUnreserveSeat(seat: Seat): void {
    if (this.isReservedByUser(seat)) {
      const reservation = this._reservations.find(t => t.seat && t.seat.id === seat.id);
      this.ticketService.deleteTicket(reservation.id).subscribe(() => {
        this._reservations = this._reservations.filter(t => t.seat && t.seat.id !== seat.id);
      });
    } else {
      this.ticketService.reserveTicket(this._performanceId, seat.id).subscribe(t => {
        this._reservations.push(t);
      });
    }
    this.loadReservations();
  }

  buyOrUnbuySeat(seat: Seat): void {

    if (this.isReservedByUser(seat)) {
      const reservation = this._reservations.find(t => t.seat && t.seat.id === seat.id);
      this.ticketService.deleteTicket(reservation.id).subscribe(() => {
        this._reservations = this._reservations.filter(t => t.seat && t.seat.id !== seat.id);
      });
    } else {
      this.ticketService.reserveTicket(this._performanceId, seat.id).subscribe(
        {
          next: t => {
            if (t) {
              this.ticketService.addTicketToCart(t);
              this._reservations.push(t);
            }
          },
          error: err => {
            this.notification.error('Seat is already reserved or bought.');
            this.loadReservations();
          }
        });
    }
    this.loadReservations();
  }

  deleteSeat(seat: Seat): void {
    if (!this._canDelete) {
      return;
    }
    this.sector.seats.forEach((row, rowIndex) => {
      row.forEach((s, colIndex) => {
        if (s === seat) {
          this.sector.seats[rowIndex][colIndex] = null;
        }
      });
    });
    this.performRotation();
  }

  saveDeletions(): void {
    if (!this._canDelete) {
      return;
    }
    this.refreshSector.emit(this.sector);
  }
}
