import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.css'
})
export class LoginPageComponent {
  loading = false;
  error = '';
  form = this.fb.nonNullable.group({
    email: ['user@helixdesk.local', [Validators.required, Validators.email]],
    password: ['Password123!', Validators.required]
  });

  fill(email: string): void {
    this.form.patchValue({ email, password: 'Password123!' });
  }

  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly router: Router
  ) {}

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.loading = true;
    this.error = '';
    const { email, password } = this.form.getRawValue();
    this.auth.login(email, password).subscribe({
      next: () => {
        this.loading = false;
        void this.router.navigateByUrl(this.auth.homeRoute());
      },
      error: (err) => {
        this.loading = false;
        const status = err?.status;
        if (status === 401 || status === 403) {
          this.error = 'Invalid credentials. Use user@helixdesk.local / Password123!';
        } else if (!status) {
          this.error = 'Cannot reach HelixDesk API. Try again in a moment.';
        } else {
          this.error = err?.error?.message || 'Sign in failed.';
        }
      }
    });
  }
}
