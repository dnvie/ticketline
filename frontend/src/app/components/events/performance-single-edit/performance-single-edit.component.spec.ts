import { ComponentFixture, TestBed } from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ToastrModule, ToastrService} from 'ngx-toastr';
import { PerformanceSingleEditComponent } from './performance-single-edit.component';
import {RouterTestingModule} from '@angular/router/testing';
import {MatAutocomplete} from '@angular/material/autocomplete';

describe('PerformanceSingleEditComponent', () => {
  let component: PerformanceSingleEditComponent;
  let fixture: ComponentFixture<PerformanceSingleEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PerformanceSingleEditComponent, MatAutocomplete ],
      imports: [HttpClientTestingModule, ToastrModule.forRoot(), RouterTestingModule],
      providers: [ToastrService]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PerformanceSingleEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('#setLocation undefined should set the locationForm to empty', () => {
    const location = undefined;
    component.setLocation(location);
    expect(component.locationForm.value).toEqual('');
  });

  it('#setLocation should set the location', () => {
    const location = {id: 1, name: 'test', street: 'test', city: 'test', country: 'test', description: 'test', postalCode: 'test'};
    component.setLocation(location);
    expect(component.performance.location).toEqual(location);
  });

  it('#setPerformersInputField should set the performers input field', () => {
    const performers = ['test', 'test2', 'test3'];
    component.performance.performers = performers;
    component.setPerformersInputField();
    expect(component.keywords).toEqual(performers);
  });

  it('#removeKeyword should remove keyword', () => {
    component.keywords = ['test'];
    component.removeKeyword('test');
    expect(component.keywords).toEqual([]);
  });

});
