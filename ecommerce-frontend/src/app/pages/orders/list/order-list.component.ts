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
    <div class="max-w-3xl mx-auto px-4 py-8">

      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
        <div>
          <h1 class="text-2xl font-normal text-zinc-900 tracking-tight">
            Meus pedidos
          </h1>
          <p class="text-xs text-zinc-500 mt-0.5">{{ orders().length }} pedido(s) no total</p>
        </div>
        <a routerLink="/products"
           class="px-4 py-1.5 bg-amber-400 hover:bg-amber-500 text-zinc-950 font-medium text-sm rounded border border-amber-500 shadow-sm transition-colors text-center">
          + Novo pedido
        </a>
      </div>

      @if (loading()) {
        <div class="space-y-3">
          @for (n of [1,2,3]; track n) {
            <div class="h-16 bg-white border border-zinc-200 rounded-md animate-pulse shadow-sm"></div>
          }
        </div>
      }

      @if (!loading() && orders().length === 0) {
        <div class="text-center py-16 bg-white border border-zinc-200 rounded-md shadow-sm">
          <p class="text-zinc-500 text-sm mb-4">Você ainda não fez nenhum pedido</p>
          <a routerLink="/products"
             class="px-4 py-2 bg-amber-400 hover:bg-amber-500 text-zinc-950 font-medium rounded border border-amber-500 text-sm shadow-sm transition-colors">
            Ver produtos
          </a>
        </div>
      }

      @if (!loading() && orders().length > 0) {
        <div class="flex flex-col gap-3">
          @for (order of orders(); track order.id) {
            <a [routerLink]="['/orders', order.id]"
               class="block bg-white border border-zinc-200 rounded-md p-4 shadow-sm
                      hover:border-zinc-300 transition-colors">
              <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                
                <div class="flex items-center gap-4">
                  <span class="text-xs font-medium px-2.5 py-1 rounded border min-w-[95px] text-center shadow-xs"
                    [class]="statusClass(order.status)">
                    {{ statusLabel(order.status) }}
                  </span>
                  <div>
                    <p class="text-zinc-900 font-bold text-sm">
                      Pedido #{{ order.id.slice(0, 8).toUpperCase() }}
                    </p>
                    <p class="text-zinc-400 text-xs mt-0.5">
                      {{ order.createdAt | date:'dd/MM/yyyy HH:mm' }}
                    </p>
                  </div>
                </div>

                <div class="flex items-center justify-between sm:justify-end gap-4 w-full sm:w-auto border-t sm:border-0 border-zinc-100 pt-2 sm:pt-0">
                  <span class="text-base font-normal text-zinc-900">
                    {{ order.totalAmount | currency:'BRL':'symbol':'1.2-2' }}
                  </span>
                  <span class="text-xs font-medium text-blue-600 hover:underline hidden sm:inline">
                    Ver detalhes
                  </span>
                </div>

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
      PENDING:   'bg-amber-50 text-amber-700 border-amber-200',
      CONFIRMED: 'bg-green-50 text-green-700 border-green-200',
      SHIPPED:   'bg-purple-50 text-purple-700 border-purple-200',
      DELIVERED: 'bg-blue-50 text-blue-700 border-blue-200',
      CANCELLED: 'bg-red-50 text-red-700 border-red-200'
    };
    return map[status] ?? 'bg-zinc-50 text-zinc-600 border-zinc-200';
  }
}