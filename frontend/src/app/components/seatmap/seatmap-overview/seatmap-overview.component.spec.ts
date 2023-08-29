import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HttpClientModule } from '@angular/common/http';
import { ToastrModule } from 'ngx-toastr';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { PaginatePipe, PaginationService } from 'ngx-pagination';

import { SeatmapOverviewComponent } from './seatmap-overview.component';

describe('SeatmapOverviewComponent', () => {
  let component: SeatmapOverviewComponent;
  let fixture: ComponentFixture<SeatmapOverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SeatmapOverviewComponent, PaginatePipe],
      imports: [HttpClientTestingModule, HttpClientModule, ToastrModule.forRoot(), MatSnackBarModule],
      providers: [PaginationService]
    })
      .compileComponents();

    fixture = TestBed.createComponent(SeatmapOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
