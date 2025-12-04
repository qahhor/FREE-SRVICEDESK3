import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AdminService } from '../../services/admin.service';
import { SystemSettings } from '../../services/admin.models';

@Component({
  selector: 'app-general-settings',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './general-settings.component.html',
  styleUrls: ['./general-settings.component.scss']
})
export class GeneralSettingsComponent implements OnInit {
  settingsForm!: FormGroup;
  loading = signal(true);
  saving = signal(false);

  languages = [
    { code: 'en', name: 'English' },
    { code: 'es', name: 'Spanish' },
    { code: 'fr', name: 'French' },
    { code: 'de', name: 'German' },
    { code: 'uz', name: 'Uzbek' },
    { code: 'ru', name: 'Russian' }
  ];

  timezones = [
    'UTC',
    'America/New_York',
    'America/Los_Angeles',
    'America/Chicago',
    'Europe/London',
    'Europe/Paris',
    'Europe/Berlin',
    'Asia/Tokyo',
    'Asia/Shanghai',
    'Asia/Tashkent',
    'Australia/Sydney'
  ];

  dateFormats = [
    { value: 'MM/DD/YYYY', label: 'MM/DD/YYYY (US)' },
    { value: 'DD/MM/YYYY', label: 'DD/MM/YYYY (EU)' },
    { value: 'YYYY-MM-DD', label: 'YYYY-MM-DD (ISO)' }
  ];

  timeFormats = [
    { value: '12h', label: '12-hour (AM/PM)' },
    { value: '24h', label: '24-hour' }
  ];

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
    this.settingsForm = this.fb.group({
      companyName: ['', Validators.required],
      companyLogo: [''],
      defaultLanguage: ['en', Validators.required],
      timezone: ['UTC', Validators.required],
      dateFormat: ['YYYY-MM-DD', Validators.required],
      timeFormat: ['24h', Validators.required],
      paginationDefault: [20, [Validators.required, Validators.min(10), Validators.max(100)]]
    });
  }

  private loadSettings(): void {
    this.loading.set(true);

    this.adminService.getSystemSettings().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.settingsForm.patchValue(response.data);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading settings:', error);
        // Use default values on error
        this.loading.set(false);
      }
    });
  }

  onSubmit(): void {
    if (this.settingsForm.invalid) {
      this.settingsForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const settings: SystemSettings = this.settingsForm.value;

    this.adminService.updateSystemSettings(settings).subscribe({
      next: () => {
        this.snackBar.open('Settings saved successfully', 'Close', { duration: 3000 });
        this.saving.set(false);
      },
      error: (error) => {
        console.error('Error saving settings:', error);
        this.snackBar.open('Failed to save settings', 'Close', { duration: 3000 });
        this.saving.set(false);
      }
    });
  }
}
