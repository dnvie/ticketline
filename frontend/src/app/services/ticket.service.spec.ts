import { TestBed } from '@angular/core/testing';

import { TicketService } from './ticket.service';
import {HttpClientModule} from '@angular/common/http';

describe('TicketService', () => {
  let service: TicketService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule]
    });
    service = TestBed.inject(TicketService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
