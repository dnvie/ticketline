import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { SeatmapService } from './seatmap.service';

describe('SeatmapService', () => {
  let service: SeatmapService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(SeatmapService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
