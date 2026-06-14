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
    <div class="min-h-[calc(100vh-4rem)] flex items-center justify-center px-4">
      <div class="w-full max-w-sm">

        <div class="mb-10 text-center">
          <h1 class="text-3xl font-display font-black text-white mb-2">Bem-vindo de volta</h1>
          <p class="text-zinc-500 text-sm">Entre na sua conta para continuar</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4">

          <div>
            <label class="block text-xs font-medium text-zinc-400 mb-1.5 uppercase tracking-wider">Email</label>
            <input formControlName="email" type="email" placeholder="seu@email.com"
              class="w-full px-4 py-3 bg-zinc-900 border border-zinc-800 rounded-xl text-white placeholder-zinc-600
                     focus:outline-none focus:border-amber-400 focus:ring-1 focus:ring-amber-400/30 transition-all text-sm" />
          </div>

          <div>
            <label class="block text-xs font-medium text-zinc-400 mb-1.5 uppercase tracking-wider">Senha</label>
            <input formControlName="password" type="password" placeholder="••••••••"
              class="w-full px-4 py-3 bg-zinc-900 border border-zinc-800 rounded-xl text-white placeholder-zinc-600
                     focus:outline-none focus:border-amber-400 focus:ring-1 focus:ring-amber-400/30 transition-all text-sm" />
          </div>

          @if (error) {
            <div class="px-4 py-3 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-sm">
              {{ error }}
            </div>
          }

          <button type="submit" [disabled]="loading || form.invalid"
            class="w-full py-3 bg-amber-400 text-zinc-950 font-bold rounded-xl hover:bg-amber-300
                   transition-all disabled:opacity-50 disabled:cursor-not-allowed text-sm tracking-wide">
            @if (loading) { Entrando... } @else { Entrar }
          </button>

        </form>

        <p class="mt-6 text-center text-sm text-zinc-500">
          Não tem conta?
          <a routerLink="/register" class="text-amber-400 hover:text-amber-300 font-medium ml-1">Cadastre-se</a>
        </p>
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
