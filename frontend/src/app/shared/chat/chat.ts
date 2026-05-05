import {
  Component, OnInit, AfterViewChecked,
  ElementRef, ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/services/auth';

interface Message {
  role: 'user' | 'bot';
  text: string;
  sql?: string;
  data?: any[];
  plotlyJson?: string;
  loading?: boolean;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class Chat implements OnInit, AfterViewChecked {
  @ViewChild('messagesEnd') messagesEnd!: ElementRef;

  messages: Message[] = [];
  question = '';
  isOpen = false;
  private renderedCharts = new Set<number>();

  // Spring Boot üzerinden — artık direkt Python'a değil
  private apiUrl = 'http://localhost:8080/api/chat/ask';

  constructor(private http: HttpClient, private auth: AuthService) {}

  ngOnInit() {
    this.messages.push({
      role: 'bot',
      text: '👋 Merhaba! Ben ShopX AI asistanınım. Siparişler, ürünler ve satışlar hakkında sorular sorabilirsiniz.'
    });
  }

  ngAfterViewChecked() {
    this.renderPendingCharts();
    this.scrollToBottom();
  }

  toggleChat() { this.isOpen = !this.isOpen; }

  sendMessage() {
    if (!this.question.trim()) return;
    const userMessage = this.question.trim();
    this.question = '';

    this.messages.push({ role: 'user', text: userMessage });
    this.messages.push({ role: 'bot', text: '', loading: true });

    // Sadece question gönder — rol ve userId Spring Boot JWT'den alacak
    this.http.post<any>(this.apiUrl, { question: userMessage }).subscribe({
      next: (res) => {
        const idx = this.messages.map(m => m.loading).lastIndexOf(true);
        if (idx > -1) {
          this.messages[idx] = {
            role: 'bot',
            text: res.answer,
            sql: res.sql,
            data: res.data,
            plotlyJson: res.plotly_json
          };
        }
      },
      error: () => {
        const idx = this.messages.map(m => m.loading).lastIndexOf(true);
        if (idx > -1) {
          this.messages[idx] = {
            role: 'bot',
            text: 'Şu an yanıt verilemiyor. Lütfen daha sonra tekrar deneyin.'
          };
        }
      }
    });
  }

  private renderPendingCharts() {
    this.messages.forEach((msg, idx) => {
      if (msg.plotlyJson && !this.renderedCharts.has(idx)) {
        const el = document.getElementById(`plotly-chart-${idx}`);
        const Plotly = (window as any)['Plotly'];
        if (el && Plotly) {
          try {
            const figure = JSON.parse(msg.plotlyJson);
            Plotly.react(el, figure.data, {
              ...figure.layout,
              paper_bgcolor: 'transparent',
              plot_bgcolor: 'transparent',
              font: { color: '#e2e8f0' },
              margin: { t: 40, r: 10, b: 40, l: 40 }
            });
            this.renderedCharts.add(idx);
          } catch (e) {
            console.error('Plotly render error:', e);
          }
        }
      }
    });
  }

  private scrollToBottom() {
    try {
      this.messagesEnd?.nativeElement?.scrollIntoView({ behavior: 'smooth' });
    } catch {}
  }

  onEnter(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  getKeys(obj: any): string[] { return Object.keys(obj); }
}
