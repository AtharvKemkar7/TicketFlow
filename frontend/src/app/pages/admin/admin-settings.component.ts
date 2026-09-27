import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Setting } from '../../core/models';

@Component({
  selector: 'app-admin-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-settings.component.html',
  styleUrl: './admin-settings.component.css'
})
export class AdminSettingsComponent implements OnInit {
  settings: Setting[] = [];
  key = 'ticket.prefix';
  value = 'HX';

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.api.adminSettings().subscribe({ next: (items) => (this.settings = items) });
  }

  save(): void {
    if (!this.key.trim()) {
      return;
    }
    this.api.saveSetting(this.key, this.value).subscribe({ next: () => this.reload() });
  }
}
