import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // rotas públicas não precisam de token
  const publicRoutes = ['/api/auth/login', '/api/auth/register', '/api/products'];
  const isPublic = publicRoutes.some(route => req.url.includes(route));

  if (token && !isPublic) {
    const authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(authReq);
  }

  return next(req);
};
