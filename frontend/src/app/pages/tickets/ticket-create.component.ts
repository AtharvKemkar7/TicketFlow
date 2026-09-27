import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { Category, Subcategory } from '../../core/models';

@Component({
  selector: 'app-ticket-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './ticket-create.component.html',
  styleUrl: './ticket-create.component.css'
})
export class TicketCreateComponent implements OnInit {
  categories: Category[] = [];
  subcategories: Subcategory[] = [];
  error = '';
  loading = false;
  form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    description: ['', Validators.required],
    categoryId: [null as number | null],
    subcategoryId: [null as number | null],
    impact: ['INDIVIDUAL', Validators.required],
    urgency: ['MEDIUM', Validators.required],
    businessEffect: ['']
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: ApiService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.api.categories().subscribe({ next: (cats) => (this.categories = cats) });
    this.form.controls.categoryId.valueChanges.subscribe((id) => {
      this.form.patchValue({ subcategoryId: null });
      if (!id) {
        this.subcategories = [];
        return;
      }
      this.api.subcategories(id).subscribe({ next: (subs) => (this.subcategories = subs) });
    });
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.loading = true;
    this.error = '';
    this.api.createTicket(this.form.getRawValue()).subscribe({
      next: (ticket) => {
        this.loading = false;
        void this.router.navigate(['/tickets', ticket.id]);
      },
      error: () => {
        this.loading = false;
        this.error = 'Unable to create ticket.';
      }
    });
  }
}
