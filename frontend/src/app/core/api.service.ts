import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import {
  AdminReport,
  Attachment,
  AuditEntry,
  Category,
  Comment,
  IntakeResponse,
  KnowledgeArticle,
  NotificationItem,
  ResolutionResponse,
  Setting,
  SlaPolicy,
  Specialist,
  SpecialistDashboard,
  Subcategory,
  Team,
  Ticket,
  TimelineEntry,
  UserProfile
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly base = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  me() {
    return this.http.get<UserProfile>(`${this.base}/auth/me`);
  }

  myTickets() {
    return this.http.get<Ticket[]>(`${this.base}/tickets/my`);
  }

  ticket(id: number) {
    return this.http.get<Ticket>(`${this.base}/tickets/${id}`);
  }

  createTicket(payload: unknown) {
    return this.http.post<Ticket>(`${this.base}/tickets`, payload);
  }

  comments(id: number) {
    return this.http.get<Comment[]>(`${this.base}/tickets/${id}/comments`);
  }

  addComment(id: number, body: string, internal = false) {
    return this.http.post<Comment>(`${this.base}/tickets/${id}/comments`, { body, internal });
  }

  confirm(id: number, accepted: boolean, feedback?: string) {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/confirm-resolution`, { accepted, feedback });
  }

  cancel(id: number) {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/cancel`, {});
  }

  resolve(id: number, resolutionSummary: string) {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/resolve`, { resolutionSummary });
  }

  start(id: number) {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/start`, {});
  }

  wait(id: number) {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/wait`, {});
  }

  reopen(id: number, feedback?: string) {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/reopen`, { accepted: false, feedback });
  }

  reassign(id: number, specialistId: number, reason: string) {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/reassign`, { specialistId, reason });
  }

  escalate(id: number, specialistId: number, reason: string, notes?: string) {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/escalate`, { specialistId, reason, notes });
  }

  assign(id: number, specialistId: number, reason?: string) {
    return this.http.post<Ticket>(`${this.base}/tickets/${id}/assign`, { specialistId, reason });
  }

  categories() {
    return this.http.get<Category[]>(`${this.base}/categories`);
  }

  subcategories(categoryId?: number) {
    const query = categoryId ? `?categoryId=${categoryId}` : '';
    return this.http.get<Subcategory[]>(`${this.base}/subcategories${query}`);
  }

  specialists() {
    return this.http.get<Specialist[]>(`${this.base}/specialists`);
  }

  availableSpecialists() {
    return this.http.get<Specialist[]>(`${this.base}/specialists/available`);
  }

  specialistTickets() {
    return this.http.get<Ticket[]>(`${this.base}/specialist/tickets`);
  }

  specialistDashboard() {
    return this.http.get<SpecialistDashboard>(`${this.base}/specialist/dashboard`);
  }

  specialistProfile() {
    return this.http.get<Specialist>(`${this.base}/specialist/profile`);
  }

  setAvailability(status: string) {
    return this.http.put<Specialist>(`${this.base}/specialist/availability?status=${status}`, {});
  }

  intake(sessionId: number | null, message: string) {
    return this.http.post<IntakeResponse>(`${this.base}/ai/intake`, { sessionId, message });
  }

  resolution(ticketId: number, message: string, requestHuman = false) {
    const query = requestHuman ? '?requestHuman=true' : '';
    return this.http.post<ResolutionResponse>(`${this.base}/ai/tickets/${ticketId}/resolution${query}`, { message });
  }

  requestSpecialist(ticketId: number) {
    return this.http.post<ResolutionResponse>(`${this.base}/ai/tickets/${ticketId}/request-specialist`, {});
  }

  notifications() {
    return this.http.get<NotificationItem[]>(`${this.base}/notifications`);
  }

  unreadCount() {
    return this.http.get<{ count: number }>(`${this.base}/notifications/unread-count`);
  }

  markRead(id: number) {
    return this.http.post(`${this.base}/notifications/${id}/read`, {});
  }

  knowledge(q?: string) {
    const query = q ? `?q=${encodeURIComponent(q)}` : '';
    return this.http.get<KnowledgeArticle[]>(`${this.base}/knowledge${query}`);
  }

  adminTickets() {
    return this.http.get<Ticket[]>(`${this.base}/admin/tickets`);
  }

  adminUsers() {
    return this.http.get<UserProfile[]>(`${this.base}/admin/users`);
  }

  adminReports() {
    return this.http.get<AdminReport>(`${this.base}/admin/reports`);
  }

  adminAudit() {
    return this.http.get<AuditEntry[]>(`${this.base}/admin/audit`);
  }

  adminTeams() {
    return this.http.get<Team[]>(`${this.base}/admin/teams`);
  }

  adminCategories() {
    return this.http.get<Category[]>(`${this.base}/admin/categories`);
  }

  adminSla() {
    return this.http.get<SlaPolicy[]>(`${this.base}/admin/sla`);
  }

  adminKnowledge() {
    return this.http.get<KnowledgeArticle[]>(`${this.base}/admin/knowledge`);
  }

  adminSpecialists() {
    return this.http.get<Specialist[]>(`${this.base}/admin/specialists`);
  }

  createUser(payload: unknown) {
    return this.http.post<UserProfile>(`${this.base}/admin/users`, payload);
  }

  createArticle(payload: unknown) {
    return this.http.post<KnowledgeArticle>(`${this.base}/admin/knowledge`, payload);
  }

  createCategory(payload: unknown) {
    return this.http.post<Category>(`${this.base}/admin/categories`, payload);
  }

  createTeam(payload: unknown) {
    return this.http.post<Team>(`${this.base}/admin/teams`, payload);
  }

  createSla(payload: unknown) {
    return this.http.post<SlaPolicy>(`${this.base}/admin/sla`, payload);
  }

  createSubcategory(payload: unknown) {
    return this.http.post<Subcategory>(`${this.base}/admin/subcategories`, payload);
  }

  createSpecialist(payload: unknown) {
    return this.http.post<Specialist>(`${this.base}/admin/specialists`, payload);
  }

  updateUser(id: number, payload: unknown) {
    return this.http.put<UserProfile>(`${this.base}/admin/users/${id}`, payload);
  }

  history(id: number) {
    return this.http.get<TimelineEntry[]>(`${this.base}/tickets/${id}/history`);
  }

  attachments(id: number) {
    return this.http.get<Attachment[]>(`${this.base}/tickets/${id}/attachments`);
  }

  uploadAttachment(id: number, file: File) {
    const data = new FormData();
    data.append('file', file);
    return this.http.post<Attachment>(`${this.base}/tickets/${id}/attachments`, data);
  }

  adminSettings() {
    return this.http.get<Setting[]>(`${this.base}/admin/settings`);
  }

  saveSetting(key: string, value: string) {
    return this.http.post<Setting>(`${this.base}/admin/settings`, { key, value });
  }
}
