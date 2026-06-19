import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Rotas onde o método GET é 100% público (não precisa de token)
  const isPublicGetProducts = req.url.includes('/api/products') && req.method === 'GET';
  const isPublicGetCategories = req.url.includes('/api/categories') && req.method === 'GET'; // <-- ADICIONEI ESTA LINHA
  
  // Rotas de autenticação (Login/Cadastro) sempre públicas
  const isAuthRoute = req.url.includes('/api/auth/login') || req.url.includes('/api/auth/register');

  // Une todas as condições públicas
  const isPublic = isPublicGetProducts || isPublicGetCategories || isAuthRoute;

  // Se NÃO for público e tivermos um token salvo, nós o injetamos no cabeçalho
  if (token && !isPublic) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(authReq);
  }

  return next(req);
};