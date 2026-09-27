import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface HealthResponse {
  status: string;
  application: string;
  phase: number;
  timestamp: string;
  database: {
    status: string;
    product?: string;
    version?: string;
    valid?: boolean;
    error?: string;
  };
}

export interface InfoResponse {
  name: string;
  application: string;
  description: string;
  phase: number;
  phaseName: string;
  javaVersion: string;
  timestamp: string;
  stack: string[];
}

@Injectable({ providedIn: 'root' })
export class SystemService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  getHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(`${this.baseUrl}/health`);
  }

  getInfo(): Observable<InfoResponse> {
    return this.http.get<InfoResponse>(`${this.baseUrl}/info`);
  }
}
