import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Category, SlaPolicy, Specialist, Team } from '../../core/models';

@Component({
  selector: 'app-admin-catalog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-catalog.component.html',
  styleUrl: './admin-catalog.component.css'
})
export class AdminCatalogComponent implements OnInit {
  categories: Category[] = [];
  teams: Team[] = [];
  sla: SlaPolicy[] = [];
  specialists: Specialist[] = [];
  categoryName = '';
  categorySkill = '';
  teamName = '';
  slaPriority = 'MEDIUM';
  slaResponse = 60;
  slaResolution = 480;
  slaRisk = 30;

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.api.adminCategories().subscribe({ next: (items) => (this.categories = items) });
    this.api.adminTeams().subscribe({ next: (items) => (this.teams = items) });
    this.api.adminSla().subscribe({ next: (items) => (this.sla = items) });
    this.api.adminSpecialists().subscribe({ next: (items) => (this.specialists = items) });
  }

  addCategory(): void {
    if (!this.categoryName.trim()) {
      return;
    }
    this.api.createCategory({
      name: this.categoryName,
      requiredSkillName: this.categorySkill,
      active: true
    }).subscribe({
      next: () => {
        this.categoryName = '';
        this.categorySkill = '';
        this.reload();
      }
    });
  }

  addTeam(): void {
    if (!this.teamName.trim()) {
      return;
    }
    this.api.createTeam({ name: this.teamName, active: true }).subscribe({
      next: () => {
        this.teamName = '';
        this.reload();
      }
    });
  }

  addSla(): void {
    this.api.createSla({
      priority: this.slaPriority,
      responseTargetMinutes: this.slaResponse,
      resolutionTargetMinutes: this.slaResolution,
      atRiskThresholdMinutes: this.slaRisk
    }).subscribe({ next: () => this.reload() });
  }
}
