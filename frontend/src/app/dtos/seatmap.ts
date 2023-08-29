import {Sector, SectorDto} from './sector';

export class Seatmap {
  id?: string;
  name?: string;
  sectors?: Sector[][];
  numberOfSectors?: number;
  numberOfSeats?: number;
  isUsed?: boolean;
}

export class SeatmapDto {
  id?: string;
  name: string;
  sectors: SectorDto[];
  numberOfSectors?: number;
  numberOfSeats?: number;
  isUsed?: boolean;
}
