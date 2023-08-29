import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpHeaderResponse, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {AuthService} from '../services/auth.service';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';
import {map, tap} from 'rxjs/operators';
import {Router} from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  handle403Error = tap({
      next: (event: HttpEvent<any>) => this.handle403Response(event),
      error: (error) => {
        if (error.status === 403) {
          console.log('403 Error');
          this.authService.logoutUser();
          this.router.navigate(['']);
        }
      }
    }
  );

  constructor(private authService: AuthService, private globals: Globals, private router: Router) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const authUri = this.globals.backendUri + '/authentication';
    const registerUri = this.globals.backendUri + '/users';

    // Do not intercept authentication requests
    /*if (!(req.url.includes('tickets') || req.url.includes('orders'))) {
      return next.handle(req)
        .pipe(this.handle403Error);
    }*/

    if (this.authService.isLoggedIn()) {
      const authReq = req.clone({
        headers: req.headers.set('Authorization', 'Bearer ' + this.authService.getToken())
      });
      return next.handle(authReq)
        .pipe(this.handle403Error);
    }

    return next.handle(req)
      .pipe(this.handle403Error);
  }

  handle403Response(event: HttpEvent<any>) {
    if (event instanceof HttpHeaderResponse && event.status === 403) {
      console.log('403 Error');
      this.authService.logoutUser();
      this.router.navigate(['']);
    }
    return event;
  }
}
