import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ProductResponse, ProductRequest,
  CategoryResponse, CategoryRequest,
  OrderResponse, OrderRequest,
  PaymentResponse
} from '../../models';

// ─── PRODUCT SERVICE ──────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class ProductService {
  constructor(private http: HttpClient) {}

  findAll(): Observable<ProductResponse[]> {
    return this.http.get<ProductResponse[]>('/api/products');
  }

  findById(id: string): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`/api/products/${id}`);
  }

  create(data: ProductRequest): Observable<ProductResponse> {
    return this.http.post<ProductResponse>('/api/products', data);
  }

  update(id: string, data: ProductRequest): Observable<ProductResponse> {
    return this.http.put<ProductResponse>(`/api/products/${id}`, data);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`/api/products/${id}`);
  }
}

// ─── CATEGORY SERVICE ─────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class CategoryService {
  constructor(private http: HttpClient) {}

  findAll(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>('/api/categories');
  }

  findById(id: string): Observable<CategoryResponse> {
    return this.http.get<CategoryResponse>(`/api/categories/${id}`);
  }

  create(data: CategoryRequest): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>('/api/categories', data);
  }

  update(id: string, data: CategoryRequest): Observable<CategoryResponse> {
    return this.http.put<CategoryResponse>(`/api/categories/${id}`, data);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`/api/categories?id=${id}`);
  }
}

// ─── ORDER SERVICE ────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class OrderService {
  constructor(private http: HttpClient) {}

  findAll(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>('/api/orders');
  }

  findById(id: string): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`/api/orders/${id}`);
  }

  create(data: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>('/api/orders', data);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`/api/orders/${id}`);
  }

  getPayment(id: string): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(`/api/payments/${id}`);
  }
}
