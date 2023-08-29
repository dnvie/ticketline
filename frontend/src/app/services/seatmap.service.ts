import {Injectable} from '@angular/core';
import {Seatmap, SeatmapDto} from '../dtos/seatmap';
import {Sector, SectorDto, SectorType} from '../dtos/sector';
import {Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Globals} from '../global/globals';
import {map} from 'rxjs/operators';
import {Seat} from '../dtos/seat';
import {SeatMapWithCount} from '../dtos/SeatMapWithCount';

@Injectable({
  providedIn: 'root'
})
export class SeatmapService {

  private baseUri = this.globals.backendUri + '/seatmaps';

  constructor(
    private httpClient: HttpClient,
    private globals: Globals,
  ) {
  }


  getSeatmapById(id: string): Observable<Seatmap> {
    return this.httpClient.get<SeatmapDto>(this.baseUri + '/' + id)
      .pipe(
        map((seatMapDto: SeatmapDto) => this.seatMapDtoToSeatMap(seatMapDto))
      );
  }

  getAllSimpleSeatMaps(page: number, size: number): Observable<SeatMapWithCount> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.httpClient.get<SeatMapWithCount>(this.baseUri, {params});
  }

  getAllSeatmaps(): Observable<Seatmap[]> {
    return this.httpClient.get<SeatmapDto[]>(this.baseUri)
      .pipe(
        map((seatMapDtos: SeatmapDto[]) => seatMapDtos.map(seatMapDto => this.seatMapDtoToSeatMap(seatMapDto)))
      );
  }

  createSeatmap(seatmap: Seatmap): Observable<Seatmap> {
    return this.httpClient.post<SeatmapDto>(this.baseUri, this.seatMapToSeatMapDto(seatmap))
      .pipe(
        map((seatMapDto: SeatmapDto) => this.seatMapDtoToSeatMap(seatMapDto))
      );
  }

  updateSeatmap(seatmap: Seatmap): Observable<Seatmap> {
    return this.httpClient.put<SeatmapDto>(this.baseUri + '/' + seatmap.id, this.seatMapToSeatMapDto(seatmap))
      .pipe(
        map((seatMapDto: SeatmapDto) => this.seatMapDtoToSeatMap(seatMapDto))
      );
  }

  deleteSeatMapById(id: string): Observable<any> {
    return this.httpClient.delete(this.baseUri + '/' + id);
  }

  deepCopySector(sector: Sector): Sector {
    const newSector: Sector = new Sector();
    newSector.id = sector.id;
    newSector.name = sector.name;
    newSector.price = sector.price;
    newSector.type = sector.type;
    newSector.orientation = sector.orientation;
    newSector.lodgeSize = sector.lodgeSize;
    newSector.row = sector.row;
    newSector.column = sector.column;
    newSector.noUpdate = sector.noUpdate;
    newSector.seats = [];
    newSector.width = sector.width;
    newSector.length = sector.length;
    newSector.row = sector.row;
    newSector.column = sector.column;
    newSector.standingSector = sector.standingSector;

    sector.seats.forEach((row, rowIndex) => {
      newSector.seats[rowIndex] = [];
      row.forEach((seat, seatIndex) => {
        let s = null;
        if (seat) {
          s = new Seat();
          s.id = seat.id;
          s.seatRow = seat.seatRow;
          s.seatColumn = seat.seatColumn;
          s.reserved = seat.reserved;
          s.number = seat.number;
          s.sector = seat.sector;
        }
        newSector.seats[rowIndex][seatIndex] = s;
      });
    });
    return newSector;
  }

  private seatMapDtoToSeatMap(seatMapDto: SeatmapDto): Seatmap {
    let length = 0;
    for (const sector of seatMapDto.sectors) {
      if (sector.seatMapRow > length) {
        length = sector.seatMapRow;
      }
    }
    length++;

    const seatMap: Seatmap = new Seatmap();
    seatMap.id = seatMapDto.id;
    seatMap.name = seatMapDto.name;
    seatMap.numberOfSectors = seatMapDto.numberOfSectors;
    seatMap.numberOfSeats = seatMapDto.numberOfSeats;
    seatMap.isUsed = seatMapDto.isUsed;

    seatMap.sectors = [];

    for (let i = 0; i < length; i++) {
      seatMap.sectors[i] = [];
    }

    for (const sector of seatMapDto.sectors) {
      seatMap.sectors[sector.seatMapRow][sector.seatMapColumn] = this.sectorDtoToSector(sector);
    }

    return seatMap;
  }

  private sectorDtoToSector(sectorDto: SectorDto): Sector {
    let length = 0;
    for (const seat of sectorDto.seats) {
      if (seat.seatRow > length) {
        length = seat.seatRow;
      }
    }
    length++;

    const sector: Sector = new Sector();
    sector.id = sectorDto.id;
    sector.name = sectorDto.name;
    sector.price = sectorDto.price;
    sector.type =
      sectorDto.type.toString() === 'REGULAR' ? SectorType.regular :
        (sectorDto.type.toString() === 'LODGE' ? SectorType.lodge :
          (sectorDto.type.toString() === 'GRANDSTANDCURVELEFT' ? SectorType.grandstandCurveLeft :
            (SectorType.grandstandCurveLeft)));
    sector.orientation = sectorDto.orientation;
    sector.lodgeSize = sectorDto.lodgeSize;
    sector.row = sectorDto.seatMapRow;
    sector.column = sectorDto.seatMapColumn;
    sector.noUpdate = sectorDto.noUpdate;
    sector.width = sectorDto.width;
    sector.length = sectorDto.length;
    sector.standingSector = sectorDto.standingSector;

    sector.seats = [];

    for (let i = 0; i < length; i++) {
      sector.seats[i] = [];
    }

    for (const seat of sectorDto.seats) {
      sector.seats[seat.seatRow][seat.seatColumn] = seat;
    }

    return sector;
  }

  private seatMapToSeatMapDto(seatMap: Seatmap): SeatmapDto {
    const seatMapDto: SeatmapDto = new SeatmapDto();
    seatMapDto.id = seatMap.id;
    seatMapDto.name = seatMap.name;

    seatMapDto.sectors = [];

    for (const row of seatMap.sectors) {
      for (const sector of row) {
        if (sector) {
          seatMapDto.sectors.push(this.sectorToSectorDto(sector));
        }
      }
    }

    return seatMapDto;
  }

  private sectorToSectorDto(sector: Sector): SectorDto {
    const sectorDto: SectorDto = new SectorDto();
    sectorDto.id = sector.id;
    sectorDto.name = sector.name;
    sectorDto.price = sector.price;
    sectorDto.type = sector.type;
    sectorDto.orientation = sector.orientation;
    sectorDto.lodgeSize = sector.lodgeSize;
    sectorDto.seatMapRow = sector.row;
    sectorDto.seatMapColumn = sector.column;
    sectorDto.noUpdate = sector.noUpdate;
    sectorDto.width = sector.width;
    sectorDto.length = sector.length;
    sectorDto.standingSector = sector.standingSector;

    sectorDto.seats = [];

    for (const row of sector.seats) {
      for (const seat of row) {
        if (seat) {
          sectorDto.seats.push(seat);
        }
      }
    }

    return sectorDto;
  }

}
