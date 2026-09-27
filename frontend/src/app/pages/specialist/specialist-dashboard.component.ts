import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Specialist, SpecialistDashboard, Ticket } from '../../core/models';

@Component({
  selector: 'app-specialist-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './specialist-dashboard.component.html',
  styleUrl: './specialist-dashboard.component.css'
})
export class SpecialistDashboardComponent implements OnInit {
  stats: SpecialistDashboard | null = null;
  tickets: Ticket[] = [];
  profile: Specialist | null = null;
  availability = 'AVAILABLE';

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.api.specialistDashboard().subscribe({ next: (stats) => (this.stats = stats) });
    this.api.specialistTickets().subscribe({ next: (tickets) => (this.tickets = tickets) });
    this.api.specialistProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.availability = profile.availability;
      }
    });
  }

  updateAvailability(): void {
    this.api.setAvailability(this.availability).subscribe({
      next: (profile) => (this.profile = profile)
    });
  }
}
