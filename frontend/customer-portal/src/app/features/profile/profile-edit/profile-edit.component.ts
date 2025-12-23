import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatExpansionModule } from '@angular/material/expansion';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-profile-edit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatSnackBarModule,
    MatExpansionModule
  ],
  template: `
    <div class="profile-edit-container">
      <div class="page-header">
        <a routerLink="/profile" class="back-link">
          <mat-icon>arrow_back</mat-icon>
          Back to Profile
        </a>
        <h1>Edit Profile</h1>
      </div>

      <!-- Profile Information -->
      <mat-card class="edit-card">
        <h3>Profile Information</h3>
        <form [formGroup]="profileForm" (ngSubmit)="saveProfile()">
          <div class="form-row">
            <mat-form-field appearance="outline">
              <mat-label>First Name</mat-label>
              <input matInput formControlName="firstName">
              @if (profileForm.get('firstName')?.hasError('required')) {
                <mat-error>First name is required</mat-error>
              }
            </mat-form-field>
            
            <mat-form-field appearance="outline">
              <mat-label>Last Name</mat-label>
              <input matInput formControlName="lastName">
              @if (profileForm.get('lastName')?.hasError('required')) {
                <mat-error>Last name is required</mat-error>
              }
            </mat-form-field>
          </div>
          
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Email</mat-label>
            <input matInput formControlName="email" readonly>
            <mat-hint>Email cannot be changed</mat-hint>
          </mat-form-field>
          
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Phone</mat-label>
            <input matInput formControlName="phone" type="tel">
          </mat-form-field>
          
          <div class="form-row">
            <mat-form-field appearance="outline">
              <mat-label>Language</mat-label>
              <mat-select formControlName="language">
                <mat-option value="en">English</mat-option>
                <mat-option value="es">Spanish</mat-option>
                <mat-option value="fr">French</mat-option>
                <mat-option value="de">German</mat-option>
                <mat-option value="ru">Russian</mat-option>
                <mat-option value="uz">Uzbek</mat-option>
              </mat-select>
            </mat-form-field>
            
            <mat-form-field appearance="outline">
              <mat-label>Timezone</mat-label>
              <mat-select formControlName="timezone">
                <mat-option value="UTC">UTC</mat-option>
                <mat-option value="America/New_York">Eastern Time</mat-option>
                <mat-option value="America/Chicago">Central Time</mat-option>
                <mat-option value="America/Denver">Mountain Time</mat-option>
                <mat-option value="America/Los_Angeles">Pacific Time</mat-option>
                <mat-option value="Europe/London">London</mat-option>
                <mat-option value="Europe/Paris">Paris</mat-option>
                <mat-option value="Asia/Tokyo">Tokyo</mat-option>
                <mat-option value="Asia/Tashkent">Tashkent</mat-option>
              </mat-select>
            </mat-form-field>
          </div>
          
          <div class="form-actions">
            <button mat-button type="button" routerLink="/profile">Cancel</button>
            <button mat-flat-button color="primary" type="submit" [disabled]="savingProfile || !profileForm.dirty">
              @if (savingProfile) {
                <mat-spinner diameter="20"></mat-spinner>
              } @else {
                Save Changes
              }
            </button>
          </div>
        </form>
      </mat-card>

      <!-- Change Password -->
      <mat-card class="edit-card">
        <mat-expansion-panel>
          <mat-expansion-panel-header>
            <mat-panel-title>
              <mat-icon>lock</mat-icon>
              Change Password
            </mat-panel-title>
          </mat-expansion-panel-header>
          
          <form [formGroup]="passwordForm" (ngSubmit)="changePassword()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Current Password</mat-label>
              <input matInput [type]="hideCurrentPassword ? 'password' : 'text'" formControlName="currentPassword">
              <button mat-icon-button matSuffix type="button" (click)="hideCurrentPassword = !hideCurrentPassword">
                <mat-icon>{{ hideCurrentPassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              @if (passwordForm.get('currentPassword')?.hasError('required')) {
                <mat-error>Current password is required</mat-error>
              }
            </mat-form-field>
            
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>New Password</mat-label>
              <input matInput [type]="hideNewPassword ? 'password' : 'text'" formControlName="newPassword">
              <button mat-icon-button matSuffix type="button" (click)="hideNewPassword = !hideNewPassword">
                <mat-icon>{{ hideNewPassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              @if (passwordForm.get('newPassword')?.hasError('required')) {
                <mat-error>New password is required</mat-error>
              }
              @if (passwordForm.get('newPassword')?.hasError('minlength')) {
                <mat-error>Password must be at least 8 characters</mat-error>
              }
            </mat-form-field>
            
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Confirm New Password</mat-label>
              <input matInput [type]="hideConfirmPassword ? 'password' : 'text'" formControlName="confirmPassword">
              <button mat-icon-button matSuffix type="button" (click)="hideConfirmPassword = !hideConfirmPassword">
                <mat-icon>{{ hideConfirmPassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              @if (passwordForm.get('confirmPassword')?.hasError('required')) {
                <mat-error>Please confirm your password</mat-error>
              }
              @if (passwordForm.hasError('passwordMismatch')) {
                <mat-error>Passwords do not match</mat-error>
              }
            </mat-form-field>
            
            <div class="form-actions">
              <button mat-flat-button color="primary" type="submit" [disabled]="changingPassword">
                @if (changingPassword) {
                  <mat-spinner diameter="20"></mat-spinner>
                } @else {
                  Change Password
                }
              </button>
            </div>
          </form>
        </mat-expansion-panel>
      </mat-card>

      <!-- Danger Zone -->
      <mat-card class="edit-card danger-zone">
        <h3>Danger Zone</h3>
        <p>Once you delete your account, there is no going back. Please be certain.</p>
        <button mat-stroked-button color="warn" disabled>
          <mat-icon>delete_forever</mat-icon>
          Delete Account
        </button>
        <small>Account deletion is currently disabled. Contact support for assistance.</small>
      </mat-card>
    </div>
  `,
  styles: [`
    .profile-edit-container {
      max-width: 700px;
      margin: 32px auto;
      padding: 0 16px;
    }
    
    .page-header {
      margin-bottom: 24px;
    }
    
    .back-link {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      color: #3f51b5;
      text-decoration: none;
      font-size: 0.875rem;
      margin-bottom: 8px;
    }
    
    .back-link mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }
    
    .page-header h1 {
      margin: 0;
    }
    
    .edit-card {
      padding: 24px;
      margin-bottom: 24px;
    }
    
    .edit-card h3 {
      margin: 0 0 20px;
      font-size: 1.1rem;
      font-weight: 500;
    }
    
    .full-width {
      width: 100%;
    }
    
    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    
    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 16px;
    }
    
    mat-expansion-panel {
      box-shadow: none !important;
    }
    
    mat-expansion-panel-header mat-icon {
      margin-right: 8px;
    }
    
    .danger-zone {
      border: 1px solid #ffcdd2;
      background: #fff8f8;
    }
    
    .danger-zone h3 {
      color: #c62828;
    }
    
    .danger-zone p {
      color: #666;
      margin: 0 0 16px;
    }
    
    .danger-zone small {
      display: block;
      margin-top: 8px;
      color: #999;
    }
    
    @media (max-width: 600px) {
      .form-row {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class ProfileEditComponent implements OnInit {
  profileForm: FormGroup;
  passwordForm: FormGroup;
  savingProfile = false;
  changingPassword = false;
  hideCurrentPassword = true;
  hideNewPassword = true;
  hideConfirmPassword = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.profileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: [{ value: '', disabled: true }],
      phone: [''],
      language: ['en'],
      timezone: ['UTC']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit() {
    const user = this.authService.currentUser();
    if (user) {
      this.profileForm.patchValue({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        phone: user.phone || '',
        language: user.language || 'en',
        timezone: user.timezone || 'UTC'
      });
    }
  }

  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');
    
    if (newPassword && confirmPassword && newPassword.value !== confirmPassword.value) {
      return { passwordMismatch: true };
    }
    return null;
  }

  saveProfile() {
    if (this.profileForm.valid) {
      this.savingProfile = true;
      
      const { firstName, lastName, phone, language, timezone } = this.profileForm.value;
      
      this.authService.updateProfile({ firstName, lastName, phone, language, timezone }).subscribe({
        next: (response) => {
          this.savingProfile = false;
          if (response.success) {
            this.snackBar.open('Profile updated successfully', 'OK', { duration: 3000 });
            this.profileForm.markAsPristine();
          } else {
            this.snackBar.open(response.error || 'Failed to update profile', 'OK', { duration: 3000 });
          }
        },
        error: (error) => {
          this.savingProfile = false;
          this.snackBar.open(error.error?.error || 'Failed to update profile', 'OK', { duration: 3000 });
        }
      });
    }
  }

  changePassword() {
    if (this.passwordForm.valid) {
      this.changingPassword = true;
      
      const { currentPassword, newPassword, confirmPassword } = this.passwordForm.value;
      
      this.authService.changePassword({ currentPassword, newPassword, confirmPassword }).subscribe({
        next: (response) => {
          this.changingPassword = false;
          if (response.success) {
            this.snackBar.open('Password changed successfully', 'OK', { duration: 3000 });
            this.passwordForm.reset();
          } else {
            this.snackBar.open(response.error || 'Failed to change password', 'OK', { duration: 3000 });
          }
        },
        error: (error) => {
          this.changingPassword = false;
          this.snackBar.open(error.error?.error || 'Failed to change password', 'OK', { duration: 3000 });
        }
      });
    } else {
      this.passwordForm.markAllAsTouched();
    }
  }
}
