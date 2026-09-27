import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/api.service';
import { AdminReport, AuditEntry } from '../../core/models';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-reports.component.html',
  styleUrl: './admin-reports.component.css'
})
export class AdminReportsComponent implements OnInit {
  report: AdminReport | null = null;
  audit: AuditEntry[] = [];

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.api.adminReports().subscribe({ next: (report) => (this.report = report) });
    this.api.adminAudit().subscribe({ next: (audit) => (this.audit = audit) });
  }

  entries(map?: Record<string, number>): { key: string; value: number }[] {
    return Object.entries(map || {}).map(([key, value]) => ({ key, value }));
  }
}
