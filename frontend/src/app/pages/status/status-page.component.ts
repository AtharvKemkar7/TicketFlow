import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HealthResponse, InfoResponse, SystemService } from '../../core/services/system.service';

@Component({
  selector: 'app-status-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './status-page.component.html',
  styleUrl: './status-page.component.css'
})
export class StatusPageComponent implements OnInit {
  loading = true;
  error: string | null = null;
  health: HealthResponse | null = null;
  info: InfoResponse | null = null;

  constructor(private readonly systemService: SystemService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    this.error = null;

    this.systemService.getHealth().subscribe({
      next: (health) => {
        this.health = health;
        this.systemService.getInfo().subscribe({
          next: (info) => {
            this.info = info;
            this.loading = false;
          },
          error: () => {
            this.loading = false;
            this.error = 'API health responded, but system info could not be loaded.';
          }
        });
      },
      error: () => {
        this.loading = false;
        this.error = 'Unable to reach the HelixDesk API. Confirm the Spring Boot server is running on port 8080.';
      }
    });
  }
}
