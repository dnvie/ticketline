import { ComponentFixture, TestBed } from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import { EventCreateEditComponent } from './event-create-edit.component';
import {ToastrService, ToastrModule} from 'ngx-toastr';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {PerformanceCreateEditComponent} from '../performance-create-edit/performance-create-edit.component';

describe('EventCreateEditComponent', () => {
  let component: EventCreateEditComponent;
  let fixture: ComponentFixture<EventCreateEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EventCreateEditComponent ],
      imports: [HttpClientTestingModule, RouterTestingModule, ToastrModule.forRoot()],
      providers: [ToastrService]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EventCreateEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('#addPerformance should add a performance', () => {
    const performance = new PerformanceCreateEditComponent();
    performance.performance = {title: '', startTime: '', endTime: '', performers: [], location: undefined, price: 100};
    component.components.push(performance);
    expect(component.components.length).toEqual(1);
  });

  it('invalid event should return false', () => {
    component.eventForm.patchValue({title: 'Test', type: undefined});
    expect(component.eventForm.valid).toEqual(false);
  });

  it('valid event should return true', () => {
    component.eventForm.patchValue({title: 'Test', type: 'FESTIVAL'});
    expect(component.eventForm.valid).toEqual(true);
  });

  it('invalid performance should return false', () => {
    const performance = new PerformanceCreateEditComponent();
    performance.performance = {title: '', startTime: '', endTime: '', performers: [], location: undefined, price: 100};
    component.components.push(performance);
    expect(component.components[0].performanceForm.valid).toEqual(false);
  });

});
