import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SeatmapComponent } from './seatmap.component';
import { ActivatedRoute } from '@angular/router';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {HttpClientModule} from '@angular/common/http';
import {of} from 'rxjs';
import { ToastrModule } from 'ngx-toastr';

class ActivatedRouteStub {
  readonly data = of({});
}

describe('SeatmapComponent', () => {
  let component: SeatmapComponent;
  let fixture: ComponentFixture<SeatmapComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SeatmapComponent ],
      imports: [HttpClientTestingModule, HttpClientModule, ToastrModule.forRoot()],
      providers: [
        { provide: ActivatedRoute, useClass: ActivatedRouteStub }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SeatmapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
