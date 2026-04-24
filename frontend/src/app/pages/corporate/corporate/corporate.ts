import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';

interface Store {
  id: number;
  name: string;
  status: string;
  owner?: { id: number; email: string };
}

interface Product {
  id?: number;
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
  user?: { id: number; email: string };
  store?: { id: number; name: string };
}

interface Review {
  id: number;
  starRating?: number;
  sentiment?: string;
  reviewTitle?: string;
  reviewText?: string;
  createdAt?: string;
  user?: { id: number; email: string };
  product?: { id: number; name: string };
}

@Component({
  selector: 'app-corporate',
  imports: [CommonModule, FormsModule],
  templateUrl: './corporate.html',
  styleUrl: './corporate.css',
})
export class Corporate implements OnInit {
  activeTab = 'dashboard';

  store: Store | null = null;
  products: Product[] = [];
  orders: Order[] = [];
  reviews: Review[] = [];
  totalRevenue = 0;

  showProductForm = false;
  editingProduct: Product | null = null;
  productForm: Product = { name: '', unitPrice: 0, stock: 0 };

  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.loadStore();
  }

  getHeaders() {
    const token = localStorage.getItem('token');
    return { headers: new HttpHeaders({ Authorization: `Bearer ${token}` }) };
  }

  getUserId(): number {
    return Number(localStorage.getItem('id'));
  }

  loadStore() {
    const userId = this.getUserId();
    this.http.get<Store[]>(`${this.apiUrl}/stores/owner/${userId}`, this.getHeaders()).subscribe({
      next: (stores) => {
        if (stores.length > 0) {
          this.store = stores[0];
          this.loadProducts();
          this.loadOrders();
          this.loadRevenue();
          this.loadReviews();
        }
      },
      error: () => {}
    });
  }

  loadProducts() {
    this.http.get<Product[]>(`${this.apiUrl}/products`, this.getHeaders()).subscribe({
      next: (data) => {
        this.products = data.filter(p => p.store?.id === this.store?.id);
      },
      error: () => {}
    });
  }

  loadOrders() {
    if (!this.store) return;
    this.http.get<Order[]>(`${this.apiUrl}/orders/store/${this.store.id}`, this.getHeaders()).subscribe({
      next: (data) => this.orders = data,
      error: () => {}
    });
  }

  loadRevenue() {
    if (!this.store) return;
    this.http.get<number>(`${this.apiUrl}/orders/revenue/${this.store.id}`, this.getHeaders()).subscribe({
      next: (rev) => this.totalRevenue = rev ?? 0,
      error: () => {}
    });
  }

  loadReviews() {
    this.http.get<Review[]>(`${this.apiUrl}/reviews`, this.getHeaders()).subscribe({
      next: (data) => {
        // Kendi mağazasının ürünlerine ait reviewları filtrele
        const productIds = this.products.map(p => p.id);
        this.reviews = data.filter(r => productIds.includes(r.product?.id));
      },
      error: () => {}
    });
  }

  openAddProduct() {
    this.editingProduct = null;
    this.productForm = { name: '', unitPrice: 0, stock: 0 };
    this.showProductForm = true;
  }

  openEditProduct(product: Product) {
    this.editingProduct = product;
    this.productForm = { ...product };
    this.showProductForm = true;
  }

  saveProduct() {
    if (!this.productForm.name.trim()) return;
    const payload = { ...this.productForm, store: { id: this.store?.id } };

    if (this.editingProduct?.id) {
      this.http.put<Product>(`${this.apiUrl}/products/${this.editingProduct.id}`, payload, this.getHeaders()).subscribe({
        next: (updated) => {
          const idx = this.products.findIndex(p => p.id === updated.id);
          if (idx > -1) this.products[idx] = updated;
          this.showProductForm = false;
        },
        error: () => alert('Ürün güncellenemedi.')
      });
    } else {
      this.http.post<Product>(`${this.apiUrl}/products`, payload, this.getHeaders()).subscribe({
        next: (created) => {
          this.products.push(created);
          this.showProductForm = false;
        },
        error: () => alert('Ürün eklenemedi.')
      });
    }
  }

  deleteProduct(id: number) {
    if (!confirm('Bu ürünü silmek istediğinize emin misiniz?')) return;
    this.http.delete(`${this.apiUrl}/products/${id}`, this.getHeaders()).subscribe({
      next: () => this.products = this.products.filter(p => p.id !== id),
      error: () => alert('Ürün silinemedi.')
    });
  }

  updateOrderStatus(order: Order, status: string) {
    if (!status) return;
    this.http.put(`${this.apiUrl}/orders/${order.id}/status?status=${status}`, {}, this.getHeaders()).subscribe({
      next: () => order.status = status,
      error: () => alert('Sipariş durumu güncellenemedi.')
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