import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ProductService, CategoryService } from '../../../core/services/api.services';
import { ProductResponse, CategoryResponse } from '../../../models/index';
import { switchMap, of } from 'rxjs';

@Component({
  selector: 'app-admin-products',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="max-w-6xl mx-auto px-6 py-12">

      <div class="flex items-center justify-between mb-10">
        <h1 class="text-4xl font-display font-black text-white tracking-tight">
          Gerenciar produtos
        </h1>
        <button (click)="openForm()"
          class="px-4 py-2 text-sm bg-amber-400 text-zinc-950 font-bold rounded-xl
                 hover:bg-amber-300 transition-all">
          + Novo produto
        </button>
      </div>

      @if (showForm()) {
        <div class="fixed inset-0 z-50 bg-black/70 flex items-center justify-center p-4">
          <div class="bg-zinc-900 border border-zinc-800 rounded-2xl p-8 w-full max-w-lg
                      max-h-[90vh] overflow-y-auto">
            <h2 class="text-xl font-display font-black text-white mb-6">
              {{ editingId() ? 'Editar produto' : 'Novo produto' }}
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
                <textarea formControlName="description" rows="3"
                  class="w-full px-4 py-3 bg-zinc-800 border border-zinc-700 rounded-xl text-white text-sm
                         focus:outline-none focus:border-amber-400 transition-all resize-none"></textarea>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-xs text-zinc-400 mb-1.5 uppercase tracking-wider">Preço (R$)</label>
                  <input formControlName="price" type="number" step="0.01" min="0.01"
                    class="w-full px-4 py-3 bg-zinc-800 border border-zinc-700 rounded-xl text-white text-sm
                           focus:outline-none focus:border-amber-400 transition-all" />
                </div>
                <div>
                  <label class="block text-xs text-zinc-400 mb-1.5 uppercase tracking-wider">Estoque</label>
                  <input formControlName="stockQuantity" type="number" min="0"
                    class="w-full px-4 py-3 bg-zinc-800 border border-zinc-700 rounded-xl text-white text-sm
                           focus:outline-none focus:border-amber-400 transition-all" />
                </div>
              </div>

              <div>
                <label class="block text-xs text-zinc-400 mb-1.5 uppercase tracking-wider">SKU</label>
                <input formControlName="sku" type="text"
                  class="w-full px-4 py-3 bg-zinc-800 border border-zinc-700 rounded-xl text-white text-sm
                         focus:outline-none focus:border-amber-400 transition-all" />
              </div>

              <div>
                <label class="block text-xs text-zinc-400 mb-1.5 uppercase tracking-wider">Categoria</label>
                <input formControlName="category" type="text"
                  placeholder="Digite o nome (ex: Livros) — criada automaticamente se não existir"
                  list="categorias-existentes"
                  class="w-full px-4 py-3 bg-zinc-800 border border-zinc-700 rounded-xl text-white text-sm
                         focus:outline-none focus:border-amber-400 transition-all" />
                <datalist id="categorias-existentes">
                  @for (cat of categories(); track cat.id) {
                    <option [value]="cat.name"></option>
                  }
                </datalist>
                @if (categoryHint()) {
                  <p class="text-xs text-amber-400/80 mt-1.5">{{ categoryHint() }}</p>
                }
              </div>

              <div>
                <label class="block text-xs text-zinc-400 mb-1.5 uppercase tracking-wider">URL da imagem</label>
                <input formControlName="imageUrl" type="url" placeholder="https://exemplo.com/imagem.jpg"
                  class="w-full px-4 py-3 bg-zinc-800 border border-zinc-700 rounded-xl text-white text-sm
                        focus:outline-none focus:border-amber-400 transition-all" />
              </div>

              <div class="flex items-center gap-3">
                <input formControlName="active" type="checkbox" id="active"
                  class="w-4 h-4 rounded accent-amber-400" />
                <label for="active" class="text-sm text-zinc-400">Produto ativo</label>
              </div>

              @if (formError()) {
                <div class="px-4 py-3 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-sm">
                  {{ formError() }}
                </div>
              }

              <div class="flex gap-3 pt-2">
                <button type="button" (click)="closeForm()"
                  class="flex-1 py-3 border border-zinc-700 text-zinc-400 rounded-xl hover:border-zinc-500
                         hover:text-white transition-all text-sm">
                  Cancelar
                </button>
                <button type="submit" [disabled]="formLoading() || form.invalid"
                  class="flex-1 py-3 bg-amber-400 text-zinc-950 font-bold rounded-xl hover:bg-amber-300
                         transition-all disabled:opacity-50 text-sm">
                  @if (formLoading()) { Salvando... } @else { Salvar }
                </button>
              </div>
            </form>
          </div>
        </div>
      }

      @if (loading()) {
        <div class="space-y-3">
          @for (n of [1,2,3,4]; track n) {
            <div class="h-16 bg-zinc-900 rounded-xl animate-pulse border border-zinc-800"></div>
          }
        </div>
      }

      @if (!loading()) {
        <div class="bg-zinc-900 border border-zinc-800 rounded-2xl overflow-hidden">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-zinc-800">
                <th class="px-6 py-4 text-left text-xs font-medium text-zinc-500 uppercase tracking-wider">Produto</th>
                <th class="px-6 py-4 text-left text-xs font-medium text-zinc-500 uppercase tracking-wider">Categoria</th>
                <th class="px-6 py-4 text-left text-xs font-medium text-zinc-500 uppercase tracking-wider">Preço</th>
                <th class="px-6 py-4 text-left text-xs font-medium text-zinc-500 uppercase tracking-wider">Estoque</th>
                <th class="px-6 py-4 text-left text-xs font-medium text-zinc-500 uppercase tracking-wider">Status</th>
                <th class="px-6 py-4 text-right text-xs font-medium text-zinc-500 uppercase tracking-wider">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-zinc-800">
              @for (product of products(); track product.id) {
                <tr class="hover:bg-zinc-800/50 transition-colors">
                  <td class="px-6 py-4">
                    <div>
                      <p class="text-white font-medium">{{ product.name }}</p>
                      <p class="text-zinc-600 text-xs mt-0.5">{{ product.sku }}</p>
                    </div>
                  </td>
                  <td class="px-6 py-4 text-zinc-400">{{ product.categoryName ?? '—' }}</td>
                  <td class="px-6 py-4 text-white font-semibold">
                    {{ product.price | currency:'BRL':'symbol':'1.2-2' }}
                  </td>
                  <td class="px-6 py-4">
                    <span [class]="product.availableQuantity > 0 ? 'text-green-400' : 'text-red-400'">
                      {{ product.availableQuantity }}
                    </span>
                  </td>
                  <td class="px-6 py-4">
                    <span class="text-xs px-2 py-1 rounded-lg"
                      [class]="product.active
                        ? 'bg-green-500/10 text-green-400'
                        : 'bg-zinc-800 text-zinc-500'">
                      {{ product.active ? 'Ativo' : 'Inativo' }}
                    </span>
                  </td>
                  <td class="px-6 py-4 text-right">
                    <div class="flex items-center justify-end gap-2">
                      <button (click)="editProduct(product)"
                        class="px-3 py-1.5 text-xs border border-zinc-700 text-zinc-400 rounded-lg
                               hover:border-amber-400 hover:text-amber-400 transition-all">
                        Editar
                      </button>
                      <button (click)="deleteProduct(product.id)"
                        class="px-3 py-1.5 text-xs border border-zinc-700 text-zinc-400 rounded-lg
                               hover:border-red-500 hover:text-red-400 transition-all">
                        Excluir
                      </button>
                    </div>
                  </td>
                </tr>
              }
            </tbody>
          </table>

          @if (products().length === 0) {
            <div class="text-center py-16">
              <p class="text-zinc-600">Nenhum produto cadastrado</p>
            </div>
          }
        </div>
      }
    </div>
  `
})
export class AdminProductsComponent implements OnInit {
  products      = signal<ProductResponse[]>([]);
  categories    = signal<CategoryResponse[]>([]);
  loading       = signal(true);
  showForm      = signal(false);
  editingId     = signal<string | null>(null);
  formLoading   = signal(false);
  formError     = signal('');
  categoryHint  = signal('');
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private categoryService: CategoryService
  ) {
    this.form = this.fb.group({
      name:          ['', Validators.required],
      description:   ['', Validators.required],
      price:         [0.01, [Validators.required, Validators.min(0.01)]],
      stockQuantity: [0, [Validators.required, Validators.min(0)]],
      sku:           ['', Validators.required],
      category:      ['', Validators.required],
      active:        [true],
      imageUrl: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadProducts();
    this.loadCategories();

    // mostra dica em tempo real se a categoria digitada é nova
    this.form.get('category')?.valueChanges.subscribe(value => {
      const typed = (value ?? '').trim().toLowerCase();
      if (!typed) { this.categoryHint.set(''); return; }

      const exists = this.categories().some(c => c.name.toLowerCase() === typed);
      this.categoryHint.set(
        exists ? '' : `Categoria nova — será criada automaticamente ao salvar.`
      );
    });
  }

  loadProducts(): void {
    this.loading.set(true);
    this.productService.findAll().subscribe({
      next: data => { this.products.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  loadCategories(): void {
    this.categoryService.findAll().subscribe({
      next: cats => this.categories.set(cats)
    });
  }

  openForm(): void {
    this.editingId.set(null);
    this.form.reset({ active: true, price: 0.01, stockQuantity: 0 });
    this.formError.set('');
    this.categoryHint.set('');
    this.showForm.set(true);
  }

  editProduct(p: ProductResponse): void {
    this.editingId.set(p.id);
    this.form.patchValue({
      name: p.name, description: p.description,
      price: p.price, stockQuantity: p.availableQuantity,
      sku: p.sku, active: p.active, category: p.categoryName ?? ''
    });
    this.formError.set('');
    this.categoryHint.set('');
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editingId.set(null);
  }

  /**
   * Resolve o nome de categoria digitado para um categoryId real.
   * Se já existir uma categoria com esse nome (case-insensitive), reusa o ID.
   * Se não existir, cria a categoria na hora e usa o ID retornado.
   */
  private resolveCategoryId(categoryName: string) {
    const typed = categoryName.trim();
    const existing = this.categories().find(
      c => c.name.toLowerCase() === typed.toLowerCase()
    );

    if (existing) {
      return of(existing.id);
    }

    return this.categoryService
      .create({ name: typed, description: `Categoria criada automaticamente: ${typed}` })
      .pipe(switchMap(created => of(created.id)));
  }

  submit(): void {
    if (this.form.invalid) return;
    this.formLoading.set(true);
    this.formError.set('');

    const { category, ...rest } = this.form.value;

    this.resolveCategoryId(category).pipe(
      switchMap(categoryId => {
        const payload = { ...rest, categoryId };
        return this.editingId()
          ? this.productService.update(this.editingId()!, payload)
          : this.productService.create(payload);
      })
    ).subscribe({
      next: () => {
        this.closeForm();
        this.loadProducts();
        this.loadCategories();
        this.formLoading.set(false);
      },
      error: () => {
        this.formError.set('Erro ao salvar produto.');
        this.formLoading.set(false);
      }
    });
  }

  deleteProduct(id: string): void {
    if (!confirm('Excluir este produto?')) return;
    this.productService.delete(id).subscribe({
      next: () => this.loadProducts()
    });
  }
}