import { Routes } from '@angular/router';
import { authGuard, adminGuard, sellerGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/products/list/product-list.component')
      .then(m => m.ProductListComponent)
  },
  {
    path: 'products',
    loadComponent: () => import('./pages/products/list/product-list.component')
      .then(m => m.ProductListComponent)
  },
  {
    path: 'products/:id',
    loadComponent: () => import('./pages/products/detail/product-detail.component')
      .then(m => m.ProductDetailComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component')
      .then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register.component')
      .then(m => m.RegisterComponent)
  },
  {
    path: 'orders',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/orders/list/order-list.component')
      .then(m => m.OrderListComponent)
  },
  {
    path: 'orders/new',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/orders/new/order-new.component')
      .then(m => m.OrderNewComponent)
  },
  {
    path: 'orders/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/orders/detail/order-detail.component')
      .then(m => m.OrderDetailComponent)
  },
  {
    path: 'admin/products',
    canActivate: [authGuard, sellerGuard],
    loadComponent: () => import('./pages/admin/products/admin-products.component')
      .then(m => m.AdminProductsComponent)
  },
  {
    path: 'admin/categories',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./pages/admin/categories/admin-categories.component')
      .then(m => m.AdminCategoriesComponent)
  },
  { path: '**', redirectTo: '' }
];
