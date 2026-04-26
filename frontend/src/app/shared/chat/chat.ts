import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface Message {
  role: 'user' | 'bot';
  text: string;
  sql?: string;
  data?: any[];
  loading?: boolean;
}

@Component({
  selector: 'app-chat',
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class Chat implements OnInit {
  @Input() userRole: string = 'individual';
  @Input() userId: number = 0;

  messages: Message[] = [];
  question = '';
  isOpen = false;

  private apiUrl = 'http://localhost:8000/chat/ask';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.messages.push({
      role: 'bot',
      text: '👋 Merhaba! Ben ShopX AI asistanınım. Ürünler, siparişler, satışlar hakkında sorular sorabilirsiniz.'
    });
  }

  toggleChat() {
    this.isOpen = !this.isOpen;
  }

  sendMessage() {
    if (!this.question.trim()) return;

    const userMessage = this.question.trim();
    this.question = '';

    this.messages.push({ role: 'user', text: userMessage });
    this.messages.push({ role: 'bot', text: '', loading: true });

    this.http.post<any>(this.apiUrl, {
      question: userMessage,
      role: this.userRole,
      user_id: this.userId
    }).subscribe({
      next: (res) => {
        const idx = this.messages.map(m => m.loading).lastIndexOf(true);
        if (idx > -1) {
          this.messages[idx] = {
            role: 'bot',
            text: res.answer,
            sql: res.sql,
            data: res.data
          };
        }
      },
      error: () => {
        const idx = this.messages.map(m => m.loading).lastIndexOf(true);
        if (idx > -1) {
          this.messages[idx] = {
            role: 'bot',
            text: '❌ Bir hata oluştu. Lütfen tekrar deneyin.'
          };
        }
      }
    });
  }

  onEnter(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  getKeys(obj: any): string[] {
    return Object.keys(obj);
  }
}