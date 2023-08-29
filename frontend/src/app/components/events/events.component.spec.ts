import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EventsComponent } from './events.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('EventsComponent', () => {
  let component: EventsComponent;
  let fixture: ComponentFixture<EventsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EventsComponent ],
      imports: [HttpClientTestingModule]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EventsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('#formatDate should return a formatted date', () => {
    const date = '2021-05-30T00:00';
    const formattedDate = component.formatDate(date);
    expect(formattedDate).toEqual('30.05.2021');
  });

  it('events[] should be empty', () => {
    expect(component.events).toEqual([]);
  });
});
