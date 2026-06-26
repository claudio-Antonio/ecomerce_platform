import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CategoryService } from '../../../core/services/api.services';
import { CategoryResponse } from '../../../models/index';

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="max-w-3xl mx-auto px-4 py-8">

      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
        <div>
          <h1 class="text-2xl font-normal text-zinc-900">Categorias</h1>
          <p class="text-xs text-zinc-500 mt-0.5">Gerencie os departamentos de produtos do markethub</p>
        </div>
        <button (click)="openForm()"
          class="px-4 py-1.5 bg-amazon-yellow hover:bg-amber-500 text-zinc-950 font-medium text-sm rounded border border-amber-500 hover:border-amber-600 shadow-sm transition-colors">
          + Nova categoria
        </button>
      </div>

      @if (showForm()) {
        <div class="bg-white border border-zinc-200 rounded-md p-5 shadow-sm mb-6">
          <h2 class="text-sm font-bold text-zinc-900 mb-4">
            {{ editingId() ? 'Editar categoria' : 'Nova categoria' }}
          </h2>
          <form [formGroup]="form" (ngSubmit)="submit()" class="flex flex-col gap-4">
            
            <div class="flex flex-col gap-1">
              <label class="text-xs font-bold text-zinc-700 uppercase tracking-wider">Nome</label>
              <input formControlName="name" type="text" placeholder="Ex: Eletrônicos"
                class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm
                       focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm" />
            </div>

            <div class="flex flex-col gap-1">
              <label class="text-xs font-bold text-zinc-700 uppercase tracking-wider">Descrição</label>
              <input formControlName="description" type="text" placeholder="Ex: Smartphones, notebooks e acessórios"
                class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm
                       focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm" />
            </div>

            @if (formError()) {
              <div class="px-4 py-2 bg-red-50 border border-red-200 rounded text-red-600 text-sm">
                {{ formError() }}
              </div>
            }

            <div class="flex gap-3 mt-2">
              <button type="button" (click)="closeForm()"
                class="flex-1 py-1.5 border border-zinc-300 text-zinc-700 rounded bg-zinc-50 hover:bg-zinc-100 transition-colors text-sm font-medium">
                Cancelar
              </button>
              <button type="submit" [disabled]="formLoading() || form.invalid"
                class="flex-1 py-1.5 bg-amazon-yellow hover:bg-amber-500 text-zinc-950 font-medium rounded border border-amber-500 hover:border-amber-600 shadow-sm disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-sm">
                @if (formLoading()) { Salvando... } @else { Salvar }
              </button>
            </div>
          </form>
        </div>
      }

      @if (loading()) {
        <div class="space-y-3">
          @for (n of [1,2,3]; track n) {
            <div class="h-16 bg-white border border-zinc-200 rounded animate-pulse shadow-sm"></div>
          }
        </div>
      }

      @if (!loading()) {
        <div class="space-y-3">
          @for (cat of categories(); track cat.id) {
            <div class="bg-white border border-zinc-200 rounded-md px-4 py-3 shadow-sm
                        flex items-center justify-between gap-4 hover:border-zinc-300 transition-colors">
              <div>
                <p class="text-zinc-900 font-bold text-sm">{{ cat.name }}</p>
                <p class="text-zinc-500 text-xs mt-0.5">{{ cat.description }}</p>
              </div>
              <div class="flex gap-2 whitespace-nowrap">
                <button (click)="editCategory(cat)"
                  class="px-3 py-1 text-xs font-medium bg-white text-zinc-700 border border-zinc-300 rounded hover:bg-zinc-100 transition-colors">
                  Editar
                </button>
                <button (click)="deleteCategory(cat.id)"
                  class="px-3 py-1 text-xs font-medium bg-white text-red-600 border border-zinc-300 rounded hover:border-red-400 hover:bg-red-50 transition-colors">
                  Excluir
                </button>
              </div>
            </div>
          }

          @if (categories().length === 0) {
            <div class="text-center py-16 bg-white border border-zinc-200 rounded-md shadow-sm">
              <p class="text-zinc-500 text-sm">Nenhuma categoria cadastrada</p>
            </div>
          }
        </div>
      }
    </div>
  `
})
export class AdminCategoriesComponent implements OnInit {
  categories  = signal<CategoryResponse[]>([]);
  loading     = signal(true);
  showForm    = signal(false);
  editingId   = signal<string | null>(null);
  formLoading = signal(false);
  formError   = signal('');
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService
  ) {
    this.form = this.fb.group({
      name:        ['', Validators.required],
      description: ['', Validators.required]
    });
  }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.categoryService.findAll().subscribe({
      next: data => { this.categories.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  openForm(): void {
    this.editingId.set(null);
    this.form.reset();
    this.formError.set('');
    this.showForm.set(true);
  }

  editCategory(c: CategoryResponse): void {
    this.editingId.set(c.id);
    this.form.patchValue({ name: c.name, description: c.description });
    this.formError.set('');
    this.showForm.set(true);
  }

  closeForm(): void { this.showForm.set(false); this.editingId.set(null); }

  submit(): void {
    if (this.form.invalid) return;
    this.formLoading.set(true);
    this.formError.set('');

    const request$ = this.editingId()
      ? this.categoryService.update(this.editingId()!, this.form.value)
      : this.categoryService.create(this.form.value);

    request$.subscribe({
      next: () => { this.closeForm(); this.load(); this.formLoading.set(false); },
      error: () => { this.formError.set('Erro ao salvar.'); this.formLoading.set(false); }
    });
  }

  deleteCategory(id: string): void {
    if (!confirm('Excluir esta categoria?')) return;
    this.categoryService.delete(id).subscribe({ next: () => this.load() });
  }
}