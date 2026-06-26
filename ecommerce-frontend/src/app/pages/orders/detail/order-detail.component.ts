import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrderService } from '../../../core/services/api.services';
import { OrderResponse, PaymentResponse } from '../../../models/index';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="max-w-2xl mx-auto px-4 py-8">

      <div class="mb-6">
        <a routerLink="/orders"
           class="inline-flex items-center gap-2 text-sm text-blue-600 hover:text-amber-600 hover:underline transition-colors">
          ← Meus pedidos
        </a>
      </div>

      @if (loading()) {
        <div class="space-y-4">
          <div class="h-16 bg-white border border-zinc-200 rounded-md animate-pulse shadow-sm"></div>
          <div class="h-40 bg-white border border-zinc-200 rounded-md animate-pulse shadow-sm"></div>
        </div>
      }

      @if (!loading() && order()) {
        <div class="flex items-start justify-between gap-4 mb-6">
          <div>
            <h1 class="text-2xl font-normal text-zinc-900 tracking-tight">
              Pedido #{{ order()!.id.slice(0, 8).toUpperCase() }}
            </h1>
            <p class="text-xs text-zinc-500 mt-1">
              {{ order()!.createdAt | date:'dd/MM/yyyy - HH:mm' }}
            </p>
          </div>
          <span class="text-xs font-medium px-2.5 py-1 rounded border shadow-sm"
            [class]="statusClass(order()!.status)">
            {{ statusLabel(order()!.status) }}
          </span>
        </div>

        <div class="bg-white border border-zinc-200 rounded-md p-4 shadow-sm mb-4">
          <div class="flex items-center justify-between">
            <span class="text-zinc-600 text-sm font-medium">Total do pedido</span>
            <span class="text-xl font-bold text-zinc-900">
              {{ order()!.totalAmount | currency:'BRL':'symbol':'1.2-2' }}
            </span>
          </div>
        </div>

        @if (payment()) {
          <div class="bg-white border border-zinc-200 rounded-md p-5 shadow-sm mb-4">
            <h2 class="text-xs font-bold text-zinc-400 uppercase tracking-wider mb-4 border-b border-zinc-100 pb-2">
              Informações de Pagamento
            </h2>
            <div class="space-y-3 text-sm">
              <div class="flex justify-between items-center border-b border-zinc-50 pb-2 last:border-0 last:pb-0">
                <span class="text-zinc-500">Método</span>
                <span class="font-medium text-zinc-800">{{ payment()!.paymentMethod }}</span>
              </div>
              <div class="flex justify-between items-center border-b border-zinc-50 pb-2 last:border-0 last:pb-0">
                <span class="text-zinc-500">Status</span>
                <span class="font-medium text-zinc-800 flex items-center gap-1.5">
                  <span class="w-2 h-2 rounded-full" 
                        [ngClass]="{'bg-amber-500': payment()!.status === 'PENDING', 'bg-green-500': payment()!.status === 'CONFIRMED'}"></span>
                  {{ payment()!.status }}
                </span>
              </div>
              <div class="flex flex-col sm:flex-row sm:justify-between gap-1 pt-1">
                <span class="text-zinc-500">ID da transação</span>
                <span class="font-mono text-xs text-zinc-600 bg-zinc-50 px-2 py-1 rounded border border-zinc-200 break-all select-all">
                  {{ payment()!.transactionId }}
                </span>
              </div>
            </div>
          </div>
        }

        @if (order()!.items && order()!.items!.length > 0) {
          <div class="bg-white border border-zinc-200 rounded-md p-5 shadow-sm">
            <h2 class="text-xs font-bold text-zinc-400 uppercase tracking-wider mb-4 border-b border-zinc-100 pb-2">
              Itens do Pedido
            </h2>
            <div class="divide-y divide-zinc-100">
              @for (item of order()!.items; track item.productId) {
                <div class="flex items-center justify-between py-3 first:pt-0 last:pb-0">
                  <div>
                    <p class="text-sm font-medium text-zinc-900">Produto ID: {{ item.productId.slice(0,8).toUpperCase() }}...</p>
                    <p class="text-xs text-zinc-500 mt-0.5">Quantidade: {{ item.quantity }}</p>
                  </div>
                  <span class="text-sm font-bold text-zinc-900">
                    {{ item.priceAtPurchase | currency:'BRL':'symbol':'1.2-2' }}
                  </span>
                </div>
              }
            </div>
          </div>
        }
      }
    </div>
  `
})
export class OrderDetailComponent implements OnInit {
  order   = signal<OrderResponse | null>(null);
  payment = signal<PaymentResponse | null>(null);
  loading = signal(true);

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.orderService.findById(id).subscribe({
      next: o => {
        this.order.set(o);
        if (o.paymentId) {
          this.orderService.getPayment(o.paymentId).subscribe({
            next: p => this.payment.set(p)
          });
        }
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'Pendente', CONFIRMED: 'Confirmado',
      SHIPPED: 'Enviado', DELIVERED: 'Entregue', CANCELLED: 'Cancelado'
    };
    return map[status] ?? status;
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      PENDING:   'bg-amber-50 text-amber-700 border-amber-200',
      CONFIRMED: 'bg-green-50 text-green-700 border-green-200',
      SHIPPED:   'bg-purple-50 text-purple-700 border-purple-200',
      DELIVERED: 'bg-blue-50 text-blue-700 border-blue-200',
      CANCELLED: 'bg-red-50 text-red-700 border-red-200'
    };
    return map[status] ?? 'bg-zinc-50 text-zinc-600 border-zinc-200';
  }
}