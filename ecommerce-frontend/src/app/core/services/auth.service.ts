import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { LoginRequest, LoginResponse, RegisterRequest, DecodedToken } from '../../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'marketplace_token';

  // signal reativo — componentes reagem automaticamente
  private _token = signal<string | null>(localStorage.getItem(this.TOKEN_KEY));

  isLoggedIn  = computed(() => !!this._token());
  currentRole = computed(() => this.decodeToken()?.role ?? null);
  currentEmail = computed(() => this.decodeToken()?.sub ?? null);

  constructor(private http: HttpClient, private router: Router) {}

  login(data: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', data).pipe(
      tap(res => {
        localStorage.setItem(this.TOKEN_KEY, res.token);
        this._token.set(res.token);
      })
    );
  }

  register(data: RegisterRequest): Observable<void> {
    return this.http.post<void>('/api/auth/register', data);
  }

  logout(): void {
    this.http.post('/api/auth/logout', {}).subscribe({
      complete: () => this.clearSession(),
      error: () => this.clearSession()
    });
  }

  getToken(): string | null {
    return this._token();
  }

  isAdmin(): boolean {
    return this.currentRole() === 'ADMIN';
  }

  isSeller(): boolean {
    return this.currentRole() === 'SELLER' || this.isAdmin();
  }

  private clearSession(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this._token.set(null);
    this.router.navigate(['/login']);
  }

  private decodeToken(): DecodedToken | null {
    const token = this._token();
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(payload)) as DecodedToken;
    } catch {
      return null;
    }
  }
}
