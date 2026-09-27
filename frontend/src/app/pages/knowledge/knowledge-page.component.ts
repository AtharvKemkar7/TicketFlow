import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { KnowledgeArticle } from '../../core/models';

@Component({
  selector: 'app-knowledge-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './knowledge-page.component.html',
  styleUrl: './knowledge-page.component.css'
})
export class KnowledgePageComponent implements OnInit {
  query = '';
  articles: KnowledgeArticle[] = [];
  selected: KnowledgeArticle | null = null;
  canCreate = this.auth.role() === 'ADMIN';
  title = '';
  content = '';
  tags = '';

  constructor(private readonly api: ApiService, private readonly auth: AuthService) {}

  ngOnInit(): void {
    this.search();
  }

  search(): void {
    this.api.knowledge(this.query || undefined).subscribe({
      next: (articles) => (this.articles = articles)
    });
  }

  open(article: KnowledgeArticle): void {
    this.selected = article;
  }

  create(): void {
    if (!this.title.trim() || !this.content.trim()) {
      return;
    }
    this.api.createArticle({
      title: this.title,
      content: this.content,
      tags: this.tags,
      status: 'PUBLISHED'
    }).subscribe({
      next: () => {
        this.title = '';
        this.content = '';
        this.tags = '';
        this.search();
      }
    });
  }
}
