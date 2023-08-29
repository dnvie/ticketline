import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {TicketService} from '../../services/ticket.service';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  darkTheme = false;

  constructor(
    public authService: AuthService,
    private ticketService: TicketService,
    private route: ActivatedRoute,
    private router: Router) {
    this.router.events.subscribe((event: any) => {
      if (event instanceof NavigationEnd) {
        this.removeActive();
        if (this.router.url.includes('/news')) {
          this.setActiveNav('news');
        } else if (this.router.url.includes('/events') && !this.router.url.includes('/events/search')) {
          this.setActiveNav('events');
        } else if (this.router.url.includes('/cart')) {
          this.setActiveNav('cart');
        } else if (this.router.url.includes('/events/search')) {
          this.setActiveNav('search');
        } else if (this.router.url.includes('/login')) {
          this.setActiveNav('login');
        } else if (this.router.url.includes('/registration')) {
          this.setActiveNav('signup');
        }
      }
    });
  }

  ngOnInit() {
  }

  setActiveNav(id: string) {
    const navItem = document.getElementById(id);
    this.removeActive();
    navItem.classList.add('navButtonActive');
  }

  removeActive() {
    const navItems = document.getElementsByClassName('navButton');
    for (const item of Array.from(navItems)) {
      item.classList.remove('navButtonActive');
    }
  }

  switchTheme() {
    if (document.documentElement.getAttribute('data-theme') === 'dark') {
      document.documentElement.setAttribute('data-theme', 'light');
      localStorage.setItem('data-theme', 'light');
      this.darkTheme = false;
    } else {
      document.documentElement.setAttribute('data-theme', 'dark');
      localStorage.setItem('data-theme', 'dark');
      this.darkTheme = true;
    }
  }


  shoppingCartCounter(): string {
      //iterate over shoppingCart and sum up the variable amount
      const counter = this.ticketService.sizeOfCart();
      if(counter === 0){
        return '';
      }
      return '('+ counter.toString() +')';
  }
}
