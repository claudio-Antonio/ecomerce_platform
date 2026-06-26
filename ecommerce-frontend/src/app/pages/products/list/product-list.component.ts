import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../core/services/api.services';
import { ProductResponse } from '../../../models/index';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="max-w-7xl mx-auto px-4 py-8">

      <div class="bg-white p-4 rounded shadow-sm mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border border-zinc-200">
        <div>
          <h1 class="text-xl font-bold text-zinc-900">
            Resultados
          </h1>
          <p class="text-zinc-500 text-xs">{{ filtered().length }} produtos encontrados</p>
        </div>

        <div class="w-full max-w-md flex">
          <input [(ngModel)]="search" type="text" placeholder="Buscar produtos no markethub..."
            class="w-full px-4 py-2 bg-white border border-zinc-300 rounded-l focus:outline-none focus:border-amazon-yellow text-zinc-800 text-sm shadow-inner" />
          <button class="bg-amazon-gold px-5 flex items-center justify-center rounded-r hover:bg-amazon-yellow text-zinc-900 transition-colors border-none outline-none">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4">
              <path stroke-linecap="round" stroke-linejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.604 10.604Z" />
            </svg>
          </button>
        </div>
      </div>

      @if (loading()) {
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          @for (n of [1,2,3,4,5,6,7,8]; track n) {
            <div class="bg-white rounded p-4 h-72 animate-pulse border border-zinc-200 shadow-sm"></div>
          }
        </div>
      }

      @if (!loading()) {
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          @for (product of filtered(); track product.id) {
            <a [routerLink]="['/products', product.id]"
               class="group bg-white border border-zinc-200 rounded p-4 flex flex-col gap-3
                      hover:shadow-lg transition-all duration-150 relative">

              <div class="w-full h-44 bg-zinc-50 rounded flex items-center justify-center overflow-hidden p-2">
                <img [src]="product.imageUrl" [alt]="product.name"
                     class="max-w-full max-h-full object-contain mix-blend-multiply group-hover:scale-105 transition-transform duration-200" />
              </div>

              <div class="flex-1 flex flex-col gap-1.5">
                @if (product.categoryName) {
                  <span class="text-[10px] text-zinc-500 font-bold uppercase tracking-wide">
                    {{ product.categoryName }}
                  </span>
                }

                <h3 class="text-zinc-900 font-normal text-sm leading-snug line-clamp-3 group-hover:text-amber-600 transition-colors">
                  {{ product.name }}
                </h3>

                <span class="text-[11px] text-zinc-400">SKU: {{ product.sku }}</span>
              </div>

              <div class="pt-2 border-t border-zinc-100 flex flex-col gap-1">
                <div class="flex items-baseline gap-1">
                  <span class="text-2xl font-normal text-zinc-900 tracking-tight">
                    {{ product.price | currency:'BRL':'symbol':'1.2-2' }}
                  </span>
                </div>

                <span class="text-xs font-medium"
                  [class]="product.availableQuantity > 0 ? 'text-green-600' : 'text-red-600'">
                  {{ product.availableQuantity > 0 ? 'Em estoque (' + product.availableQuantity + ')' : 'Fora de estoque' }}
                </span>
              </div>
            </a>
          }
        </div>

        @if (filtered().length === 0 && !loading()) {
          <div class="text-center py-20 bg-white border border-zinc-200 rounded shadow-sm">
            <p class="text-zinc-500 text-base">Nenhum produto encontrado para o termo pesquisado.</p>
          </div>
        }
      }
    </div>
  `
})
export class ProductListComponent implements OnInit {
  products = signal<ProductResponse[]>([]);
  loading  = signal(true);
  search   = '';

  filtered = computed(() =>
    this.products().filter(p =>
      p.name.toLowerCase().includes(this.search.toLowerCase()) ||
      (p.categoryName ?? '').toLowerCase().includes(this.search.toLowerCase())
    )
  );

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.productService.findAll().subscribe({
      next: data => {
        this.products.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}