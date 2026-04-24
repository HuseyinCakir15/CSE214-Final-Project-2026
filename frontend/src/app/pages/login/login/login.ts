import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  email = '';
  password = '';
  errorMessage = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  onLogin() {
    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.email, this.password).subscribe({
      next: (response: any) => {
        const role = response.role;
        if (role === 'admin') {
          this.router.navigate(['/admin']);
        } else if (role === 'corporate') {
          this.router.navigate(['/corporate']);
        } else {
          this.router.navigate(['/individual']);
        }
      },
      error: (err: any) => {
        this.loading = false;
        this.errorMessage = 'Email veya şifre hatalı!';
      }
    });
  }
}