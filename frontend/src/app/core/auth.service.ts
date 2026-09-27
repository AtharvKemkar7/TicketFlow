import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, Role, UserProfile } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'helixdesk.token';
  private readonly userKey = 'helixdesk.user';
  private readonly userSubject = new BehaviorSubject<UserProfile | null>(this.readUser());

  readonly user$ = this.userSubject.asObservable();

  constructor(private readonly http: HttpClient, private readonly router: Router) {}

  login(email: string, password: string) {
    return this.http.post<AuthResponse>(`${environment.apiBaseUrl}/auth/login`, { email, password }).pipe(
      tap((res) => this.persist(res))
    );
  }

  register(payload: { email: string; password: string; firstName: string; lastName: string; department?: string }) {
    return this.http.post<AuthResponse>(`${environment.apiBaseUrl}/auth/register`, payload).pipe(
      tap((res) => this.persist(res))
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.userSubject.next(null);
    void this.router.navigate(['/login']);
  }

  token(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  currentUser(): UserProfile | null {
    return this.userSubject.value;
  }

  role(): Role | null {
    return this.currentUser()?.role ?? null;
  }

  isLoggedIn(): boolean {
    return !!this.token();
  }

  homeRoute(): string {
    const role = this.role();
    if (role === 'ADMIN') {
      return '/admin';
    }
    if (role === 'SPECIALIST') {
      return '/specialist';
    }
    return '/app';
  }

  setUser(user: UserProfile): void {
    localStorage.setItem(this.userKey, JSON.stringify(user));
    this.userSubject.next(user);
  }

  private persist(res: AuthResponse): void {
    localStorage.setItem(this.tokenKey, res.token);
    const user: UserProfile = {
      id: res.userId,
      email: res.email,
      firstName: res.fullName.split(' ')[0] ?? '',
      lastName: res.fullName.split(' ').slice(1).join(' '),
      fullName: res.fullName,
      role: res.role,
      status: 'ACTIVE'
    };
    localStorage.setItem(this.userKey, JSON.stringify(user));
    this.userSubject.next(user);
  }

  private readUser(): UserProfile | null {
    const raw = localStorage.getItem(this.userKey);
    return raw ? (JSON.parse(raw) as UserProfile) : null;
  }
}
