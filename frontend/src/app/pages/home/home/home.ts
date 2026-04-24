import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface Product {
  id: number;
  name: string;
  sku?: string;
  unitPrice?: number;
  discountedPrice?: number;
  stock?: number;
  rating?: number;
  store?: { id: number; name: string };
  category?: { id: number; name: string };
}

@Component({
  selector: 'app-home',
  imports: [CommonModule, RouterLink, FormsModule, CurrencyPipe],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  products: Product[] = [];
  filteredProducts: Product[] = [];
  categories: string[] = ['Tümü'];
  selectedCategory = 'Tümü';
  searchQuery = '';
  loading = false;
  errorMessage = '';

  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadProducts();
  }

  loadProducts() {
    this.loading = true;
    this.errorMessage = '';

    this.http.get<Product[]>(`${this.apiUrl}/products`).subscribe({
      next: (data) => {
        this.products = data;
        this.filteredProducts = data;
        this.extractCategories(data);
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Ürünler yüklenirken bir hata oluştu.';
        this.loading = false;
      }
    });
  }

  extractCategories(products: Product[]) {
    const cats = [...new Set(products.map(p => p.category?.name).filter(Boolean))] as string[];
    this.categories = ['Tümü', ...cats];
  }

  filterByCategory(category: string) {
    this.selectedCategory = category;
    this.searchQuery = '';
    if (category === 'Tümü') {
      this.filteredProducts = this.products;
    } else {
      this.filteredProducts = this.products.filter(p => p.category?.name === category);
    }
  }

  onSearch() {
    this.selectedCategory = 'Tümü';
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) {
      this.filteredProducts = this.products;
      return;
    }
    this.filteredProducts = this.products.filter(p =>
      p.name.toLowerCase().includes(q)
    );
  }
}