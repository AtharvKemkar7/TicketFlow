import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { ChatMessage } from '../../core/models';

@Component({
  selector: 'app-intake-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './intake-page.component.html',
  styleUrl: './intake-page.component.css'
})
export class IntakePageComponent {
  sessionId: number | null = null;
  message = '';
  chat: ChatMessage[] = [
    { role: 'assistant', text: 'Describe the issue. I will classify it, create a ticket, and try to resolve it from the knowledge base.' }
  ];
  ticketId?: number;
  ticketNumber?: string;
  sending = false;

  constructor(private readonly api: ApiService) {}

  send(): void {
    const text = this.message.trim();
    if (!text || this.sending) {
      return;
    }
    this.sending = true;
    this.chat.push({ role: 'user', text });
    this.message = '';
    this.api.intake(this.sessionId, text).subscribe({
      next: (res) => {
        this.sessionId = res.sessionId;
        this.chat.push({ role: 'assistant', text: res.reply });
        if (res.createdTicketId) {
          this.ticketId = res.createdTicketId;
          this.ticketNumber = res.ticketNumber;
        }
        this.sending = false;
      },
      error: () => {
        this.chat.push({ role: 'assistant', text: 'Intake is unavailable right now.' });
        this.sending = false;
      }
    });
  }
}
