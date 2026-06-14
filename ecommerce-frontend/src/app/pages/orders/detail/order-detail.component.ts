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
    <div class="max-w-2xl mx-auto px-6 py-12">

      <a routerLink="/orders"
         class="inline-flex items-center gap-2 text-zinc-500 hover:text-white text-sm mb-8 transition-colors">
        ← Meus pedidos
      </a>

      @if (loading()) {
        <div class="space-y-4">
          <div class="h-16 bg-zinc-900 rounded-2xl animate-pulse border border-zinc-800"></div>
          <div class="h-40 bg-zinc-900 rounded-2xl animate-pulse border border-zinc-800"></div>
        </div>
      }

      @if (!loading() && order()) {
        <!-- HEADER -->
        <div class="flex items-start justify-between mb-8">
          <div>
            <h1 class="text-2xl font-display font-black text-white tracking-tight">
              Pedido #{{ order()!.id.slice(0, 8).toUpperCase() }}
            </h1>
            <p class="text-zinc-500 text-sm mt-1">
              {{ order()!.createdAt | date:'dd/MM/yyyy - HH:mm' }}
            </p>
          </div>
          <span class="text-xs font-semibold px-3 py-1.5 rounded-lg border"
            [class]="statusClass(order()!.status)">
            {{ statusLabel(order()!.status) }}
          </span>
        </div>

        <!-- VALOR TOTAL -->
        <div class="bg-zinc-900 border border-zinc-800 rounded-2xl p-6 mb-4">
          <div class="flex items-center justify-between">
            <span class="text-zinc-400 text-sm">Total do pedido</span>
            <span class="text-2xl font-display font-black text-amber-400">
              {{ order()!.totalAmount | currency:'BRL':'symbol':'1.2-2' }}
            </span>
          </div>
        </div>

        <!-- PAGAMENTO -->
        @if (payment()) {
          <div class="bg-zinc-900 border border-zinc-800 rounded-2xl p-6 mb-4">
            <h2 class="text-xs font-medium text-zinc-500 uppercase tracking-wider mb-4">Pagamento</h2>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-zinc-500">Método</span>
                <span class="text-white">{{ payment()!.paymentMethod }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-zinc-500">Status</span>
                <span class="text-white">{{ payment()!.status }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-zinc-500">ID da transação</span>
                <span class="text-white font-mono text-xs">{{ payment()!.transactionId }}</span>
              </div>
            </div>
          </div>
        }

        <!-- ITENS -->
        @if (order()!.items && order()!.items!.length > 0) {
          <div class="bg-zinc-900 border border-zinc-800 rounded-2xl p-6">
            <h2 class="text-xs font-medium text-zinc-500 uppercase tracking-wider mb-4">Itens</h2>
            <div class="space-y-3">
              @for (item of order()!.items; track item.productId) {
                <div class="flex items-center justify-between py-2 border-b border-zinc-800 last:border-0">
                  <div>
                    <p class="text-white text-sm font-mono">{{ item.productId.slice(0,8) }}...</p>
                    <p class="text-zinc-500 text-xs">Qtd: {{ item.quantity }}</p>
                  </div>
                  <span class="text-white font-semibold text-sm">
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
      PENDING:   'bg-yellow-500/10 text-yellow-400 border-yellow-500/20',
      CONFIRMED: 'bg-blue-500/10 text-blue-400 border-blue-500/20',
      SHIPPED:   'bg-purple-500/10 text-purple-400 border-purple-500/20',
      DELIVERED: 'bg-green-500/10 text-green-400 border-green-500/20',
      CANCELLED: 'bg-red-500/10 text-red-400 border-red-500/20'
    };
    return map[status] ?? 'bg-zinc-800 text-zinc-400 border-zinc-700';
  }
}
