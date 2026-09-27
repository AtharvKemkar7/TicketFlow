import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Attachment, ChatMessage, Comment, Specialist, Ticket, TimelineEntry } from '../../core/models';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ticket-detail.component.html',
  styleUrl: './ticket-detail.component.css'
})
export class TicketDetailComponent implements OnInit {
  ticket: Ticket | null = null;
  comments: Comment[] = [];
  attachments: Attachment[] = [];
  history: TimelineEntry[] = [];
  specialists: Specialist[] = [];
  commentBody = '';
  internal = false;
  resolution = '';
  feedback = '';
  aiMessage = '';
  specialistId: number | null = null;
  reason = '';
  chat: ChatMessage[] = [];
  error = '';
  role = this.auth.role();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly api: ApiService,
    private readonly auth: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
    this.api.availableSpecialists().subscribe({ next: (list) => (this.specialists = list) });
  }

  load(id: number): void {
    this.api.ticket(id).subscribe({ next: (ticket) => (this.ticket = ticket) });
    this.api.comments(id).subscribe({ next: (comments) => (this.comments = comments) });
    this.api.attachments(id).subscribe({ next: (items) => (this.attachments = items) });
    this.api.history(id).subscribe({ next: (items) => (this.history = items) });
  }

  refresh(): void {
    if (this.ticket) {
      this.load(this.ticket.id);
    }
  }

  addComment(): void {
    if (!this.ticket || !this.commentBody.trim()) {
      return;
    }
    this.api.addComment(this.ticket.id, this.commentBody, this.internal).subscribe({
      next: () => {
        this.commentBody = '';
        this.refresh();
      }
    });
  }

  start(): void {
    if (!this.ticket) {
      return;
    }
    this.api.start(this.ticket.id).subscribe({ next: (t) => (this.ticket = t) });
  }

  wait(): void {
    if (!this.ticket) {
      return;
    }
    this.api.wait(this.ticket.id).subscribe({ next: (t) => (this.ticket = t) });
  }

  resolve(): void {
    if (!this.ticket || !this.resolution.trim()) {
      return;
    }
    this.api.resolve(this.ticket.id, this.resolution).subscribe({ next: (t) => (this.ticket = t) });
  }

  confirm(accepted: boolean): void {
    if (!this.ticket) {
      return;
    }
    this.api.confirm(this.ticket.id, accepted, this.feedback).subscribe({ next: (t) => (this.ticket = t) });
  }

  cancel(): void {
    if (!this.ticket) {
      return;
    }
    this.api.cancel(this.ticket.id).subscribe({ next: (t) => (this.ticket = t) });
  }

  assign(): void {
    if (!this.ticket || !this.specialistId) {
      return;
    }
    this.api.assign(this.ticket.id, this.specialistId, this.reason).subscribe({ next: (t) => (this.ticket = t) });
  }

  reassign(): void {
    if (!this.ticket || !this.specialistId || !this.reason.trim()) {
      return;
    }
    this.api.reassign(this.ticket.id, this.specialistId, this.reason).subscribe({ next: (t) => (this.ticket = t) });
  }

  onFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!this.ticket || !file) {
      return;
    }
    this.api.uploadAttachment(this.ticket.id, file).subscribe({
      next: () => {
        input.value = '';
        this.refresh();
      }
    });
  }

  escalate(): void {
    if (!this.ticket || !this.specialistId || !this.reason.trim()) {
      return;
    }
    this.api.escalate(this.ticket.id, this.specialistId, this.reason).subscribe({ next: (t) => (this.ticket = t) });
  }

  sendAi(): void {
    if (!this.ticket || !this.aiMessage.trim()) {
      return;
    }
    const message = this.aiMessage;
    this.chat.push({ role: 'user', text: message });
    this.aiMessage = '';
    this.api.resolution(this.ticket.id, message).subscribe({
      next: (res) => {
        this.chat.push({ role: 'assistant', text: res.reply });
        this.refresh();
      },
      error: () => this.chat.push({ role: 'assistant', text: 'AI could not respond.' })
    });
  }

  requestHuman(): void {
    if (!this.ticket) {
      return;
    }
    this.api.requestSpecialist(this.ticket.id).subscribe({
      next: (res) => {
        this.chat.push({ role: 'assistant', text: res.reply });
        this.refresh();
      }
    });
  }

  canWork(): boolean {
    return this.role === 'SPECIALIST' || this.role === 'ADMIN';
  }

  canConfirm(): boolean {
    return this.ticket?.status === 'USER_CONFIRMATION' && this.role === 'USER';
  }
}
