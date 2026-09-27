import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Specialist, UserProfile } from '../../core/models';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.css'
})
export class ProfilePageComponent implements OnInit {
  user: UserProfile | null = null;
  specialist: Specialist | null = null;

  constructor(private readonly auth: AuthService, private readonly api: ApiService) {}

  ngOnInit(): void {
    this.api.me().subscribe({
      next: (user) => {
        this.user = user;
        this.auth.setUser(user);
      }
    });
    if (this.auth.role() === 'SPECIALIST' || this.auth.role() === 'ADMIN') {
      this.api.specialistProfile().subscribe({
        next: (profile) => (this.specialist = profile),
        error: () => (this.specialist = null)
      });
    }
  }
}
