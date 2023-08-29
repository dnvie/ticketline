import {Component, OnInit} from '@angular/core';
import {NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
import {ToastrService} from 'ngx-toastr';
import {User} from '../../dtos/user';
import {RegistrationService} from '../../services/registration.service';
import {AuthService} from '../../services/auth.service';
import {Title} from '@angular/platform-browser';

export enum AccountRegisterEditMode {
  create,
  edit,
  view
};

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent implements OnInit {

  mode: AccountRegisterEditMode = AccountRegisterEditMode.create;
  user: User = {
    firstName: '',
    lastName: '',
    phoneNumber: '',
    email: '',
    password: '',
    admin: false,
  };
  passwordRepeat = '';
  passwordChanged = false;

  constructor(
    private service: RegistrationService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private location: Location,
    private authService: AuthService,
    private titleService: Title) {
}

  public get heading(): string {
    switch (this.mode) {
      case AccountRegisterEditMode.create:
        return 'Create New Account';
      case AccountRegisterEditMode.edit:
        return 'Edit Account';
      case AccountRegisterEditMode.view:
        return 'View Account';
      default:
        return '?';
    }
  }

  get isAdmin(): boolean {
    return this.authService.getUserRole() === 'ADMIN';
  }

  public get submitButtonText(): string {
    switch (this.mode) {
      case AccountRegisterEditMode.create:
        return 'Register';
      case AccountRegisterEditMode.edit:
        return 'Save';
      default:
        return '?';
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === AccountRegisterEditMode.create;
  }

  get passwordText(): string {
    return this.modeIsCreate ? 'Password' : 'New Password';
  }

  get modeIsEditAndAdminAndNotOwnAccount(): boolean {
    return this.mode === AccountRegisterEditMode.edit && this.isAdmin && this.authService.getTokenUserId() !== this.user.id;
  }

  private get modeActionFinished(): string {
    switch (this.mode) {
      case AccountRegisterEditMode.create:
        return 'registered';
      case AccountRegisterEditMode.edit:
        return 'saved';
      default:
        return '?';
    }
  }

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data.mode;
      if (this.mode === 0) {
        this.titleService.setTitle('Create Account - Ticketline');
      } else if (this.mode === 1) {
        this.titleService.setTitle('Edit Account - Ticketline');
      } else if (this.mode === 2 ) {
        this.titleService.setTitle('View Account - Ticketline');
      }
    });

    if (!this.modeIsCreate) {
      const id = this.route.snapshot.paramMap.get('id');
      if (!this.authService.isLoggedIn()) {
        this.notification.error('You must be logged in to edit your account.', 'Not Logged In');
        this.router.navigate(['/login']);
      }
      this.route.params.subscribe(params => {
        this.service.getUser(id).subscribe({
          next: data => {
            this.user = data;
            this.user.password = '';
          },
          error: err => {
            console.error('Error getting user', err);
            const errorMessage = err.status === 0
              ? 'Is the backend up?'
              : err.message.message;
            const toDisplay = err.error;
            /*  for (const e of err.error.errors) {
               toDisplay += ' ' + e;
             } */
            this.notification.error(errorMessage, 'Could Not Get User: ' + toDisplay);
            this.router.navigate(['/message']);
          }
        });
      });
    }
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public onSubmit(form: NgForm): Promise<void> {
    console.log('is form valid?', form.valid, this.user);
    if (this.passwordChanged && this.user.password !== this.passwordRepeat) {
      this.notification.error('Passwords do not match.', 'Could Not Register Account');
      return;
    }
    if (form.valid) {
      if (!this.user.admin) {
        delete this.user.admin;
      }
      if (!this.user.enabled) {
        delete this.user.enabled;
      }
      if (!this.user.id) {
        delete this.user.id;
      }
      let observable;
      switch (this.mode) {
        case AccountRegisterEditMode.create:
          observable = this.service.register(this.user);
          break;
        case AccountRegisterEditMode.edit:
          if (!this.user.password) {
            delete this.user.password;
          }
          observable = this.service.update(this.user);
          break;
        default:
          console.error('Unknown AccountRegisterEditMode', this.mode);
          return;
      }
      observable.subscribe({
        next: data => {
          console.log('User created', data);
          this.notification.success(`User ${this.user.email} successfully ${this.modeActionFinished}.`);
          this.location.back();
        },
        error: error => {
          console.error(`Error ${this.modeIsCreate ? 'creating' : 'updating'} account`, error);
          const errorMessage = error.status === 0
            ? 'Is the backend up?'
            : error.message.message;
          this.notification.error(errorMessage, `Could Not ${this.modeIsCreate ? 'create' : 'update'} Account: ` + error.error);
        }
      });
    }
  }


  deleteAccount(): void {
    console.log('Delete account', this.user);
    if (this.user) {
      const id = this.route.snapshot.paramMap.get('id');
      this.service.delete(id)
        .subscribe({
          error: err => {
            console.error('Error deleting account', err);
            const errorMessage = err.status === 0
              ? 'Is the backend up?'
              : err.message.message;
            let toDisplay = '';
            for (const e of err.error.errors) {
              toDisplay += ' ' + e;
            }
            this.notification.error(errorMessage, 'Could Not Delete Account: ' + toDisplay);
          },
          complete: () => {
            this.notification.success(`User ${this.user.email} successfully deleted.`);
            if (this.isAdmin && this.authService.getTokenUserId() !== this.user.id) {
              this.location.back();
            } else {
              this.authService.logoutUser();
              this.router.navigate(['']);
            }
          }
        });
    }
    return;
  }

  enableAccount(): void {
    console.log('toggle account', this.user);

    // toggle user enabled
    if (this.user.enabled === undefined || this.user.enabled === null) {
      this.user.enabled = false;
    } else {
      this.user.enabled = !this.user.enabled;
    }

    delete this.user.password;

    this.service.update(this.user)
      .subscribe({
        error: err => {
          console.error('Error deleting account', err);
          const errorMessage = err.status === 0
            ? 'Is the backend up?'
            : err.message.message;
          let toDisplay = '';
          for (const e of err.error.errors) {
            toDisplay += ' ' + e;
          }
          this.notification.error(errorMessage, 'Could Not Enable Account: ' + toDisplay);
        },
        complete: () => {
          this.notification.success(`User ${this.user.email} successfully ${this.user.enabled ? 'enabled' : 'disabled'}.`);
        }
      });

    return;
  }

  resetPassword(): void {
    console.log('Reset password', this.user);
    if (this.user) {
      this.service.sendResetEmail(this.user.email)
        .subscribe({
          error: err => {
            console.error('Error resetting password', err);
            const errorMessage = err.status === 0
              ? 'Is the backend up?'
              : err.message.message;
            let toDisplay = '';
            for (const e of err.error.errors) {
              toDisplay += ' ' + e;
            }
            this.notification.error(errorMessage, 'Could Not Reset Password: ' + toDisplay);
          },
          complete: () => {
            this.notification.success(`Password reset email sent to ${this.user.email}.`);
          }
        });
    }
    return;
  }

}

