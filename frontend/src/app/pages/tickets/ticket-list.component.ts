import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Ticket } from '../../core/models';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './ticket-list.component.html',
  styleUrl: './ticket-list.component.css'
})
export class TicketListComponent implements OnInit {
  tickets: Ticket[] = [];
  filter = '';
  role = this.auth.role();

  constructor(private readonly api: ApiService, private readonly auth: AuthService) {}

  ngOnInit(): void {
    const source = this.role === 'ADMIN'
      ? this.api.adminTickets()
      : this.role === 'SPECIALIST'
        ? this.api.specialistTickets()
        : this.api.myTickets();
    source.subscribe({ next: (tickets) => (this.tickets = tickets) });
  }

  filtered(): Ticket[] {
    const q = this.filter.trim().toLowerCase();
    if (!q) {
      return this.tickets;
    }
    return this.tickets.filter((t) =>
      [t.ticketNumber, t.title, t.status, t.priority, t.categoryName].join(' ').toLowerCase().includes(q)
    );
  }
}
