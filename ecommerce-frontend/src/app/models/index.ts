// ─── AUTH ─────────────────────────────────────────────────────
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: 'CUSTOMER' | 'SELLER' | 'ADMIN' | 'MODERATOR';
}

export interface LoginResponse {
  token: string;
}

export interface DecodedToken {
  sub: string;   // email
  role: string;
  jti: string;
  exp: number;
}

// ─── PRODUCTS ─────────────────────────────────────────────────
export interface ProductResponse {
  id: string;
  name: string;
  description: string;
  price: number;
  sku: string;
  availableQuantity: number;
  categoryName: string | null;
  active: boolean;
  updatedAt: string;
}

export interface ProductRequest {
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  sku: string;
  active: boolean;
  categoryId: string;
}

// ─── CATEGORIES ───────────────────────────────────────────────
export interface CategoryResponse {
  id: string;
  name: string;
  description: string;
  createdAt: string;
}

export interface CategoryRequest {
  name: string;
  description: string;
}

// ─── ORDERS ───────────────────────────────────────────────────
export interface OrderItemRequest {
  productId: string;
  quantity: number;
  orderId?: string;
}

export interface OrderRequest {
  userId: string;
  items: OrderItemRequest[];
  PaymentMethod: string;
}

export interface OrderItemResponse {
  productId: string;
  quantity: number;
  priceAtPurchase: number;
}

export interface OrderResponse {
  id: string;
  userId: string;
  totalAmount: number;
  status: 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  createdAt: string;
  items: OrderItemResponse[] | null;
  paymentId: string | null;
}

export interface PaymentResponse {
  id: string;
  paymentMethod: string;
  transactionId: string;
  status: string;
  amount: number;
  processedAt: string;
}

// ─── CART (estado local, não vai ao backend) ──────────────────
export interface CartItem {
  product: ProductResponse;
  quantity: number;
}
