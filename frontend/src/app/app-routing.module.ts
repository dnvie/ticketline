import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {AuthGuard} from './guards/auth.guard';
import {MessageComponent} from './components/message/message.component';
import {EventsComponent} from './components/events/events.component';
import {
  EventCreateEditComponent,
  EventCreateEditMode
} from './components/events/event-create-edit/event-create-edit.component';
import {EventDetailsComponent} from './components/events/event-details/event-details.component';
import {
  PerformanceSingleEditComponent
} from './components/events/performance-single-edit/performance-single-edit.component';
import {AccountRegisterEditMode, RegistrationComponent} from './components/registration/registration.component';

import {AdminPanelComponent} from './components/admin-panel/admin-panel.component';
import {AllUserComponent, AllUsersMode} from './components/all-user/all-user.component';
import {SeatSelectionComponent} from './components/seat-selection/seat-selection.component';
import {SeatmapComponent, SeatmapCreateEditMode} from './components/seatmap/seatmap.component';
import {LocationsComponent} from './components/locations/locations.component';
import {
  LocationCreateEditComponent,
  LocationCreateEditMode
} from './components/locations/location-create-edit/location-create-edit.component';
import {SearchComponent} from './components/search/search.component';
import {EventsMode, ResultDetailsComponent} from './components/search/result-details/result-details.component';
import {CartComponent} from './components/cart/cart.component';
import {PasswordResetComponent, ResetEmailMode} from './components/login/password-reset/password-reset.component';
import {
  MessageCreateEditComponent,
  MessageCreateEditMode
} from './components/message/message-create-edit/message-create-edit.component';
import {MessageDetailsComponent} from './components/message/message-details/message-details.component';
import {OrdersComponent} from './components/orders/orders.component';
import {CheckoutComponent} from './components/cart/checkout/checkout.component';
import {SeatmapOverviewComponent} from './components/seatmap/seatmap-overview/seatmap-overview.component';
import {ReserveOrBuyMode} from './components/seatmap/sector/sector.component';
import {TopTenEventsComponent} from './components/events/top-ten-events/top-ten-events.component';

const routes: Routes = [
  {path: '', redirectTo: 'events/top10', pathMatch: 'full'},
  {path: 'login', component: LoginComponent},
  {
    path: 'events', children: [
      {path: '', component: EventsComponent},
      {path: 'create', component: EventCreateEditComponent, data: {mode: EventCreateEditMode.create}},
      {path: 'edit/:id', component: EventCreateEditComponent, data: {mode: EventCreateEditMode.edit}},
      {path: 'details/:id', component: EventDetailsComponent},
      {path: 'top10', component: TopTenEventsComponent},
      {
        path: 'search', children: [
          {path: '', component: SearchComponent},
          {path: 'artist/:name', component: ResultDetailsComponent, data: {mode: EventsMode.artist}},
          {path: 'location/:name', component: ResultDetailsComponent, data: {mode: EventsMode.location}}
        ]
      },
    ]
  },
  {path: 'performance/edit/:id', component: PerformanceSingleEditComponent},
  {path: 'news', children: [
    {path: '', component: MessageComponent},
    {path: 'create', component: MessageCreateEditComponent, data: {mode: MessageCreateEditMode.create}},
    {path: 'edit/:id', component: MessageCreateEditComponent, data: {mode: MessageCreateEditMode.edit}},
    {path: 'details/:id', component: MessageDetailsComponent}
    ]},
  {path: 'registration', component: RegistrationComponent, data: {mode: AccountRegisterEditMode.create}},
  {path: 'edit/:id', component: RegistrationComponent, data: {mode: AccountRegisterEditMode.edit}},
  {
    path: 'locations', children: [
      {path: '', component: LocationsComponent},
      {path: 'create', component: LocationCreateEditComponent, data: {mode: LocationCreateEditMode.create}},
      {path: 'edit/:id', component: LocationCreateEditComponent, data: {mode: LocationCreateEditMode.edit}},
    ]
  },
  {path: 'view/:id', component: RegistrationComponent, data: {mode: AccountRegisterEditMode.view}},
  {path: 'admin-panel', component: AdminPanelComponent},
  {path: 'users-list', component: AllUserComponent, data: {mode: AllUsersMode.all}},
  {path: 'locked-users-list', component: AllUserComponent, data: {mode: AllUsersMode.locked}},
  {
    path: 'seatmap', children: [
      {path: '', component: SeatmapOverviewComponent},
      {path: 'create', component: SeatmapComponent, data: {mode: SeatmapCreateEditMode.create}},
      {path: 'edit/:id', component: SeatmapComponent, data: {mode: SeatmapCreateEditMode.edit}},
    ]
  },
  {
    path: 'seat-selection', children: [
      {
        path: 'reserve/:performanceId/:seatmapId',
        component: SeatSelectionComponent,
        data: {mode: ReserveOrBuyMode.reserve}
      },
      {path: 'buy/:performanceId/:seatmapId', component: SeatSelectionComponent, data: {mode: ReserveOrBuyMode.buy}}
    ]
  },
  {
    path: 'cart', canActivate: [AuthGuard], children: [
      {path: '', component: CartComponent},
      {path: 'checkout', component: CheckoutComponent},
    ]
  },
  {path: 'orders/:id', canActivate: [AuthGuard], component: OrdersComponent},
  {path: 'password-reset', component: PasswordResetComponent, data: {mode: ResetEmailMode.sendResetEmail}},
  {path: 'password-reset/:token', component: PasswordResetComponent, data: {mode: ResetEmailMode.setNewPassword}}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
