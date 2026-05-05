import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Chat } from '../../../shared/chat/chat';

interface Product {
  id: number;
  name: string;
  sku?: string;
  unitPrice?: number;
  discountedPrice?: number;
  stock?: number;
  rating?: number;
  category?: { id: number; name: string };
  store?: { id: number; name: string };
}

interface Order {
  id: number;
  status: string;
  grandTotal?: number;
  paymentMethod?: string;
  createdAt?: string;
  store?: { id: number; name: string };
}

interface Review {
  id?: number;
  starRating?: number;
  sentiment?: string;
  reviewTitle?: string;
  reviewText?: string;
  product?: { id: number; name: string };
  user?: { id: number; email: string };
}

interface CartItem {
  product: Product;
  quantity: number;
}

@Component({
  standalone: true,
  selector: 'app-individual',
  imports: [CommonModule, FormsModule, Chat],
  templateUrl: './individual.html',
  styleUrl: './individual.css',
})
export class IndividualComponent implements OnInit {
  activeTab = 'dashboard';

  products: Product[] = [];
  filteredProducts: Product[] = [];
  orders: Order[] = [];
  myReviews: Review[] = [];
  cart: CartItem[] = [];

  searchQuery = '';
  totalSpent = 0;

  showReviewForm = false;
  reviewForm: Review = { starRating: 5, reviewTitle: '', reviewText: '' };
  reviewProductId: number | null = null;
  reviewProductName = '';

  showCheckout = false;
  checkoutForm = {
    paymentMethod: 'credit_card',
    cardNumber: '',
    cardExpMonth: '',
    cardExpYear: '',
    cardCvv: ''
  };

  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.loadProducts();
    this.loadOrders();
    this.loadMyReviews();
  }

  getHeaders() {
    const token = localStorage.getItem('token');
    return { headers: new HttpHeaders({ Authorization: `Bearer ${token}` }) };
  }

  getUserId(): number {
    return Number(localStorage.getItem('id'));
  }

  loadProducts() {
    this.http.get<Product[]>(`${this.apiUrl}/products`).subscribe({
      next: (data) => { this.products = data; this.filteredProducts = data; },
      error: () => {}
    });
  }

  onSearch() {
    const q = this.searchQuery.toLowerCase().trim();
    this.filteredProducts = q
      ? this.products.filter(p => p.name.toLowerCase().includes(q))
      : this.products;
  }

  addToCart(product: Product) {
    const existing = this.cart.find(c => c.product.id === product.id);
    if (existing) {
      existing.quantity++;
    } else {
      this.cart.push({ product, quantity: 1 });
    }
  }

  removeFromCart(productId: number) {
    this.cart = this.cart.filter(c => c.product.id !== productId);
  }

  getCartTotal(): number {
    return this.cart.reduce((sum, item) => {
      const price = item.product.discountedPrice ?? item.product.unitPrice ?? 0;
      return sum + price * item.quantity;
    }, 0);
  }

  getCartCount(): number {
    return this.cart.reduce((sum, item) => sum + item.quantity, 0);
  }

  placeOrder() {
    if (this.cart.length === 0) return;

    if (this.checkoutForm.paymentMethod === 'credit_card' || this.checkoutForm.paymentMethod === 'debit_card') {
      if (!this.checkoutForm.cardNumber || this.checkoutForm.cardNumber.length < 16) {
        alert('Geçerli bir kart numarası girin! (16 hane)');
        return;
      }
      if (!this.checkoutForm.cardExpMonth || !this.checkoutForm.cardExpYear) {
        alert('Son kullanım tarihini girin!');
        return;
      }
      if (!this.checkoutForm.cardCvv || this.checkoutForm.cardCvv.length < 3) {
        alert('CVV kodunu girin!');
        return;
      }
    }

    const userId = this.getUserId();
    const storeId = this.cart[0].product.store?.id ?? 1;
    const order = {
      user: { id: userId },
      store: { id: storeId },
      status: 'pending',
      paymentMethod: this.checkoutForm.paymentMethod,
      grandTotal: this.getCartTotal()
    };

    this.http.post<Order>(`${this.apiUrl}/orders`, order, this.getHeaders()).subscribe({
      next: (createdOrder) => {
        this.orders.unshift(createdOrder);
        this.cart = [];
        this.showCheckout = false;
        this.checkoutForm = { paymentMethod: 'credit_card', cardNumber: '', cardExpMonth: '', cardExpYear: '', cardCvv: '' };
        this.totalSpent += createdOrder.grandTotal ?? 0;
        alert('Siparişiniz başarıyla oluşturuldu!');
        this.setTab('orders');
      },
      error: () => alert('Sipariş oluşturulamadı.')
    });
  }

  loadOrders() {
    const userId = this.getUserId();
    this.http.get<Order[]>(`${this.apiUrl}/orders/user/${userId}`, this.getHeaders()).subscribe({
      next: (data) => {
        this.orders = data;
        this.totalSpent = data.reduce((sum, o) => sum + (o.grandTotal ?? 0), 0);
      },
      error: () => {}
    });
  }

  cancelOrder(order: Order) {
    if (!confirm('Siparişi iptal etmek istiyor musunuz?')) return;
    this.http.put(`${this.apiUrl}/orders/${order.id}/refund`, {}, this.getHeaders()).subscribe({
      next: () => order.status = 'cancelled',
      error: () => alert('Sipariş iptal edilemedi.')
    });
  }

  loadMyReviews() {
    const userId = this.getUserId();
    this.http.get<Review[]>(`${this.apiUrl}/reviews/user/${userId}`, this.getHeaders()).subscribe({
      next: (data) => this.myReviews = data,
      error: () => {}
    });
  }

  openReviewForm(productId: number, productName: string) {
    this.reviewProductId = productId;
    this.reviewProductName = productName;
    this.reviewForm = { starRating: 5, reviewTitle: '', reviewText: '' };
    this.showReviewForm = true;
    this.setTab('reviews');
  }

  submitReview() {
    if (!this.reviewForm.reviewTitle?.trim()) return;
    const userId = this.getUserId();
    const payload = {
      ...this.reviewForm,
      user: { id: userId },
      product: { id: this.reviewProductId }
    };
    this.http.post<Review>(`${this.apiUrl}/reviews`, payload, this.getHeaders()).subscribe({
      next: (created) => {
        this.myReviews.unshift(created);
        this.showReviewForm = false;
        this.reviewProductName = '';
      },
      error: () => alert('Yorum gönderilemedi.')
    });
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  setTab(tab: string) {
    this.activeTab = tab;
  }
}
