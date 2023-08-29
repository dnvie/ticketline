import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { User } from 'src/app/dtos/user';
import { RegistrationService } from 'src/app/services/registration.service';
import {Title} from '@angular/platform-browser';
import { SearchParams } from 'src/app/dtos/search';
import { Subject, debounceTime } from 'rxjs';

export enum AllUsersMode {
  all,
  locked
};


@Component({
  selector: 'app-all-user',
  templateUrl: './all-user.component.html',
  styleUrls: ['./all-user.component.scss']
})
export class AllUserComponent implements OnInit {

users: User[] = [];
mode: AllUsersMode = AllUsersMode.all;
search = '';
changeObserver = new Subject<any>();
constructor( private service: RegistrationService,private route: ActivatedRoute,  private notification: ToastrService,
             private titleService: Title) {}

public get heading(): string {
  switch (this.mode) {
    case AllUsersMode.all:
      return 'All Users';
    case AllUsersMode.locked:
      return 'Locked Users';
    default:
      return '?';
  }
}

ngOnInit(): void {

  this.route.data.subscribe(data => {
    this.mode = data.mode;
    if (this.mode === AllUsersMode.all) {
      this.titleService.setTitle('All Users - Ticketline');
    } else if (this.mode === AllUsersMode.locked) {
      this.titleService.setTitle('Locked Users - Ticketline');
    }
  });
  this.service.getAllUsers(this.mode)
  .subscribe({
    next: data => {
      this.users = data;
    },
    error: error => {
      this.users = [];

      const errorMessage = error.status === 0
        ? 'Could not fetch users. Try again later'
        : error.message.message;
      console.log(errorMessage);
    }
  });

  this.changeObserver.pipe(
    debounceTime(500),).subscribe({
    next:this.onSearch.bind(this),
  });

}

  enableAccount(event: Event, id: string): void {
    event.stopPropagation();
    console.log('toggle account', id);
    const user = this.users.find(u => u.id === id);

    // toggle user enabled
    if(user.enabled === undefined || user.enabled === null){
      user.enabled = false;
    } else{
      user.enabled = !user.enabled;
    }

    delete user.password;

    this.service.update(user)
      .subscribe({
        error: err => {
          console.error('Error updating account', err);
          const errorMessage = err.status === 0
            ? 'Is the backend up?'
            : err.message.message;
          let toDisplay = '';
          for (const e of err.error.errors) {
            toDisplay += ' ' + e;
          }
          this.notification.error(errorMessage, 'Could not update account: ' + toDisplay);
        },
        complete: () => {
          this.notification.success(`User ${user.email} successfully ${user.enabled? 'unlocked':'locked'}.`);
          // if the mode is locked, remove the user from the list
          if(this.mode === AllUsersMode.locked){
            this.users = this.users.filter(u => u.email !== user.email);
          }
        }
      });
  }

  onSearchDebounced(){
    this.changeObserver.next('');
  }
  onSearch() {
    //if search is empty, show all users
    if(this.search === ''){
      this.service.getAllUsers(this.mode)
      .subscribe({
        next: data => {
          this.users = data;
        }
      });
      return;
    }

    this.service.searchUsersWithUsernameAndLockedStatus(this.search, this.mode === AllUsersMode.locked)
      .subscribe({
        next: data => {
          this.users = data;
        }
      });
    }

}
