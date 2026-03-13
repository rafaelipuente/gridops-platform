import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {
    if (auth.isLoggedIn()) {
      router.navigate(['/dashboard']);
    }
  }

  onSubmit(): void {
    this.error = '';
    if (!this.username.trim() || !this.password) {
      this.error = 'Username and password are required';
      return;
    }

    this.loading = true;
    this.auth.login({ username: this.username.trim(), password: this.password }).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 401) {
          this.error = 'Invalid username or password';
        } else if (err.status === 0) {
          this.error = 'Unable to reach the server';
        } else {
          this.error = 'An unexpected error occurred';
        }
      }
    });
  }
}
