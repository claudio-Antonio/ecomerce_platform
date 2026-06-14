import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../../core/services/api.services';
import { OrderResponse } from '../../../models/index';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="max-w-4xl mx-auto px-6 py-12">

      <div class="flex items-center justify-between mb-10">
        <div>
          <h1 class="text-4xl font-display font-black text-white tracking-tight mb-1">
            Meus pedidos
          </h1>
          <p class="text-zinc-500 text-sm">{{ orders().length }} pedido(s) no total</p>
        </div>
        <a routerLink="/products"
           class="px-4 py-2 text-sm bg-amber-400 text-zinc-950 font-bold rounded-xl
                  hover:bg-amber-300 transition-all">
          + Novo pedido
        </a>
      </div>

      @if (loading()) {
        <div class="space-y-4">
          @for (n of [1,2,3]; track n) {
            <div class="h-24 bg-zinc-900 rounded-2xl animate-pulse border border-zinc-800"></div>
          }
        </div>
      }

      @if (!loading() && orders().length === 0) {
        <div class="text-center py-24">
          <p class="text-zinc-600 text-lg mb-4">Você ainda não fez nenhum pedido</p>
          <a routerLink="/products"
             class="px-6 py-3 bg-amber-400 text-zinc-950 font-bold rounded-xl hover:bg-amber-300 transition-all text-sm">
            Ver produtos
          </a>
        </div>
      }

      @if (!loading() && orders().length > 0) {
        <div class="space-y-3">
          @for (order of orders(); track order.id) {
            <a [routerLink]="['/orders', order.id]"
               class="block bg-zinc-900 border border-zinc-800 rounded-2xl p-5
                      hover:border-amber-400/40 hover:bg-zinc-800/50 transition-all">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-4">
                  <!-- STATUS BADGE -->
                  <span class="text-xs font-semibold px-3 py-1.5 rounded-lg border"
                    [class]="statusClass(order.status)">
                    {{ statusLabel(order.status) }}
                  </span>
                  <div>
                    <p class="text-white font-medium text-sm">
                      Pedido #{{ order.id.slice(0, 8).toUpperCase() }}
                    </p>
                    <p class="text-zinc-600 text-xs mt-0.5">
                      {{ order.createdAt | date:'dd/MM/yyyy HH:mm' }}
                    </p>
                  </div>
                </div>
                <span class="text-lg font-display font-black text-white">
                  {{ order.totalAmount | currency:'BRL':'symbol':'1.2-2' }}
                </span>
              </div>
            </a>
          }
        </div>
      }
    </div>
  `
})
export class OrderListComponent implements OnInit {
  orders  = signal<OrderResponse[]>([]);
  loading = signal(true);

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.orderService.findAll().subscribe({
      next: data => { this.orders.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING:   'Pendente',
      CONFIRMED: 'Confirmado',
      SHIPPED:   'Enviado',
      DELIVERED: 'Entregue',
      CANCELLED: 'Cancelado'
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
