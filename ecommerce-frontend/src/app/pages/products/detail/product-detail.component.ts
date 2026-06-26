import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../../core/services/api.services';
import { AuthService } from '../../../core/services/auth.service';
import { ProductResponse } from '../../../models/index';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="max-w-7xl mx-auto px-4 py-8">

      <div class="mb-6">
        <a routerLink="/products" class="text-sm text-blue-600 hover:text-amber-600 hover:underline">
          ← Voltar ao catálogo
        </a>
      </div>

      @if (loading()) {
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
          <div class="lg:col-span-5 bg-white border border-zinc-200 rounded h-96 animate-pulse shadow-sm"></div>
          <div class="lg:col-span-4 space-y-4">
            <div class="h-6 bg-white border border-zinc-200 rounded animate-pulse w-1/3"></div>
            <div class="h-10 bg-white border border-zinc-200 rounded animate-pulse"></div>
            <div class="h-24 bg-white border border-zinc-200 rounded animate-pulse"></div>
          </div>
          <div class="lg:col-span-3 bg-white border border-zinc-200 rounded h-48 animate-pulse shadow-sm"></div>
        </div>
      }

      @if (!loading() && error()) {
        <div class="px-4 py-3 bg-red-50 border border-red-200 rounded text-red-600 text-sm mb-6">
          {{ error() }}
        </div>
      }

      @if (product() && !loading()) {
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">

          <div class="lg:col-span-5 bg-white border border-zinc-200 rounded p-4 flex items-center justify-center min-h-[400px]">
            @if (product()!.imageUrl) {
              <img [src]="product()!.imageUrl" [alt]="product()!.name"
                   class="max-w-full max-h-[380px] object-contain mix-blend-multiply"
                   (error)="onImageError($event)" />
            } @else {
              <span class="text-8xl">🛍️</span>
            }
          </div>

          <div class="lg:col-span-4 flex flex-col gap-4">
            <div>
              @if (product()!.categoryName) {
                <span class="text-xs text-zinc-500 font-bold uppercase tracking-wide">
                  {{ product()!.categoryName }}
                </span>
              }
              <h1 class="text-2xl font-normal text-zinc-900 mt-1 leading-snug">
                {{ product()!.name }}
              </h1>
              <span class="text-xs text-zinc-400 block mt-1">SKU: {{ product()!.sku }}</span>
            </div>

            <hr class="border-zinc-200" />

            <div>
              <h4 class="text-sm font-bold text-zinc-900 mb-1">Sobre este item</h4>
              <p class="text-sm text-zinc-700 leading-relaxed">
                {{ product()!.description }}
              </p>
            </div>
          </div>

          <div class="lg:col-span-3 bg-white border border-zinc-200 rounded p-4 shadow-sm flex flex-col gap-4">
            
            <div>
              <span class="text-3xl font-normal text-zinc-900">
                {{ product()!.price | currency:'BRL':'symbol':'1.2-2' }}
              </span>
            </div>

            <div>
              <span class="text-sm font-medium block"
                [class]="product()!.availableQuantity > 0 ? 'text-green-600' : 'text-red-600'">
                {{ product()!.availableQuantity > 0
                  ? 'Em estoque — ' + product()!.availableQuantity + ' disponíveis'
                  : 'Fora de estoque' }}
              </span>
            </div>

            @if (product()!.availableQuantity > 0) {
              <div class="flex flex-col gap-3">
                <div class="flex items-center gap-2 text-sm text-zinc-700">
                  <span>Qtd:</span>
                  <div class="flex items-center border border-zinc-300 rounded bg-zinc-50 shadow-sm">
                    <button (click)="decQty()" aria-label="diminuir quantidade"
                      class="px-3 py-1 hover:bg-zinc-200 text-zinc-600 font-bold border-r border-zinc-300 transition-colors">−</button>
                    <span class="px-4 font-medium text-zinc-800 text-sm">{{ qty }}</span>
                    <button (click)="incQty()" aria-label="aumentar quantidade"
                      class="px-3 py-1 hover:bg-zinc-200 text-zinc-600 font-bold border-l border-zinc-300 transition-colors">+</button>
                  </div>
                </div>

                <div class="text-xs text-zinc-500">
                  Total: <span class="font-bold text-zinc-800">{{ (product()!.price * qty) | currency:'BRL':'symbol':'1.2-2' }}</span>
                </div>

                <button (click)="buy()"
                  class="w-full py-2 bg-amazon-yellow hover:bg-amber-500 text-zinc-950 font-medium text-sm rounded border border-amber-500 hover:border-amber-600 shadow-sm transition-colors mt-1">
                  Comprar agora
                </button>
              </div>
            }

            @if (feedbackMsg()) {
              <div class="px-4 py-2 rounded text-sm mt-2"
                [class]="feedbackOk()
                  ? 'bg-green-50 border border-green-200 text-green-600'
                  : 'bg-red-50 border border-red-200 text-red-600'">
                {{ feedbackMsg() }}
              </div>
            }

          </div>

        </div>
      }
    </div>
  `
})
export class ProductDetailComponent implements OnInit {
  product     = signal<ProductResponse | null>(null);
  loading     = signal(true);
  error       = signal('');
  feedbackMsg = signal('');
  feedbackOk  = signal(false);
  qty         = 1;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (!id) {
      this.error.set('ID do produto não encontrado na URL.');
      this.loading.set(false);
      return;
    }

    this.productService.findById(id).subscribe({
      next: p => {
        this.product.set(p);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Não foi possível carregar este produto.');
        this.loading.set(false);
        console.error('Erro ao buscar produto:', err);
      }
    });
  }

  incQty(): void {
    if (this.qty < (this.product()?.availableQuantity ?? 1)) this.qty++;
  }

  decQty(): void {
    if (this.qty > 1) this.qty--;
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).style.display = 'none';
  }

  buy(): void {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.router.navigate(['/orders/new'], {
      queryParams: {
        productId: this.product()!.id,
        quantity: this.qty
      }
    });
  }
}