import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { ToastrModule } from 'ngx-toastr';
import { TextFieldModule } from '@angular/cdk/text-field';

import { MessageCreateEditComponent } from './message-create-edit.component';

class ActivatedRouteStub {
  readonly data = of({});
}

describe('MessageCreateEditComponent', () => {
  let component: MessageCreateEditComponent;
  let fixture: ComponentFixture<MessageCreateEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MessageCreateEditComponent],
      imports: [HttpClientModule, ToastrModule.forRoot(), TextFieldModule],
      providers: [
        { provide: ActivatedRoute, useClass: ActivatedRouteStub }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MessageCreateEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
