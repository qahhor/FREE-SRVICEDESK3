import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AdminService } from '../../services/admin.service';
import { EmailSettings } from '../../services/admin.models';

@Component({
  selector: 'app-email-settings',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSlideToggleModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './email-settings.component.html',
  styleUrls: ['./email-settings.component.scss']
})
export class EmailSettingsComponent implements OnInit {
  emailForm!: FormGroup;
  loading = signal(true);
  saving = signal(false);
  testing = signal(false);
  showPassword = signal(false);

  constructor(
    private fb: FormBuilder,
    private adminService: AdminService,
    private snackBar: MatSnackBar
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadSettings();
  }

  private initForm(): void {
    this.emailForm = this.fb.group({
      smtpHost: ['', Validators.required],
      smtpPort: [587, [Validators.required, Validators.min(1), Validators.max(65535)]],
      smtpUsername: ['', Validators.required],
      smtpPassword: [''],
      smtpSecure: [true],
      fromEmail: ['', [Validators.required, Validators.email]],
      fromName: ['', Validators.required]
    });
  }

  private loadSettings(): void {
    this.loading.set(true);

    this.adminService.getEmailSettings().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.emailForm.patchValue({
            ...response.data,
            smtpPassword: '' // Don't show actual password
          });
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading email settings:', error);
        this.loading.set(false);
      }
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword.update(v => !v);
  }

  onSubmit(): void {
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const settings: EmailSettings = this.emailForm.value;

    this.adminService.updateEmailSettings(settings).subscribe({
      next: () => {
        this.snackBar.open('Email settings saved successfully', 'Close', { duration: 3000 });
        this.saving.set(false);
      },
      error: (error) => {
        console.error('Error saving email settings:', error);
        this.snackBar.open('Failed to save email settings', 'Close', { duration: 3000 });
        this.saving.set(false);
      }
    });
  }

  testEmailSettings(): void {
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      this.snackBar.open('Please fill in all required fields first', 'Close', { duration: 3000 });
      return;
    }

    this.testing.set(true);
    const settings: EmailSettings = this.emailForm.value;

    this.adminService.testEmailSettings(settings).subscribe({
      next: () => {
        this.snackBar.open('Test email sent successfully!', 'Close', { duration: 5000 });
        this.testing.set(false);
      },
      error: (error) => {
        console.error('Error testing email:', error);
        this.snackBar.open('Failed to send test email. Check your settings.', 'Close', { duration: 5000 });
        this.testing.set(false);
      }
    });
  }
}
