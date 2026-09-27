import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../core/auth.service';
import { ApiService } from '../core/api.service';
import { UserProfile } from '../core/models';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.css'
})
export class ShellComponent implements OnInit {
  user: UserProfile | null = null;
  unread = 0;
  menuOpen = false;

  constructor(private readonly auth: AuthService, private readonly api: ApiService) {}

  ngOnInit(): void {
    this.auth.user$.subscribe((user) => (this.user = user));
    this.api.unreadCount().subscribe({
      next: (res) => (this.unread = res.count),
      error: () => (this.unread = 0)
    });
  }

  logout(): void {
    this.auth.logout();
  }

  is(role: string): boolean {
    return this.user?.role === role;
  }
}
