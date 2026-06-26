import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  template: `
    <div class="max-w-md mx-auto my-16 p-6 bg-white border border-zinc-200 rounded-md shadow-sm">
      
      <div class="text-center mb-6">
        <h1 class="text-2xl font-normal text-zinc-900 mb-1">
          Fazer login
        </h1>
        <p class="text-zinc-600 text-sm">Entre na sua conta para continuar</p>
      </div>

      <form [formGroup]="form" (ngSubmit)="submit()" class="flex flex-col gap-4">
        
        <div class="flex flex-col gap-1">
          <label class="text-xs font-bold text-zinc-800">E-mail</label>
          <input formControlName="email" type="email" placeholder="seu@email.com"
            class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm
                   focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm" />
        </div>

        <div class="flex flex-col gap-1">
          <label class="text-xs font-bold text-zinc-800">Senha</label>
          <input formControlName="password" type="password" placeholder="••••••••"
            class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm
                   focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm" />
        </div>

        @if (error) {
          <div class="px-4 py-2 bg-red-50 border border-red-200 rounded text-red-600 text-sm">
            {{ error }}
          </div>
        }

        <button type="submit" [disabled]="loading || form.invalid"
          class="w-full mt-2 py-1.5 bg-amazon-yellow hover:bg-amber-500 text-zinc-950 font-medium text-sm rounded border border-amber-500 hover:border-amber-600 shadow-sm disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
          @if (loading) { Entrando... } @else { Entrar }
        </button>

      </form>

      <div class="mt-6 pt-4 border-t border-zinc-200 text-center text-xs text-zinc-600">
        Novo no markethub? 
        <a routerLink="/register" class="text-blue-600 hover:text-amber-600 hover:underline ml-1">
          Cadastre-se
        </a>
      </div>

    </div>
  `
})
export class LoginComponent {
  form: FormGroup;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email:    ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';

    this.auth.login(this.form.value).subscribe({
      next: () => this.router.navigate(['/products']),
      error: () => {
        this.error = 'Email ou senha incorretos.';
        this.loading = false;
      }
    });
  }
}