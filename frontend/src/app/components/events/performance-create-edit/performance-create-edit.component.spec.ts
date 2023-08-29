import { ComponentFixture, TestBed } from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';

import { PerformanceCreateEditComponent } from './performance-create-edit.component';
import {MatAutocomplete} from '@angular/material/autocomplete';

describe('PerformanceCreateEditComponent', () => {
  let component: PerformanceCreateEditComponent;
  let fixture: ComponentFixture<PerformanceCreateEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PerformanceCreateEditComponent, MatAutocomplete ],
      imports: [HttpClientTestingModule]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PerformanceCreateEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
