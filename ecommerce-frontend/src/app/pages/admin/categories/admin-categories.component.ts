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
    <div class="max-w-3xl mx-auto px-6 py-12">

      <div class="flex items-center justify-between mb-10">
        <h1 class="text-4xl font-display font-black text-white tracking-tight">Categorias</h1>
        <button (click)="openForm()"
          class="px-4 py-2 text-sm bg-amber-400 text-zinc-950 font-bold rounded-xl
                 hover:bg-amber-300 transition-all">
          + Nova categoria
        </button>
      </div>

      <!-- FORMULÁRIO INLINE -->
      @if (showForm()) {
        <div class="bg-zinc-900 border border-amber-400/30 rounded-2xl p-6 mb-6">
          <h2 class="text-sm font-semibold text-white mb-4">
            {{ editingId() ? 'Editar categoria' : 'Nova categoria' }}
          </h2>
          <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4">
            <div>
              <label class="block text-xs text-zinc-400 mb-1.5 uppercase tracking-wider">Nome</label>
              <input formControlName="name" type="text"
                class="w-full px-4 py-3 bg-zinc-800 border border-zinc-700 rounded-xl text-white text-sm
                       focus:outline-none focus:border-amber-400 transition-all" />
            </div>
            <div>
              <label class="block text-xs text-zinc-400 mb-1.5 uppercase tracking-wider">Descrição</label>
              <input formControlName="description" type="text"
                class="w-full px-4 py-3 bg-zinc-800 border border-zinc-700 rounded-xl text-white text-sm
                       focus:outline-none focus:border-amber-400 transition-all" />
            </div>

            @if (formError()) {
              <div class="px-4 py-3 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-sm">
                {{ formError() }}
              </div>
            }

            <div class="flex gap-3">
              <button type="button" (click)="closeForm()"
                class="flex-1 py-2.5 border border-zinc-700 text-zinc-400 rounded-xl
                       hover:border-zinc-500 hover:text-white transition-all text-sm">
                Cancelar
              </button>
              <button type="submit" [disabled]="formLoading() || form.invalid"
                class="flex-1 py-2.5 bg-amber-400 text-zinc-950 font-bold rounded-xl
                       hover:bg-amber-300 transition-all disabled:opacity-50 text-sm">
                @if (formLoading()) { Salvando... } @else { Salvar }
              </button>
            </div>
          </form>
        </div>
      }

      <!-- LISTA -->
      @if (loading()) {
        <div class="space-y-3">
          @for (n of [1,2,3]; track n) {
            <div class="h-16 bg-zinc-900 rounded-xl animate-pulse border border-zinc-800"></div>
          }
        </div>
      }

      @if (!loading()) {
        <div class="space-y-3">
          @for (cat of categories(); track cat.id) {
            <div class="bg-zinc-900 border border-zinc-800 rounded-xl px-5 py-4
                        flex items-center justify-between">
              <div>
                <p class="text-white font-medium text-sm">{{ cat.name }}</p>
                <p class="text-zinc-500 text-xs mt-0.5">{{ cat.description }}</p>
              </div>
              <div class="flex gap-2">
                <button (click)="editCategory(cat)"
                  class="px-3 py-1.5 text-xs border border-zinc-700 text-zinc-400 rounded-lg
                         hover:border-amber-400 hover:text-amber-400 transition-all">
                  Editar
                </button>
                <button (click)="deleteCategory(cat.id)"
                  class="px-3 py-1.5 text-xs border border-zinc-700 text-zinc-400 rounded-lg
                         hover:border-red-500 hover:text-red-400 transition-all">
                  Excluir
                </button>
              </div>
            </div>
          }

          @if (categories().length === 0) {
            <div class="text-center py-16">
              <p class="text-zinc-600">Nenhuma categoria cadastrada</p>
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
