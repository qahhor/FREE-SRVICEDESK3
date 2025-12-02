import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="forgot-password-container">
      <mat-card class="forgot-password-card">
        <mat-card-header>
          <mat-card-title>Forgot Password?</mat-card-title>
          <mat-card-subtitle>Enter your email to receive a reset link</mat-card-subtitle>
        </mat-card-header>
        
        <mat-card-content>
          @if (errorMessage) {
            <div class="error-message">
              <mat-icon>error</mat-icon>
              {{ errorMessage }}
            </div>
          }
          
          @if (successMessage) {
            <div class="success-message">
              <mat-icon>check_circle</mat-icon>
              {{ successMessage }}
            </div>
          }
          
          @if (!submitted) {
            <form [formGroup]="forgotForm" (ngSubmit)="onSubmit()">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Email</mat-label>
                <input matInput type="email" formControlName="email" placeholder="Enter your email">
                <mat-icon matPrefix>email</mat-icon>
                @if (forgotForm.get('email')?.hasError('required') && forgotForm.get('email')?.touched) {
                  <mat-error>Email is required</mat-error>
                }
                @if (forgotForm.get('email')?.hasError('email') && forgotForm.get('email')?.touched) {
                  <mat-error>Please enter a valid email</mat-error>
                }
              </mat-form-field>
              
              <button mat-flat-button color="primary" type="submit" class="full-width submit-btn" [disabled]="loading">
                @if (loading) {
                  <mat-spinner diameter="20"></mat-spinner>
                } @else {
                  Send Reset Link
                }
              </button>
            </form>
          }
        </mat-card-content>
        
        <mat-card-actions>
          <p class="back-link">
            <a routerLink="/login">
              <mat-icon>arrow_back</mat-icon>
              Back to Login
            </a>
          </p>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .forgot-password-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: calc(100vh - 200px);
      padding: 24px;
    }
    
    .forgot-password-card {
      width: 100%;
      max-width: 400px;
      padding: 24px;
    }
    
    mat-card-header {
      display: block;
      text-align: center;
      margin-bottom: 24px;
    }
    
    mat-card-title {
      font-size: 1.5rem !important;
      margin-bottom: 8px;
    }
    
    .full-width {
      width: 100%;
    }
    
    .error-message {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      margin-bottom: 16px;
      background: #ffebee;
      color: #c62828;
      border-radius: 4px;
    }
    
    .success-message {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      margin-bottom: 16px;
      background: #e8f5e9;
      color: #2e7d32;
      border-radius: 4px;
    }
    
    .submit-btn {
      height: 48px;
      font-size: 1rem;
      margin-top: 16px;
    }
    
    mat-card-actions {
      text-align: center;
      padding-top: 16px;
    }
    
    .back-link {
      margin: 0;
    }
    
    .back-link a {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: #3f51b5;
      text-decoration: none;
    }
    
    .back-link mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }
  `]
})
export class ForgotPasswordComponent {
  forgotForm: FormGroup;
  loading = false;
  submitted = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit() {
    if (this.forgotForm.valid) {
      this.loading = true;
      this.errorMessage = '';
      
      const { email } = this.forgotForm.value;
      
      this.authService.forgotPassword({ email }).subscribe({
        next: (response) => {
          this.loading = false;
          if (response.success) {
            this.submitted = true;
            this.successMessage = 'If an account exists with this email, you will receive a password reset link.';
          } else {
            this.errorMessage = response.error || 'Failed to send reset link';
          }
        },
        error: () => {
          this.loading = false;
          // Always show success to prevent email enumeration
          this.submitted = true;
          this.successMessage = 'If an account exists with this email, you will receive a password reset link.';
        }
      });
    }
  }
}
