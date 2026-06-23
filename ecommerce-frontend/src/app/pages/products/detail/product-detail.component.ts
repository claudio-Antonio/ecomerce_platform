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
    <div class="max-w-6xl mx-auto px-6 py-12">

      <a routerLink="/products"
         class="inline-flex items-center gap-2 text-zinc-500 hover:text-white text-sm mb-8 transition-colors">
        ← Voltar ao catálogo
      </a>

      @if (loading()) {
        <div class="grid grid-cols-1 lg:grid-cols-[1.1fr_1fr] gap-10">
          <div class="bg-zinc-900 rounded-2xl h-96 animate-pulse border border-zinc-800"></div>
          <div class="space-y-4">
            <div class="h-6 bg-zinc-900 rounded-lg animate-pulse w-1/3"></div>
            <div class="h-10 bg-zinc-900 rounded-lg animate-pulse"></div>
            <div class="h-4 bg-zinc-900 rounded-lg animate-pulse w-2/3"></div>
          </div>
        </div>
      }

      @if (!loading() && error()) {
        <div class="px-4 py-3 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-sm">
          {{ error() }}
        </div>
      }

      @if (product() && !loading()) {
        <div class="grid grid-cols-1 lg:grid-cols-[1.1fr_1fr] gap-10">

          <!-- COLUNA ESQUERDA: GALERIA -->
          <div>
            <div class="bg-zinc-900 border border-zinc-800 rounded-2xl h-96 flex items-center justify-center overflow-hidden p-6">
              @if (product()!.imageUrl) {
                <img [src]="product()!.imageUrl" [alt]="product()!.name"
                     class="max-w-full max-h-full object-contain"
                     (error)="onImageError($event)" />
              } @else {
                <span class="text-8xl">🛍️</span>
              }
            </div>
          </div>

          <!-- COLUNA DIREITA: INFORMAÇÕES + COMPRA -->
          <div class="flex flex-col gap-5">

            <div>
              @if (product()!.categoryName) {
                <span class="text-xs text-amber-400 font-semibold uppercase tracking-wider">
                  {{ product()!.categoryName }}
                </span>
              }
              <h1 class="text-2xl font-display font-bold text-white tracking-tight mt-1 leading-snug">
                {{ product()!.name }}
              </h1>
            </div>

            <!-- PREÇO -->
            <div>
              <p class="text-xs text-zinc-500 mb-1">Preço</p>
              <p class="text-3xl font-display font-black text-white">
                {{ product()!.price | currency:'BRL':'symbol':'1.2-2' }}
              </p>
            </div>

            <!-- DISPONIBILIDADE -->
            <div>
              <span class="inline-flex items-center gap-1.5 text-sm px-3 py-1.5 rounded-lg"
                [class]="product()!.availableQuantity > 0
                  ? 'bg-green-500/10 text-green-400 border border-green-500/20'
                  : 'bg-red-500/10 text-red-400 border border-red-500/20'">
                <span class="w-1.5 h-1.5 rounded-full"
                  [class]="product()!.availableQuantity > 0 ? 'bg-green-400' : 'bg-red-400'"></span>
                {{ product()!.availableQuantity > 0
                  ? 'Em estoque — ' + product()!.availableQuantity + ' disponíveis'
                  : 'Esgotado' }}
              </span>
            </div>

            <!-- CARD DE COMPRA -->
            @if (product()!.availableQuantity > 0) {
              <div class="bg-zinc-900 border border-zinc-800 rounded-2xl p-5 flex flex-col gap-4">
                <div class="flex items-center gap-3">
                  <div class="flex items-center gap-2 bg-zinc-800 border border-zinc-700 rounded-xl p-1">
                    <button (click)="decQty()" aria-label="diminuir quantidade"
                      class="w-8 h-8 flex items-center justify-center text-zinc-400 hover:text-white
                             hover:bg-zinc-700 rounded-lg transition-colors text-lg font-bold">−</button>
                    <span class="w-8 text-center text-white font-semibold text-sm">{{ qty }}</span>
                    <button (click)="incQty()" aria-label="aumentar quantidade"
                      class="w-8 h-8 flex items-center justify-center text-zinc-400 hover:text-white
                             hover:bg-zinc-700 rounded-lg transition-colors text-lg font-bold">+</button>
                  </div>
                  <span class="text-xs text-zinc-500">
                    Total: {{ (product()!.price * qty) | currency:'BRL':'symbol':'1.2-2' }}
                  </span>
                </div>

                <button (click)="buy()"
                  class="w-full py-3 bg-amber-400 text-zinc-950 font-bold rounded-xl
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

            <!-- DESCRIÇÃO -->
            <div class="border-t border-zinc-800 pt-5">
              <h2 class="text-sm font-semibold text-white mb-2">Sobre este item</h2>
              <p class="text-sm text-zinc-400 leading-relaxed">
                {{ product()!.description }}
              </p>
            </div>

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