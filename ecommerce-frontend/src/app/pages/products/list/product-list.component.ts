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
    <div class="max-w-7xl mx-auto px-6 py-12">

      <!-- HERO -->
      <div class="mb-12">
        <h1 class="text-5xl font-display font-black text-white tracking-tighter mb-3">
          Catálogo
        </h1>
        <p class="text-zinc-500 text-lg">{{ filtered().length }} produtos disponíveis</p>
      </div>

      <!-- BUSCA -->
      <div class="mb-8">
        <input [(ngModel)]="search" type="text" placeholder="Buscar produtos..."
          class="w-full max-w-md px-5 py-3 bg-zinc-900 border border-zinc-800 rounded-xl text-white
                 placeholder-zinc-600 focus:outline-none focus:border-amber-400
                 focus:ring-1 focus:ring-amber-400/30 transition-all text-sm" />
      </div>

      <!-- LOADING -->
      @if (loading()) {
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          @for (n of [1,2,3,4,5,6,7,8]; track n) {
            <div class="bg-zinc-900 rounded-2xl h-64 animate-pulse border border-zinc-800"></div>
          }
        </div>
      }

      <!-- GRID DE PRODUTOS -->
      @if (!loading()) {
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          @for (product of filtered(); track product.id) {
            <a [routerLink]="['/products', product.id]"
               class="group bg-zinc-900 border border-zinc-800 rounded-2xl p-5 flex flex-col gap-4
                      hover:border-amber-400/50 hover:bg-zinc-800/50 transition-all duration-200">

              <!-- PLACEHOLDER IMAGEM -->
              <div class="w-full h-40 bg-zinc-800 rounded-xl flex items-center justify-center
                          group-hover:bg-zinc-700 transition-colors overflow-hidden">
                <img [src]="product.imageUrl" [alt]="product.name"
                class="w-full h-40 object-cover rounded-xl" />
              </div>

              <div class="flex-1 flex flex-col gap-2">
                <!-- CATEGORIA -->
                @if (product.categoryName) {
                  <span class="text-xs text-amber-400 font-medium uppercase tracking-wider">
                    {{ product.categoryName }}
                  </span>
                }

                <!-- NOME -->
                <h3 class="text-white font-semibold text-sm leading-tight line-clamp-2
                           group-hover:text-amber-400 transition-colors">
                  {{ product.name }}
                </h3>

                <!-- SKU -->
                <span class="text-xs text-zinc-600">{{ product.sku }}</span>
              </div>

              <div class="flex items-center justify-between pt-2 border-t border-zinc-800">
                <!-- PREÇO -->
                <span class="text-lg font-display font-black text-white">
                  {{ product.price | currency:'BRL':'symbol':'1.2-2' }}
                </span>

                <!-- ESTOQUE -->
                <span class="text-xs px-2 py-1 rounded-lg"
                  [class]="product.availableQuantity > 0
                    ? 'bg-green-500/10 text-green-400'
                    : 'bg-red-500/10 text-red-400'">
                  {{ product.availableQuantity > 0 ? product.availableQuantity + ' disp.' : 'Esgotado' }}
                </span>
              </div>
            </a>
          }
        </div>

        @if (filtered().length === 0 && !loading()) {
          <div class="text-center py-24">
            <p class="text-zinc-600 text-lg">Nenhum produto encontrado para "{{ search }}"</p>
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
