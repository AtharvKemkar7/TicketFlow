import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/auth.guard';
import { LoginPageComponent } from './pages/login/login-page.component';
import { RegisterPageComponent } from './pages/register/register-page.component';
import { ShellComponent } from './layout/shell.component';
import { UserDashboardComponent } from './pages/user/user-dashboard.component';
import { TicketListComponent } from './pages/tickets/ticket-list.component';
import { TicketCreateComponent } from './pages/tickets/ticket-create.component';
import { TicketDetailComponent } from './pages/tickets/ticket-detail.component';
import { IntakePageComponent } from './pages/intake/intake-page.component';
import { NotificationsPageComponent } from './pages/notifications/notifications-page.component';
import { ProfilePageComponent } from './pages/profile/profile-page.component';
import { SpecialistDashboardComponent } from './pages/specialist/specialist-dashboard.component';
import { AdminDashboardComponent } from './pages/admin/admin-dashboard.component';
import { AdminUsersComponent } from './pages/admin/admin-users.component';
import { AdminCatalogComponent } from './pages/admin/admin-catalog.component';
import { AdminReportsComponent } from './pages/admin/admin-reports.component';
import { AdminSettingsComponent } from './pages/admin/admin-settings.component';
import { KnowledgePageComponent } from './pages/knowledge/knowledge-page.component';

export const routes: Routes = [
  { path: 'login', component: LoginPageComponent, canActivate: [guestGuard] },
  { path: 'register', component: RegisterPageComponent, canActivate: [guestGuard] },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'app' },
      { path: 'app', component: UserDashboardComponent, canActivate: [roleGuard(['USER', 'ADMIN'])] },
      { path: 'tickets', component: TicketListComponent },
      { path: 'tickets/new', component: TicketCreateComponent },
      { path: 'tickets/:id', component: TicketDetailComponent },
      { path: 'intake', component: IntakePageComponent },
      { path: 'notifications', component: NotificationsPageComponent },
      { path: 'profile', component: ProfilePageComponent },
      { path: 'knowledge', component: KnowledgePageComponent },
      { path: 'specialist', component: SpecialistDashboardComponent, canActivate: [roleGuard(['SPECIALIST', 'ADMIN'])] },
      { path: 'admin', component: AdminDashboardComponent, canActivate: [roleGuard(['ADMIN'])] },
      { path: 'admin/users', component: AdminUsersComponent, canActivate: [roleGuard(['ADMIN'])] },
      { path: 'admin/catalog', component: AdminCatalogComponent, canActivate: [roleGuard(['ADMIN'])] },
      { path: 'admin/reports', component: AdminReportsComponent, canActivate: [roleGuard(['ADMIN'])] },
      { path: 'admin/settings', component: AdminSettingsComponent, canActivate: [roleGuard(['ADMIN'])] }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
