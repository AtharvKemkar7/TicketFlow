import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { NotificationItem } from '../../core/models';

@Component({
  selector: 'app-notifications-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './notifications-page.component.html',
  styleUrl: './notifications-page.component.css'
})
export class NotificationsPageComponent implements OnInit {
  items: NotificationItem[] = [];

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.api.notifications().subscribe({ next: (items) => (this.items = items) });
  }

  mark(item: NotificationItem): void {
    this.api.markRead(item.id).subscribe({
      next: () => (item.read = true)
    });
  }
}
