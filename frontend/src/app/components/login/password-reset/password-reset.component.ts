import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import {
  UntypedFormBuilder,
  UntypedFormGroup,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

import { RegistrationService } from 'src/app/services/registration.service';
import {Title} from '@angular/platform-browser';

export enum ResetEmailMode {
  sendResetEmail,
  setNewPassword,
}

@Component({
  selector: 'app-password-reset',
  templateUrl: './password-reset.component.html',
  styleUrls: ['./password-reset.component.scss'],
})
export class PasswordResetComponent implements OnInit {
  loginForm: UntypedFormGroup;
  // After first submission attempt, form validation will start
  submitted = false;
  // Error flag
  error = false;
  errorMessage = '';

  mode: ResetEmailMode = ResetEmailMode.sendResetEmail;

  token = '';

  email = '';
  password = '';
  passwordRepeat = '';

  constructor(
    private formBuilder: UntypedFormBuilder,
    private regristrationServire: RegistrationService,
    private router: Router,
    private notification: ToastrService,
    private route: ActivatedRoute,
    private titleService: Title
  ) {
    this.loginForm = this.formBuilder.group({
      username: ['', [Validators.required]],
    });
    this.titleService.setTitle('Reset Password - Ticketline');
  }

  ngOnInit(): void {
    this.route.data.subscribe((data) => {
      this.mode = data.mode;
    });

    if (!this.modeIsSendResetEmail()) {
      this.token = this.route.snapshot.paramMap.get('token');
      console.log(this.token);
    }
  }

  modeIsSendResetEmail(): boolean {
    return this.mode === ResetEmailMode.sendResetEmail;
  }

  resetPassword(form: NgForm) {
    this.submitted = true;
    this.sendResetEmail(form);
    return;
  }

  sendResetEmail(form: NgForm) {
    if (this.modeIsSendResetEmail()) {
      this.regristrationServire.sendResetEmail(this.email).subscribe({
        next: () => {
          this.notification.success(
            'Successfully sent reset email to: ' + this.email
          );
          this.router.navigate(['/login']);
        },
        error: (error) => {
         console.log('error', error);
          this.error = true;
          if (typeof error.error === 'object') {
            this.errorMessage = error.error.error;
          } else {
            this.errorMessage = error.error;
          }
        },
      });
    } else {
      // check if passwords match
      if (this.password !== this.passwordRepeat) {
        this.notification.error('Passwords do not match');
        return;
      }
      this.regristrationServire
        .setNewPassword(this.email, this.password, this.token)
        .subscribe({
          next: () => {
            this.notification.success('Successfully reset password');
            this.router.navigate(['/login']);
          },
          error: (error) => {
            console.log('Could not reset password due to:');
            console.log(error);
            this.error = true;
            if (typeof error.error === 'object') {
              this.errorMessage = error.error.error;
            } else {
              this.errorMessage = error.error;
            }
          },
        });
    }
  }
}
