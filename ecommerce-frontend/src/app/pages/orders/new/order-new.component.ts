import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { OrderService, ProductService } from '../../../core/services/api.services';
import { AuthService } from '../../../core/services/auth.service';
import { ProductResponse } from '../../../models/index';

@Component({
  selector: 'app-order-new',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  template: `
    <div class="max-w-2xl mx-auto px-6 py-12">

      <a routerLink="/products"
         class="inline-flex items-center gap-2 text-zinc-500 hover:text-white text-sm mb-8 transition-colors">
        ← Voltar ao catálogo
      </a>

      <h1 class="text-3xl font-display font-black text-white tracking-tight mb-8">
        Finalizar pedido
      </h1>

      @if (loading()) {
        <div class="space-y-4">
          <div class="h-32 bg-zinc-900 rounded-2xl animate-pulse border border-zinc-800"></div>
          <div class="h-24 bg-zinc-900 rounded-2xl animate-pulse border border-zinc-800"></div>
        </div>
      }

      @if (!loading() && product()) {
        <!-- RESUMO DO PRODUTO -->
        <div class="bg-zinc-900 border border-zinc-800 rounded-2xl p-6 mb-6">
          <h2 class="text-xs font-medium text-zinc-500 uppercase tracking-wider mb-4">Resumo do pedido</h2>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-white font-semibold">{{ product()!.name }}</p>
              <p class="text-zinc-500 text-sm mt-1">Quantidade: {{ quantity }}</p>
            </div>
            <span class="text-xl font-display font-black text-amber-400">
              {{ (product()!.price * quantity) | currency:'BRL':'symbol':'1.2-2' }}
            </span>
          </div>
        </div>

        <!-- FORMULÁRIO -->
        <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4">

          <div>
            <label class="block text-xs font-medium text-zinc-400 mb-1.5 uppercase tracking-wider">
              Método de pagamento
            </label>
            <select formControlName="paymentMethod"
              class="w-full px-4 py-3 bg-zinc-900 border border-zinc-800 rounded-xl text-white
                     focus:outline-none focus:border-amber-400 focus:ring-1 focus:ring-amber-400/30
                     transition-all text-sm">
              <option value="CREDIT_CARD">Cartão de crédito</option>
              <option value="DEBIT_CARD">Cartão de débito</option>
              <option value="PIX">PIX</option>
              <option value="BOLETO">Boleto bancário</option>
            </select>
          </div>

          @if (error()) {
            <div class="px-4 py-3 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-sm">
              {{ error() }}
            </div>
          }

          <button type="submit" [disabled]="submitting() || form.invalid"
            class="w-full py-4 bg-amber-400 text-zinc-950 font-bold rounded-xl hover:bg-amber-300
                   transition-all disabled:opacity-50 disabled:cursor-not-allowed text-sm tracking-wide">
            @if (submitting()) {
              Processando pedido...
            } @else {
              Confirmar pedido · {{ (product()!.price * quantity) | currency:'BRL':'symbol':'1.2-2' }}
            }
          </button>

        </form>
      }
    </div>
  `
})
export class OrderNewComponent implements OnInit {
  product    = signal<ProductResponse | null>(null);
  loading    = signal(true);
  submitting = signal(false);
  error      = signal('');
  quantity   = 1;
  form: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private orderService: OrderService,
    private productService: ProductService,
    private auth: AuthService
  ) {
    this.form = this.fb.group({
      paymentMethod: ['CREDIT_CARD', Validators.required]
    });
  }

  ngOnInit(): void {
    const productId = this.route.snapshot.queryParamMap.get('productId')!;
    this.quantity   = Number(this.route.snapshot.queryParamMap.get('quantity') ?? 1);

    this.productService.findById(productId).subscribe({
      next: p => { this.product.set(p); this.loading.set(false); },
      error: () => { this.error.set('Produto não encontrado.'); this.loading.set(false); }
    });
  }

  submit(): void {
    if (this.form.invalid || !this.product()) return;
    this.submitting.set(true);
    this.error.set('');

    // decodifica o userId do token JWT
    const token   = this.auth.getToken()!;
    const payload = JSON.parse(atob(token.split('.')[1]));

    this.orderService.create({
      userId: payload.sub,
      PaymentMethod: this.form.value.paymentMethod,
      items: [{
        productId: this.product()!.id,
        quantity: this.quantity
      }]
    }).subscribe({
      next: order => this.router.navigate(['/orders', order.id]),
      error: () => {
        this.error.set('Erro ao criar pedido. Tente novamente.');
        this.submitting.set(false);
      }
    });
  }
}
