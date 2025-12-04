import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AdminService } from '../../services/admin.service';
import { SecuritySettings } from '../../services/admin.models';

@Component({
  selector: 'app-security-settings',
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
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './security-settings.component.html',
  styleUrls: ['./security-settings.component.scss']
})
export class SecuritySettingsComponent implements OnInit {
  securityForm!: FormGroup;
  loading = signal(true);
  saving = signal(false);

  ipWhitelist = signal<string[]>([]);
  newIpAddress = '';

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
    this.securityForm = this.fb.group({
      passwordMinLength: [8, [Validators.required, Validators.min(6), Validators.max(32)]],
      passwordRequireUppercase: [true],
      passwordRequireNumber: [true],
      passwordRequireSpecial: [false],
      sessionTimeoutMinutes: [60, [Validators.required, Validators.min(5), Validators.max(1440)]],
      twoFactorEnabled: [false],
      apiRateLimitPerMinute: [60, [Validators.required, Validators.min(10), Validators.max(1000)]]
    });
  }

  private loadSettings(): void {
    this.loading.set(true);

    this.adminService.getSecuritySettings().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.securityForm.patchValue(response.data);
          this.ipWhitelist.set(response.data.ipWhitelist || []);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading security settings:', error);
        this.loading.set(false);
      }
    });
  }

  addIpAddress(): void {
    const ip = this.newIpAddress.trim();
    if (ip && this.isValidIp(ip) && !this.ipWhitelist().includes(ip)) {
      this.ipWhitelist.update(list => [...list, ip]);
      this.newIpAddress = '';
    } else if (ip && !this.isValidIp(ip)) {
      this.snackBar.open('Invalid IP address format', 'Close', { duration: 3000 });
    }
  }

  removeIpAddress(ip: string): void {
    this.ipWhitelist.update(list => list.filter(i => i !== ip));
  }

  private isValidIp(ip: string): boolean {
    // Simple IPv4 validation
    const ipv4Pattern = /^(\d{1,3}\.){3}\d{1,3}(\/\d{1,2})?$/;
    return ipv4Pattern.test(ip);
  }

  onSubmit(): void {
    if (this.securityForm.invalid) {
      this.securityForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const settings: SecuritySettings = {
      ...this.securityForm.value,
      ipWhitelist: this.ipWhitelist()
    };

    this.adminService.updateSecuritySettings(settings).subscribe({
      next: () => {
        this.snackBar.open('Security settings saved successfully', 'Close', { duration: 3000 });
        this.saving.set(false);
      },
      error: (error) => {
        console.error('Error saving security settings:', error);
        this.snackBar.open('Failed to save security settings', 'Close', { duration: 3000 });
        this.saving.set(false);
      }
    });
  }
}
