import {ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {MessageService} from '../../services/message.service';
import {Message} from '../../dtos/message';
import {NgbModal, NgbPaginationConfig} from '@ng-bootstrap/ng-bootstrap';
import {UntypedFormBuilder} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {Router} from '@angular/router';
import { Title } from '@angular/platform-browser';
import { MatPaginator, PageEvent } from '@angular/material/paginator';

@Component({
  selector: 'app-message',
  templateUrl: './message.component.html',
  styleUrls: ['./message.component.scss']
})
export class MessageComponent implements OnInit {

  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

  showAll = false;
  error = false;
  errorMessage = '';
  // After first submission attempt, form validation will start
  submitted = false;

  currentMessage: Message;
  currentPage = 0;
  totalPages = 0;
  pageSize = 12;
  loaded = false;
  skeletons: any[] = Array(this.pageSize).fill({});

  private message: Message[];

  constructor(private messageService: MessageService,
              private ngbPaginationConfig: NgbPaginationConfig,
              private formBuilder: UntypedFormBuilder,
              private cd: ChangeDetectorRef,
              private authService: AuthService,
              private modalService: NgbModal,
              private router: Router,
              private titleService: Title) {
    this.titleService.setTitle('News - Ticketline');
  }

  ngOnInit() {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.loaded = false;
    this.loadMessage();
  }

  formatDate(message: Message) {
    return message.publishedAt.substring(0, 10).replace(/-/g, '.');
  }

  loadAllMessages() {
    this.showAll = true;
    this.messageService.getSeenNews(this.currentPage, this.pageSize).subscribe({
      next: res => {
        this.message = res.messages;
        this.totalPages = res.totalCount;
        this.loaded = true;
      },
      error: err => {
        console.log(err);
      }
    });
  }

  isEmpty(): boolean {
    return this.message.length === 0;
  }

  /**
   * Returns true if the authenticated user is an admin
   */
  isAdmin(): boolean {
    return this.authService.getUserRole() === 'ADMIN';
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  openAddModal(messageAddModal: TemplateRef<any>) {
    this.currentMessage = new Message();
    this.modalService.open(messageAddModal, {ariaLabelledBy: 'modal-basic-title'});
  }

  openExistingMessageModal(id: number, messageAddModal: TemplateRef<any>) {
    this.messageService.getMessageById(id).subscribe({
      next: res => {
        this.currentMessage = res;
        this.modalService.open(messageAddModal, {ariaLabelledBy: 'modal-basic-title'});
      },
      error: err => {
        this.defaultServiceErrorHandling(err);
      }
    });
  }

  /**
   * Starts form validation and builds a message dto for sending a creation request if the form is valid.
   * If the procedure was successful, the form will be cleared.
   */
  addMessage(form) {
    this.submitted = true;


    if (form.valid) {
      this.currentMessage.publishedAt = new Date().toISOString();
      this.createMessage(this.currentMessage);
      this.clearForm();
    }
  }

  getMessage(): Message[] {
    return this.message;
  }

  /**
   * Error flag will be deactivated, which clears the error message
   */
  vanishError() {
    this.error = false;
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loaded = false;
    if (this.showAll) {
      this.loadAllMessages();
    } else {
      this.loadMessage();
    }
  }

  /**
   * Sends message creation request
   *
   * @param message the message which should be created
   */
  private createMessage(message: Message) {
    this.messageService.createMessage(message).subscribe({
        next: () => {
          this.loadMessage();
        },
        error: error => {
          this.defaultServiceErrorHandling(error);
        }
      }
    );
  }

  /**
   * Loads the specified page of message from the backend
   */
  private loadMessage() {
    this.showAll = false;
    this.messageService.getMessage(this.currentPage, this.pageSize).subscribe({
      next: res => {
        this.message = res.messages;
        this.totalPages = res.totalCount;
        this.loaded = true;
      },
      error: err => {
        console.log(err);
      }
    });
  }

  private defaultServiceErrorHandling(error: any) {
    console.log(error);
    this.error = true;
    if (typeof error.error === 'object') {
      this.errorMessage = error.error.error;
    } else {
      this.errorMessage = error.error;
    }
  }

  private clearForm() {
    this.currentMessage = new Message();
    this.submitted = false;
  }

}
