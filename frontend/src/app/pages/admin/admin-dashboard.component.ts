import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AdminReport, AuditEntry, Ticket } from '../../core/models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  report: AdminReport | null = null;
  tickets: Ticket[] = [];
  audit: AuditEntry[] = [];

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.api.adminReports().subscribe({ next: (report) => (this.report = report) });
    this.api.adminTickets().subscribe({ next: (tickets) => (this.tickets = tickets.slice(0, 8)) });
    this.api.adminAudit().subscribe({ next: (audit) => (this.audit = audit.slice(0, 8)) });
  }

  entries(map?: Record<string, number>): { key: string; value: number }[] {
    return Object.entries(map || {}).map(([key, value]) => ({ key, value }));
  }
}
