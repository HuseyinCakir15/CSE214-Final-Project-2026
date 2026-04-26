import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Chat } from '../../../shared/chat/chat';

interface User {
  id: number;
  email: string;
  roleType: string;
  gender?: string;
}

interface Store {
  id: number;
  name: string;
  status: string; // 'open' veya 'closed'
  owner?: { id: number; email: string };
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

interface Category {
  id: number;
  name: string;
  parent?: { id: number; name: string };
}

@Component({
  selector: 'app-admin',
  imports: [CommonModule, FormsModule, Chat],
  templateUrl: './admin.html',
  styleUrl: './admin.css',
})
export class Admin implements OnInit {
  activeTab = 'dashboard';

  totalUsers = 0;
  totalOrders = 0;
  totalStores = 0;
  totalCategories = 0;

  users: User[] = [];
  stores: Store[] = [];
  orders: Order[] = [];
  categories: Category[] = [];
  newCategoryName = '';

  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.loadAll();
  }

  getHeaders() {
    const token = localStorage.getItem('token');
    return { headers: new HttpHeaders({ Authorization: `Bearer ${token}` }) };
  }

  loadAll() {
    this.loadUsers();
    this.loadStores();
    this.loadOrders();
    this.loadCategories();
  }

  loadUsers() {
    this.http.get<User[]>(`${this.apiUrl}/users`, this.getHeaders()).subscribe({
      next: (data) => { this.users = data; this.totalUsers = data.length; },
      error: () => {}
    });
  }

  loadStores() {
    this.http.get<Store[]>(`${this.apiUrl}/stores`, this.getHeaders()).subscribe({
      next: (data) => { this.stores = data; this.totalStores = data.length; },
      error: () => {}
    });
  }

  loadOrders() {
    this.http.get<Order[]>(`${this.apiUrl}/orders`, this.getHeaders()).subscribe({
      next: (data) => { this.orders = data; this.totalOrders = data.length; },
      error: () => {}
    });
  }

  loadCategories() {
    this.http.get<Category[]>(`${this.apiUrl}/categories`, this.getHeaders()).subscribe({
      next: (data) => { this.categories = data; this.totalCategories = data.length; },
      error: () => {}
    });
  }

  openStore(store: Store) {
    this.http.put<Store>(`${this.apiUrl}/stores/${store.id}/open`, {}, this.getHeaders()).subscribe({
      next: () => store.status = 'open',
      error: () => alert('Mağaza açılamadı.')
    });
  }

  closeStore(store: Store) {
    this.http.put<Store>(`${this.apiUrl}/stores/${store.id}/close`, {}, this.getHeaders()).subscribe({
      next: () => store.status = 'closed',
      error: () => alert('Mağaza kapatılamadı.')
    });
  }

  deleteUser(id: number) {
    if (!confirm('Bu kullanıcıyı silmek istediğinize emin misiniz?')) return;
    this.http.delete(`${this.apiUrl}/users/${id}`, this.getHeaders()).subscribe({
      next: () => { this.users = this.users.filter(u => u.id !== id); this.totalUsers--; },
      error: () => alert('Kullanıcı silinemedi.')
    });
  }

  addCategory() {
    if (!this.newCategoryName.trim()) return;
    this.http.post<Category>(`${this.apiUrl}/categories`, { name: this.newCategoryName }, this.getHeaders()).subscribe({
      next: (cat) => { this.categories.push(cat); this.newCategoryName = ''; this.totalCategories++; },
      error: () => alert('Kategori eklenemedi.')
    });
  }

  deleteCategory(id: number) {
    if (!confirm('Bu kategoriyi silmek istediğinize emin misiniz?')) return;
    this.http.delete(`${this.apiUrl}/categories/${id}`, this.getHeaders()).subscribe({
      next: () => { this.categories = this.categories.filter(c => c.id !== id); this.totalCategories--; },
      error: () => alert('Kategori silinemedi.')
    });
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    this.router.navigate(['/login']);
  }

  setTab(tab: string) {
    this.activeTab = tab;
  }
}