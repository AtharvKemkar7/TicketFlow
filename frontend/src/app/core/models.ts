export type Role = 'USER' | 'SPECIALIST' | 'ADMIN';

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  fullName: string;
  role: Role;
}

export interface UserProfile {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  role: Role;
  status: string;
  department?: string;
}

export interface Ticket {
  id: number;
  ticketNumber: string;
  title: string;
  description: string;
  status: string;
  priority: string;
  impact: string;
  urgency: string;
  businessEffect?: string;
  requesterId: number;
  requesterName: string;
  categoryId?: number;
  categoryName?: string;
  subcategoryId?: number;
  subcategoryName?: string;
  assignedSpecialistId?: number;
  assignedSpecialistName?: string;
  assignedTeamId?: number;
  assignedTeamName?: string;
  currentSupportLevel?: string;
  slaState: string;
  slaResponseDueAt?: string;
  slaResolutionDueAt?: string;
  resolutionSummary?: string;
  aiAttemptCount: number;
  humanRequested: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Comment {
  id: number;
  type: string;
  body: string;
  author: string;
  createdAt: string;
}

export interface NotificationItem {
  id: number;
  type: string;
  message: string;
  read: boolean;
  ticketId: number;
  createdAt: string;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
  active: boolean;
  requiredSkillName?: string;
}

export interface Subcategory {
  id: number;
  name: string;
  categoryId: number;
  active: boolean;
  requiredSkillName?: string;
}

export interface Specialist {
  id: number;
  userId: number;
  fullName: string;
  email: string;
  supportLevel: string;
  availability: string;
  currentWorkload: number;
  active: boolean;
  teamId?: number;
  teamName?: string;
  skills: string[];
}

export interface KnowledgeArticle {
  id: number;
  title: string;
  content: string;
  categoryId?: number;
  categoryName?: string;
  subcategoryId?: number;
  subcategoryName?: string;
  tags?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface IntakeResponse {
  sessionId: number;
  reply: string;
  complete: boolean;
  suggestedCategory?: string;
  suggestedSubcategory?: string;
  impact?: string;
  urgency?: string;
  title?: string;
  createdTicketId?: number;
  ticketNumber?: string;
}

export interface ResolutionResponse {
  reply: string;
  attemptCount: number;
  handedOff: boolean;
  handoffReason?: string;
  sources?: string[];
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  text: string;
}

export interface SpecialistDashboard {
  assigned: number;
  open: number;
  inProgress: number;
  waiting: number;
  resolved: number;
  reassigned: number;
  escalated: number;
  highPriority: number;
  slaAtRisk: number;
  workload: number;
  level: string;
  availability: string;
}

export interface TimelineEntry {
  id: number;
  actor?: string;
  action: string;
  previousValue?: string;
  newValue?: string;
  reason?: string;
  createdAt: string;
}

export interface Attachment {
  id: number;
  filename: string;
  sizeBytes: number;
  contentType: string;
}

export interface Setting {
  key: string;
  value: string;
}

export interface AdminReport {
  totalTickets: number;
  openTickets: number;
  closedTickets: number;
  resolvedTickets: number;
  reopenedTickets: number;
  escalatedTickets: number;
  byCategory: Record<string, number>;
  byPriority: Record<string, number>;
  byTeam: Record<string, number>;
  bySpecialist: Record<string, number>;
  averageResolutionHours: number;
  slaCompliancePercent: number;
  aiResolutionAttempts: number;
  aiToHumanHandoffs: number;
}

export interface Team {
  id: number;
  name: string;
  description?: string;
  active: boolean;
  primaryCategoryId?: number;
  primaryCategoryName?: string;
}

export interface SlaPolicy {
  id: number;
  priority: string;
  responseTargetMinutes: number;
  resolutionTargetMinutes: number;
  atRiskThresholdMinutes: number;
}

export interface AuditEntry {
  id: number;
  actor?: string;
  action: string;
  ticketId?: number;
  previousValue?: string;
  newValue?: string;
  reason?: string;
  createdAt: string;
}
