import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HeaderComponent} from './components/header/header.component';
import {FooterComponent} from './components/footer/footer.component';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {MessageComponent} from './components/message/message.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {httpInterceptorProviders} from './interceptors';
import {EventsComponent} from './components/events/events.component';
import {EventCreateEditComponent} from './components/events/event-create-edit/event-create-edit.component';
import {EventDetailsComponent} from './components/events/event-details/event-details.component';
import {
  PerformanceCreateEditComponent
} from './components/events/performance-create-edit/performance-create-edit.component';
import {PerformanceDetailsComponent} from './components/events/performance-details/performance-details.component';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatChipsModule} from '@angular/material/chips';
import {MatIconModule} from '@angular/material/icon';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {ToastrModule} from 'ngx-toastr';
import {
  PerformanceSingleEditComponent
} from './components/events/performance-single-edit/performance-single-edit.component';
import {RegistrationComponent} from './components/registration/registration.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {AdminPanelComponent} from './components/admin-panel/admin-panel.component';
import {AllUserComponent} from './components/all-user/all-user.component';
import {SeatmapComponent} from './components/seatmap/seatmap.component';
import {SectorComponent} from './components/seatmap/sector/sector.component';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {SeatSelectionComponent} from './components/seat-selection/seat-selection.component';
import {SearchComponent} from './components/search/search.component';
import {ResultDetailsComponent} from './components/search/result-details/result-details.component';
import {LocationsComponent} from './components/locations/locations.component';
import {LocationCreateEditComponent} from './components/locations/location-create-edit/location-create-edit.component';
import {PasswordResetComponent} from './components/login/password-reset/password-reset.component';
import {CartComponent} from './components/cart/cart.component';
import {OrdersComponent} from './components/orders/orders.component';
import {CheckoutComponent} from './components/cart/checkout/checkout.component';
import {ConfirmationComponent} from './components/cart/checkout/confirmation/confirmation.component';
import {MatCardModule} from '@angular/material/card';
import {MessageCreateEditComponent} from './components/message/message-create-edit/message-create-edit.component';
import {MessageDetailsComponent} from './components/message/message-details/message-details.component';
import {SeatmapOverviewComponent} from './components/seatmap/seatmap-overview/seatmap-overview.component';
import {MatButtonModule} from '@angular/material/button';
import {MatPaginatorModule} from '@angular/material/paginator';
import {NgxPaginationModule} from 'ngx-pagination';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {NgxPanZoomModule} from 'ngx-panzoom';
import { TopTenEventsComponent } from './components/events/top-ten-events/top-ten-events.component';
import {MatSliderModule} from '@angular/material/slider';


@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    HomeComponent,
    LoginComponent,
    MessageComponent,
    RegistrationComponent,
    AdminPanelComponent,
    AllUserComponent,
    EventsComponent,
    EventCreateEditComponent,
    EventDetailsComponent,
    PerformanceCreateEditComponent,
    PerformanceDetailsComponent,
    PerformanceSingleEditComponent,
    SeatmapComponent,
    SectorComponent,
    SeatSelectionComponent,
    SearchComponent,
    ResultDetailsComponent,
    LocationsComponent,
    LocationCreateEditComponent,
    PasswordResetComponent,
    CartComponent,
    OrdersComponent,
    CheckoutComponent,
    ConfirmationComponent,
    MessageCreateEditComponent,
    MessageDetailsComponent,
    SeatmapOverviewComponent,
    TopTenEventsComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    NgbModule,
    FormsModule,
    BrowserAnimationsModule,
    MatInputModule,
    MatSelectModule,
    MatChipsModule,
    MatIconModule,
    MatAutocompleteModule,
    ToastrModule.forRoot(),
    DragDropModule,
    MatButtonModule,
    MatSnackBarModule,
    MatSlideToggleModule,
    NgxPaginationModule,
    NgxPanZoomModule,
    MatCardModule,
    MatPaginatorModule,
    MatSliderModule
  ],
  providers: [httpInterceptorProviders],
  bootstrap: [AppComponent]
})
export class AppModule {
}
