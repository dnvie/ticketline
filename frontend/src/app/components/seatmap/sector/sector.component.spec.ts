import { ComponentFixture, TestBed } from '@angular/core/testing';


import { SectorComponent } from './sector.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {HttpClientModule} from '@angular/common/http';
import {ToastrModule} from 'ngx-toastr';

describe('SectorComponent', () => {
  let component: SectorComponent;
  let fixture: ComponentFixture<SectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SectorComponent ],
      imports: [HttpClientTestingModule, HttpClientModule, ToastrModule.forRoot()],
    })
    .compileComponents();

    fixture = TestBed.createComponent(SectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
