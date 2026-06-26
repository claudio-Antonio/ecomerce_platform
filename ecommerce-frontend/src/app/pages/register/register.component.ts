import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  template: `
    <div class="min-h-[calc(100vh-4rem)] flex items-center justify-center px-4 py-12">
      <div class="w-full max-w-md bg-white border border-zinc-200 rounded-md p-6 shadow-sm">

        <div class="mb-6 text-center">
          <h1 class="text-2xl font-normal text-zinc-900 mb-1">
            Criar conta
          </h1>
          <p class="text-zinc-600 text-sm">Registe-se no markethub para começar</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="submit()" class="flex flex-col gap-4">

          <div class="flex flex-col gap-1">
            <label class="text-xs font-bold text-zinc-800">Nome completo</label>
            <input formControlName="name" type="text" placeholder="Seu nome"
              class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm
                     focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-bold text-zinc-800">E-mail</label>
            <input formControlName="email" type="email" placeholder="seu@email.com"
              class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm
                     focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-bold text-zinc-800">Palavra-passe</label>
            <input formControlName="password" type="password" placeholder="No mínimo 6 caracteres"
              class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm
                     focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-bold text-zinc-800">Tipo de conta</label>
            <select formControlName="role"
              class="w-full px-3 py-2 bg-white border border-zinc-400 rounded text-zinc-900 text-sm
                     focus:outline-none focus:border-amazon-yellow focus:ring-1 focus:ring-amazon-yellow shadow-sm cursor-pointer">
              <option value="CUSTOMER">Cliente padrão (CUSTOMER)</option>
              <option value="ADMIN">Administrador de Catálogo (ADMIN)</option>
            </select>
          </div>

          @if (error) {
            <div class="px-4 py-2 bg-red-50 border border-red-200 rounded text-red-600 text-sm">
              {{ error }}
            </div>
          }

          @if (success) {
            <div class="px-4 py-2 bg-green-50 border border-green-200 rounded text-green-700 text-sm">
              Conta criada com sucesso! Redirecionando...
            </div>
          }

          <button type="submit" [disabled]="loading || form.invalid"
            class="w-full mt-2 py-1.5 bg-amazon-yellow hover:bg-amber-500 text-zinc-950 font-medium text-sm rounded border border-amber-500 hover:border-amber-600 shadow-sm disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
            @if (loading) { Criando conta... } @else { Criar conta }
          </button>

        </form>

        <div class="mt-6 pt-4 border-t border-zinc-200 text-center text-xs text-zinc-600">
          Já tem uma conta? 
          <a routerLink="/login" class="text-blue-600 hover:text-amber-600 hover:underline ml-1">
            Fazer login
          </a>
        </div>
      </div>
    </div>
  `
})
export class RegisterComponent {
  form: FormGroup;
  loading = false;
  error = '';
  success = false;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      name:     ['', Validators.required],
      email:    ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role:     ['CUSTOMER', Validators.required]
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.error = '';

    this.auth.register(this.form.value).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: err => {
        this.error = err.error?.message || 'Erro ao criar conta.';
        this.loading = false;
      }
    });
  }
}