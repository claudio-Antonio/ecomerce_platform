import { Component, computed, OnInit, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router'; // <-- Injetado Router
import { CommonModule } from '@angular/common';
import { AuthService } from './core/services/auth.service';
import { CategoryService } from './core/services/api.services'; // <-- Importado o CategoryService
import { CategoryResponse } from './models/index'; // <-- Importado o Model de categoria

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  template: `
    <div class="min-h-screen bg-zinc-950 text-zinc-100 font-body">

      <nav class="fixed top-0 inset-x-0 z-50 border-b border-zinc-800/60 bg-zinc-950/80 backdrop-blur-md">
        <div class="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">

          <a routerLink="/" class="flex items-center gap-2 group">
            <span class="text-2xl font-display font-black tracking-tighter text-white">
              market<span class="text-amber-400">hub</span>
            </span>
          </a>

          <div class="flex items-center gap-1">
            
            <div class="flex items-center gap-1 border-r border-zinc-800/60 pr-2 mr-2">
              <button (click)="filtrarPorCategoria()"
                 class="px-3 py-2 text-xs uppercase font-medium tracking-wider text-zinc-500 hover:text-white transition-colors rounded-lg hover:bg-zinc-900">
                Todos
              </button>

              @for (cat of categories(); track cat.id) {
                <button (click)="filtrarPorCategoria(cat.id)"
                   class="px-3 py-2 text-xs uppercase font-medium tracking-wider text-zinc-400 hover:text-white transition-colors rounded-lg hover:bg-zinc-900">
                  {{ cat.name }}
                </button>
              }
            </div>

            <a routerLink="/products"
               routerLinkActive="text-amber-400"
               class="px-4 py-2 text-sm text-zinc-400 hover:text-white transition-colors rounded-lg hover:bg-zinc-800">
              Produtos
            </a>

            @if (auth.isLoggedIn()) {
              <a routerLink="/orders"
                 routerLinkActive="text-amber-400"
                 class="px-4 py-2 text-sm text-zinc-400 hover:text-white transition-colors rounded-lg hover:bg-zinc-800">
                Pedidos
              </a>

              @if (auth.isSeller()) {
                <a routerLink="/admin/products"
                   routerLinkActive="text-amber-400"
                   class="px-4 py-2 text-sm text-zinc-400 hover:text-white transition-colors rounded-lg hover:bg-zinc-800">
                  Gerenciar
                </a>
              }

              <div class="flex items-center gap-2 ml-4 pl-4 border-l border-zinc-800">
                <span class="text-xs text-zinc-500">{{ auth.currentEmail() }}</span>
                <button (click)="auth.logout()"
                  class="px-3 py-1.5 text-xs text-zinc-400 border border-zinc-700 rounded-lg hover:border-red-500 hover:text-red-400 transition-all">
                  Sair
                </button>
              </div>
            } @else {
              <div class="flex items-center gap-2 ml-4">
                <a routerLink="/login"
                   class="px-4 py-2 text-sm text-zinc-400 hover:text-white transition-colors">
                  Entrar
                </a>
                <a routerLink="/register"
                   class="px-4 py-2 text-sm bg-amber-400 text-zinc-950 font-semibold rounded-lg hover:bg-amber-300 transition-colors">
                  Cadastrar
                </a>
              </div>
            }
          </div>
        </div>
      </nav>

      <main class="pt-16">
        <router-outlet />
      </main>
    </div>
  `
})
export class AppComponent implements OnInit {
  // Signal para salvar dinamicamente as categorias encontradas
  categories = signal<CategoryResponse[]>([]);

  constructor(
    public auth: AuthService,
    private categoryService: CategoryService, // Injetado
    private router: Router // Injetado
  ) {}

  ngOnInit(): void {
    // Busca as categorias do banco de dados ao iniciar o site
    this.categoryService.findAll().subscribe({
      next: (data) => this.categories.set(data),
      error: (err) => console.error('Erro ao listar categorias na navbar', err)
    });
  }

  // Altera a URL adicionando ou removendo o queryParam categoryId
  filtrarPorCategoria(id?: string) {
    this.router.navigate(['/products'], {
      queryParams: { categoryId: id },
      queryParamsHandling: 'merge'
    });
  }
}