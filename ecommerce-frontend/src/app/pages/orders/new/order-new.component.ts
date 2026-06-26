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
    <div class="max-w-2xl mx-auto px-4 py-8">

      <div class="mb-6">
        <a routerLink="/products" class="text-sm text-blue-600 hover:text-amber-600 hover:underline">
          ← Voltar ao catálogo
        </a>
      </div>

      <h1 class="text-2xl font-normal text-zinc-900 mb-6">
        Finalizar pedido
      </h1>

      @if (loading()) {
        <div class="space-y-4">
          <div class="h-28 bg-white border border-zinc-200 rounded animate-pulse shadow-sm"></div>
          <div class="h-24 bg-white border border-zinc-200 rounded animate-pulse shadow-sm"></div>
        </div>
      }

      @if (!loading() && error()) {
        <div class="px-4 py-3 bg-red-50 border border-red-200 rounded text-red-600 text-sm mb-6">
          {{ error() }}
        </div>
      }

      @if (!loading() && product()) {
        <div class="bg-white border border-zinc-200 rounded-md p-4 shadow-sm mb-6 flex flex-col gap-4">
          <span class="text-xs font-bold text-zinc-500 uppercase tracking-wider">Resumo do pedido</span>
          
          <div class="flex items-center justify-between gap-4">
            <div>
              <h3 class="text-base font-bold text-zinc-900">{{ product()!.name }}</h3>
              <p class="text-xs text-zinc-500 mt-0.5">Quantidade: {{ quantity }}</p>
            </div>
            <span class="text-xl font-normal text-zinc-900">
              {{ (product()!.price * quantity) | currency:'BRL':'symbol':'1.2-2' }}
            </span>
          </div>
        </div>

        <form [formGroup]="form" (ngSubmit)="submit()" class="flex flex-col gap-5">

          <div class="flex flex-col gap-1.5">
            <label class="text-xs font-bold text-zinc-700 uppercase tracking-wider">
              Método de pagamento
            </label>
            <select formControlName="paymentMethod"
              class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm 
                     focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm cursor-pointer">
              <option value="CREDIT_CARD">Cartão de crédito</option>
              <option value="DEBIT_CARD">Cartão de débito</option>
              <option value="PIX">PIX</option>
              <option value="BOLETO">Boleto bancário</option>
            </select>
          </div>

          @if (submitError()) {
            <div class="px-4 py-2 bg-red-50 border border-red-200 rounded text-red-600 text-sm">
              {{ submitError() }}
            </div>
          }

          <button type="submit" [disabled]="submitting() || form.invalid"
            class="w-full py-2.5 bg-amazon-yellow hover:bg-amber-500 text-zinc-950 font-medium text-sm rounded border border-amber-500 hover:border-amber-600 shadow-sm disabled:opacity-50 disabled:cursor-not-allowed transition-colors mt-2">
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
  product     = signal<ProductResponse | null>(null);
  loading     = signal(true);
  submitting  = signal(false);
  error       = signal('');       
  submitError = signal('');       
  quantity    = 1;
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
    const productId = this.route.snapshot.queryParamMap.get('productId');
    const qtyParam   = this.route.snapshot.queryParamMap.get('quantity');

    if (!productId) {
      this.error.set('Nenhum produto selecionado. Volte ao catálogo e escolha um produto.');
      this.loading.set(false);
      return;
    }

    this.quantity = Number(qtyParam ?? 1);

    this.productService.findById(productId).subscribe({
      next: p => {
        this.product.set(p);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Não foi possível carregar este produto. Volte ao catálogo e tente novamente.');
        this.loading.set(false);
        console.error('Erro ao buscar produto em order-new:', err);
      }
    });
  }

  submit(): void {
    if (this.form.invalid || !this.product()) return;
    this.submitting.set(true);
    this.submitError.set('');

    const token = this.auth.getToken();
    if (!token) {
      this.submitError.set('Sessão expirada. Faça login novamente.');
      this.submitting.set(false);
      this.router.navigate(['/login']);
      return;
    }

    const payload = JSON.parse(atob(token.split('.')[1]));
    console.log('Payload do Token:', payload); 

    this.orderService.create({
      userId: payload.userId,
      paymentMethod: this.form.value.paymentMethod,
      items: [{
        productId: this.product()!.id,
        quantity: this.quantity
      }]
    }).subscribe({
      next: order => this.router.navigate(['/orders', order.id]),
      error: (err) => {
        this.submitError.set('Erro ao criar pedido. Tente novamente.');
        this.submitting.set(false);
        console.error('Erro ao criar pedido:', err);
      }
    });
  }
}