import {Component, OnInit} from '@angular/core';
import {MessageService} from '../../../services/message.service';
import {Message} from '../../../dtos/message';
import {ActivatedRoute} from '@angular/router';
import {AuthService} from '../../../services/auth.service';
import {Title} from '@angular/platform-browser';

@Component({
  selector: 'app-message-details',
  templateUrl: './message-details.component.html',
  styleUrls: ['./message-details.component.scss']
})
export class MessageDetailsComponent implements OnInit{

  message: Message = {title: '', summary: '', text: '', publishedAt: '', image: undefined};
  messageId: number;

  constructor(public authService: AuthService, private messageService: MessageService, public route: ActivatedRoute,
              private titleService: Title) {
  this.titleService.setTitle('News Details - Ticketline');
}

  formatDate() {
    return this.message.publishedAt.substring(0, 10).replace(/-/g, '.');
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      const id = params['id'];
      this.messageService.getMessageById(id).subscribe({
        next: res => {
          this.message = res;
          this.messageId = id;
          console.log(this.message);
        },
        error: err => {
          console.log(err);
        }
      });
    });
  }
}
