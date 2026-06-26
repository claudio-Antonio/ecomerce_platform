import { Component, computed, OnInit, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router'; 
import { CommonModule } from '@angular/common';
import { AuthService } from './core/services/auth.service';
import { CategoryService } from './core/services/api.services'; 
import { CategoryResponse } from './models/index'; 

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  template: `
    <div class="min-h-screen bg-amazon-graybg text-zinc-800 font-body">

      <nav class="fixed top-0 inset-x-0 z-50 bg-amazon-darkblue text-white shadow-md">
        <div class="max-w-7xl mx-auto px-4 h-14 flex items-center justify-between gap-4">

          <a routerLink="/" class="flex items-center p-2 border border-transparent hover:border-white rounded-sm transition-all">
            <span class="text-xl font-display font-black tracking-tight text-white">
              market<span class="text-amazon-yellow">hub</span>
            </span>
          </a>

          <div class="flex items-center gap-4">
            <a routerLink="/products"
               routerLinkActive="text-amazon-yellow font-medium"
               class="px-3 py-2 text-sm text-zinc-200 hover:text-white border border-transparent hover:border-white rounded-sm transition-all">
              Produtos
            </a>

            @if (auth.isLoggedIn()) {
              <a routerLink="/orders"
                 routerLinkActive="text-amazon-yellow font-medium"
                 class="px-3 py-2 text-sm text-zinc-200 hover:text-white border border-transparent hover:border-white rounded-sm transition-all">
                Pedidos
              </a>

              @if (auth.isSeller()) {
                <a routerLink="/admin/products"
                   routerLinkActive="text-amazon-yellow font-medium"
                   class="px-3 py-2 text-sm text-zinc-200 hover:text-white border border-transparent hover:border-white rounded-sm transition-all">
                  Gerenciar
                </a>
              }

              <div class="flex items-center gap-3 pl-3 border-l border-zinc-700">
                <div class="flex flex-col text-left">
                  <span class="text-[10px] text-zinc-400 leading-none">Olá,</span>
                  <span class="text-xs font-bold leading-tight">{{ auth.currentEmail() }}</span>
                </div>
                <button (click)="auth.logout()"
                  class="px-2 py-1 text-xs text-zinc-300 border border-zinc-600 rounded hover:text-white hover:border-red-400 transition-all">
                  Sair
                </button>
              </div>
            } @else {
              <div class="flex items-center gap-2">
                <a routerLink="/login"
                   class="px-3 py-2 text-sm text-zinc-200 hover:text-white border border-transparent hover:border-white rounded-sm transition-all">
                  Olá, Faça seu login
                </a>
                <a routerLink="/register"
                   class="px-4 py-1.5 text-sm bg-amazon-yellow text-zinc-950 font-medium rounded-md hover:bg-amber-500 shadow-sm transition-colors">
                  Cadastrar
                </a>
              </div>
            }
          </div>
        </div>

        <div class="bg-amazon-navbelw px-4 py-1.5 flex items-center gap-2 overflow-x-auto text-xs text-white">
          <span class="font-bold px-2 py-1 border border-transparent hover:border-white rounded-sm cursor-pointer">
            ☰ Categorias:
          </span>
          
          <button (click)="filtrarPorCategoria()"
             class="px-2 py-1 border border-transparent hover:border-white rounded-sm transition-all text-zinc-200 hover:text-white">
            Todos
          </button>

          @for (cat of categories(); track cat.id) {
            <button (click)="filtrarPorCategoria(cat.id)"
               class="px-2 py-1 border border-transparent hover:border-white rounded-sm transition-all text-zinc-200 hover:text-white whitespace-nowrap">
              {{ cat.name }}
            </button>
          }
        </div>
      </nav>

      <main class="pt-24 min-h-screen">
        <router-outlet />
      </main>
    </div>
  `
})
export class AppComponent implements OnInit {
  categories = signal<CategoryResponse[]>([]);

  constructor(
    public auth: AuthService,
    private categoryService: CategoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.categoryService.findAll().subscribe({
      next: (data) => this.categories.set(data),
      error: (err) => console.error('Erro ao listar categorias na navbar', err)
    });
  }

  filtrarPorCategoria(id?: string) {
    this.router.navigate(['/products'], {
      queryParams: { categoryId: id },
      queryParamsHandling: 'merge'
    });
  }
}