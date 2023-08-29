import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {MessageService} from '../../../services/message.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Title} from '@angular/platform-browser';

export enum MessageCreateEditMode {
  create,
  edit,
}

@Component({
  selector: 'app-message-create-edit',
  templateUrl: './message-create-edit.component.html',
  styleUrls: ['./message-create-edit.component.scss']
})
export class MessageCreateEditComponent implements OnInit{

  mode: MessageCreateEditMode = MessageCreateEditMode.create;
  id: number;
  heading = 'Add News Entry';
  message = {title: '', summary: '', text: '', publishedAt: '', image: undefined};
  inputFormControl = new FormControl('', [Validators.required]);
  newsForm = new FormGroup({
    title: new FormControl('', Validators.required),
    summary: new FormControl('', Validators.required),
    text: new FormControl('', Validators.required)
  });

  constructor(private messageService: MessageService,
              private router: Router,
              private route: ActivatedRoute,
              private notification: ToastrService,
              private titleService: Title) {
  }

 imageUploaded() {
    const file = document.querySelector(
      'input[type=file]')['files'][0];

    const reader = new FileReader();

    reader.onload = () => {
      const base64String = reader.result;
      console.log(base64String);
      this.message.image = base64String;
    };
   reader.readAsDataURL(file);
  }

  saveNews() {
    if(this.newsForm.valid) {
      this.message.title = this.newsForm.controls.title.value;
      this.message.summary = this.newsForm.controls.summary.value;
      this.message.text = this.newsForm.controls.text.value;
      if (this.message.image === undefined) {
        // eslint-disable-next-line max-len
        this.message.image = 'data:image/png;base64,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';
      }
      if (this.mode === MessageCreateEditMode.create) {
        this.messageService.createMessage(this.message).subscribe({
          next: res => {
            this.notification.success('News entry created successfully');
            this.router.navigate(['/news']);
          },
          error: err => {
            this.notification.error('News entry creation failed');
            console.log(err);
          }
        });
      } else {
       this.messageService.updateMessage(this.message, this.id).subscribe({
          next: res => {
            this.notification.success('News entry updated successfully');
            this.router.navigate(['/news']);
          },
          error: err => {
            this.notification.error('News entry update failed');
            console.log(err);
          }
        });
      }
    } else {
      this.newsForm.markAllAsTouched();
    }
  }

  delete() {
    if(confirm('Are you sure you want to delete this news entry?')) {
      this.messageService.deleteMessage(this.id).subscribe({
        next: res => {
          this.notification.success('News entry deleted successfully');
          this.router.navigate(['/news']);
        },
        error: err => {
          this.notification.error('News entry deletion failed');
          console.log(err);
        }
      });
    }
  }

  ngOnInit(): void {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.route.data.subscribe(data => {
      this.mode = data.mode;
      if (this.mode === MessageCreateEditMode.create) {
        this.titleService.setTitle('Create News Entry - Ticketline');
      } else {
        this.titleService.setTitle('Edit News Entry - Ticketline');
      }
      if (this.mode === MessageCreateEditMode.edit) {
        this.heading = 'Edit News Entry';
        this.id = +this.route.snapshot.paramMap.get('id');
      }
    });
    if (this.mode === MessageCreateEditMode.edit) {
      this.messageService.getMessageById(this.id).subscribe({
        next: res => {
          this.message = res;
          this.newsForm.patchValue({
            title: this.message.title,
            summary: this.message.summary,
            text: this.message.text
          });
        },
        error: err => {
          this.notification.error('Error loading news entry');
        }
      });
    }
  }
}
