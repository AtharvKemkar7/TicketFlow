import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Ticket, UserProfile } from '../../core/models';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.css'
})
export class UserDashboardComponent implements OnInit {
  user: UserProfile | null = null;
  tickets: Ticket[] = [];

  constructor(private readonly api: ApiService, private readonly auth: AuthService) {}

  ngOnInit(): void {
    this.user = this.auth.currentUser();
    this.api.myTickets().subscribe({ next: (tickets) => (this.tickets = tickets) });
  }

  get openCount(): number {
    return this.tickets.filter((t) => !['CLOSED', 'CANCELLED'].includes(t.status)).length;
  }

  get waitingCount(): number {
    return this.tickets.filter((t) => t.status === 'USER_CONFIRMATION' || t.status === 'WAITING_FOR_USER').length;
  }

  get resolvedCount(): number {
    return this.tickets.filter((t) => t.status === 'CLOSED').length;
  }

  recent(): Ticket[] {
    return this.tickets.slice(0, 6);
  }
}
