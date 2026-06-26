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
    <div class="max-w-6xl mx-auto px-4 py-8">

      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
        <div>
          <h1 class="text-2xl font-normal text-zinc-900 tracking-tight">
            Gerenciar produtos
          </h1>
          <p class="text-xs text-zinc-500 mt-0.5">Configure e atualize o catálogo do markethub</p>
        </div>
        <button (click)="openForm()"
          class="px-4 py-1.5 bg-amazon-yellow hover:bg-amber-500 text-zinc-950 font-medium text-sm rounded border border-amber-500 hover:border-amber-600 shadow-sm transition-colors">
          + Novo produto
        </button>
      </div>

      @if (showForm()) {
        <div class="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4 backdrop-blur-xs">
          <div class="bg-white border border-zinc-200 rounded-md p-6 w-full max-w-lg shadow-xl max-h-[90vh] overflow-y-auto">
            <h2 class="text-lg font-bold text-zinc-900 mb-5 pb-2 border-b border-zinc-100">
              {{ editingId() ? 'Editar produto' : 'Novo produto' }}
            </h2>

            <form [formGroup]="form" (ngSubmit)="submit()" class="flex flex-col gap-4">

              <div class="flex flex-col gap-1">
                <label class="text-xs font-bold text-zinc-700 uppercase tracking-wider">Nome</label>
                <input formControlName="name" type="text" placeholder="Ex: Echo Dot 5ª Geração"
                  class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm" />
              </div>

              <div class="flex flex-col gap-1">
                <label class="text-xs font-bold text-zinc-700 uppercase tracking-wider">Descrição</label>
                <textarea formControlName="description" rows="3" placeholder="Insira os detalhes do produto..."
                  class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm resize-none"></textarea>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div class="flex flex-col gap-1">
                  <label class="text-xs font-bold text-zinc-700 uppercase tracking-wider">Preço (R$)</label>
                  <input formControlName="price" type="number" step="0.01" min="0.01"
                    class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm" />
                </div>
                <div class="flex flex-col gap-1">
                  <label class="text-xs font-bold text-zinc-700 uppercase tracking-wider">Estoque</label>
                  <input formControlName="stockQuantity" type="number" min="0"
                    class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm" />
                </div>
              </div>

              <div class="flex flex-col gap-1">
                <label class="text-xs font-bold text-zinc-700 uppercase tracking-wider">SKU</label>
                <input formControlName="sku" type="text" placeholder="Ex: ELEC-ECHO-05"
                  class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm" />
              </div>

              <div class="flex flex-col gap-1">
                <label class="text-xs font-bold text-zinc-700 uppercase tracking-wider">Categoria</label>
                <input formControlName="category" type="text"
                  placeholder="Digite o nome da categoria"
                  list="categorias-existentes"
                  class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm" />
                <datalist id="categorias-existentes">
                  @for (cat of categories(); track cat.id) {
                    <option [value]="cat.name"></option>
                  }
                </datalist>
                @if (categoryHint()) {
                  <p class="text-xs text-amber-600 font-medium mt-1">{{ categoryHint() }}</p>
                }
              </div>

              <div class="flex flex-col gap-1">
                <label class="text-xs font-bold text-zinc-700 uppercase tracking-wider">URL da imagem</label>
                <input formControlName="imageUrl" type="url" placeholder="https://exemplo.com/imagem.jpg"
                  class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm" />
              </div>

              <div class="flex items-center gap-2 mt-1">
                <input formControlName="active" type="checkbox" id="active"
                  class="w-4 h-4 rounded border-zinc-300 accent-amber-500 cursor-pointer" />
                <label for="active" class="text-sm text-zinc-700 cursor-pointer select-none">Produto ativo no catálogo</label>
              </div>

              @if (formError()) {
                <div class="px-4 py-2 bg-red-50 border border-red-200 rounded text-red-600 text-sm">
                  {{ formError() }}
                </div>
              }

              <div class="flex gap-3 mt-3 pt-2 border-t border-zinc-100">
                <button type="button" (click)="closeForm()"
                  class="flex-1 py-2 border border-zinc-300 text-zinc-700 rounded bg-zinc-50 hover:bg-zinc-100 transition-colors text-sm font-medium">
                  Cancelar
                </button>
                <button type="submit" [disabled]="formLoading() || form.invalid"
                  class="flex-1 py-2 bg-amazon-yellow hover:bg-amber-500 text-zinc-950 font-medium rounded border border-amber-500 hover:border-amber-600 shadow-sm disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-sm">
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
            <div class="h-16 bg-white border border-zinc-200 rounded animate-pulse shadow-sm"></div>
          }
        </div>
      }

      @if (!loading()) {
        <div class="bg-white border border-zinc-200 rounded-md shadow-sm overflow-hidden">
          <div class="overflow-x-auto">
            <table class="w-full text-sm text-left border-collapse">
              <thead>
                <tr class="bg-zinc-50 border-b border-zinc-200 text-xs font-bold text-zinc-500 uppercase tracking-wider">
                  <th class="px-6 py-3.5">Produto</th>
                  <th class="px-6 py-3.5">Categoria</th>
                  <th class="px-6 py-3.5">Preço</th>
                  <th class="px-6 py-3.5">Estoque</th>
                  <th class="px-6 py-3.5">Status</th>
                  <th class="px-6 py-3.5 text-right">Ações</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-zinc-100">
                @for (product of products(); track product.id) {
                  <tr class="hover:bg-zinc-50/60 transition-colors text-zinc-700">
                    
                    <td class="px-6 py-4">
                      <div class="font-bold text-zinc-900">{{ product.name }}</div>
                      <div class="text-xs text-zinc-400 font-mono mt-0.5">SKU: {{ product.sku }}</div>
                    </td>
                    
                    <td class="px-6 py-4 text-zinc-600">
                      {{ product.categoryName ?? '—' }}
                    </td>
                    
                    <td class="px-6 py-4 font-medium text-zinc-900">
                      {{ product.price | currency:'BRL':'symbol':'1.2-2' }}
                    </td>
                    
                    <td class="px-6 py-4">
                      <span class="font-medium" [class]="product.availableQuantity > 0 ? 'text-zinc-900' : 'text-rose-600'">
                        {{ product.availableQuantity }}
                      </span>
                    </td>
                    
                    <td class="px-6 py-4">
                      <span class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded text-xs font-medium border"
                        [class]="product.active
                          ? 'bg-green-50 text-green-700 border-green-200'
                          : 'bg-zinc-50 text-zinc-500 border-zinc-200'">
                        @if (product.active) {
                          <span class="w-1.5 h-1.5 rounded-full bg-green-500"></span>
                        }
                        {{ product.active ? 'Ativo' : 'Inativo' }}
                      </span>
                    </td>
                    
                    <td class="px-6 py-4 text-right whitespace-nowrap">
                      <div class="flex items-center justify-end gap-2">
                        <button (click)="editProduct(product)"
                          class="px-3 py-1 text-xs font-medium bg-white text-zinc-700 border border-zinc-300 rounded hover:bg-zinc-100 transition-colors">
                          Editar
                        </button>
                        <button (click)="deleteProduct(product.id)"
                          class="px-3 py-1 text-xs font-medium bg-white text-red-600 border border-zinc-300 rounded hover:border-red-400 hover:bg-red-50 transition-colors">
                          Excluir
                        </button>
                      </div>
                    </td>

                  </tr>
                }
              </tbody>
            </table>
          </div>

          @if (products().length === 0) {
            <div class="text-center py-16">
              <p class="text-zinc-500 text-sm">Nenhum produto cadastrado</p>
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
      imageUrl:      ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadProducts();
    this.loadCategories();

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