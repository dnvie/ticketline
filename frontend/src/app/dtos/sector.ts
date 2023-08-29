import {Seat} from './seat';

export enum SectorType {
  'regular',
  'grandstandCurveLeft',
  'grandstandCurveRight',
  'lodge',
}

export class Sector {
  id?: string;
  type?: SectorType;
  seats?: Seat[][];
  name?: string;
  price?: number;
  length: number;
  width: number;
  orientation?: number;
  lodgeSize?: number;
  row?: number;
  column?: number;
  noUpdate?: boolean;
  standingSector?: boolean;
}

export class SectorDto {
  id?: string;
  type: SectorType;
  seats: Seat[];
  name: string;
  price: number;
  orientation: number;
  lodgeSize?: number;
  seatMapRow: number;
  seatMapColumn: number;
  noUpdate?: boolean;
  standingSector?: boolean;
  length: number;
  width: number;
}
