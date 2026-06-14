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
    <div class="max-w-4xl mx-auto px-6 py-12">

      <a routerLink="/products"
         class="inline-flex items-center gap-2 text-zinc-500 hover:text-white text-sm mb-8 transition-colors">
        ← Voltar ao catálogo
      </a>

      @if (loading()) {
        <div class="grid grid-cols-1 md:grid-cols-2 gap-12">
          <div class="bg-zinc-900 rounded-2xl h-80 animate-pulse border border-zinc-800"></div>
          <div class="space-y-4">
            <div class="h-6 bg-zinc-900 rounded-lg animate-pulse w-1/3"></div>
            <div class="h-10 bg-zinc-900 rounded-lg animate-pulse"></div>
            <div class="h-4 bg-zinc-900 rounded-lg animate-pulse w-2/3"></div>
          </div>
        </div>
      }

      @if (product() && !loading()) {
        <div class="grid grid-cols-1 md:grid-cols-2 gap-12">

          <!-- IMAGEM -->
          <div class="bg-zinc-900 border border-zinc-800 rounded-2xl h-80 flex items-center justify-center">
            <span class="text-8xl">🛍️</span>
          </div>

          <!-- INFO -->
          <div class="flex flex-col gap-6">

            @if (product()!.categoryName) {
              <span class="text-sm text-amber-400 font-medium uppercase tracking-wider">
                {{ product()!.categoryName }}
              </span>
            }

            <div>
              <h1 class="text-3xl font-display font-black text-white tracking-tight mb-2">
                {{ product()!.name }}
              </h1>
              <p class="text-zinc-500 text-sm">SKU: {{ product()!.sku }}</p>
            </div>

            <p class="text-zinc-400 text-sm leading-relaxed">
              {{ product()!.description }}
            </p>

            <div class="flex items-center gap-4">
              <span class="text-4xl font-display font-black text-white">
                {{ product()!.price | currency:'BRL':'symbol':'1.2-2' }}
              </span>
              <span class="text-sm px-3 py-1 rounded-lg"
                [class]="product()!.availableQuantity > 0
                  ? 'bg-green-500/10 text-green-400 border border-green-500/20'
                  : 'bg-red-500/10 text-red-400 border border-red-500/20'">
                {{ product()!.availableQuantity > 0
                  ? product()!.availableQuantity + ' disponíveis'
                  : 'Esgotado' }}
              </span>
            </div>

            @if (product()!.availableQuantity > 0) {
              <div class="flex items-center gap-3">
                <div class="flex items-center gap-2 bg-zinc-900 border border-zinc-800 rounded-xl p-1">
                  <button (click)="decQty()"
                    class="w-8 h-8 flex items-center justify-center text-zinc-400 hover:text-white
                           hover:bg-zinc-800 rounded-lg transition-colors text-lg font-bold">−</button>
                  <span class="w-8 text-center text-white font-semibold text-sm">{{ qty }}</span>
                  <button (click)="incQty()"
                    class="w-8 h-8 flex items-center justify-center text-zinc-400 hover:text-white
                           hover:bg-zinc-800 rounded-lg transition-colors text-lg font-bold">+</button>
                </div>

                <button (click)="buy()"
                  class="flex-1 py-3 bg-amber-400 text-zinc-950 font-bold rounded-xl
                         hover:bg-amber-300 transition-all text-sm tracking-wide">
                  Comprar agora
                </button>
              </div>
            }

            @if (feedbackMsg()) {
              <div class="px-4 py-3 rounded-xl text-sm"
                [class]="feedbackOk()
                  ? 'bg-green-500/10 border border-green-500/30 text-green-400'
                  : 'bg-red-500/10 border border-red-500/30 text-red-400'">
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
    const id = this.route.snapshot.paramMap.get('id')!;
    this.productService.findById(id).subscribe({
      next: p => { this.product.set(p); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  incQty(): void {
    if (this.qty < (this.product()?.availableQuantity ?? 1)) this.qty++;
  }

  decQty(): void {
    if (this.qty > 1) this.qty--;
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
