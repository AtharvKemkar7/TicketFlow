import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { UserProfile } from '../../core/models';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.css'
})
export class AdminUsersComponent implements OnInit {
  users: UserProfile[] = [];
  form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['Password123!', Validators.required],
    role: ['USER', Validators.required],
    department: ['']
  });

  constructor(private readonly api: ApiService, private readonly fb: FormBuilder) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.api.adminUsers().subscribe({ next: (users) => (this.users = users) });
  }

  create(): void {
    if (this.form.invalid) {
      return;
    }
    this.api.createUser(this.form.getRawValue()).subscribe({
      next: () => {
        this.form.patchValue({ firstName: '', lastName: '', email: '', department: '' });
        this.reload();
      }
    });
  }
}
