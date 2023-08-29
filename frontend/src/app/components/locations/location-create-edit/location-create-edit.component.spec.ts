import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { LocationCreateEditComponent } from './location-create-edit.component';
import {HttpClientModule} from '@angular/common/http';
import {of} from 'rxjs';
import { ToastrModule } from 'ngx-toastr';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import { TextFieldModule } from '@angular/cdk/text-field';

class ActivatedRouteStub {
  readonly data = of({});
}

describe('LocationCreateEditComponent', () => {
  let component: LocationCreateEditComponent;
  let fixture: ComponentFixture<LocationCreateEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LocationCreateEditComponent ],
      imports: [HttpClientModule, ToastrModule.forRoot(), MatAutocompleteModule, TextFieldModule],
      providers: [
        { provide: ActivatedRoute, useClass: ActivatedRouteStub }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LocationCreateEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
