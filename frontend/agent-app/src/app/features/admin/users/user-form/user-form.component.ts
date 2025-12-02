import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { AdminService } from '../../services/admin.service';
import { AdminUser, AdminTeam, CreateUserRequest, UpdateUserRequest } from '../../services/admin.models';
import { UserRole } from '../../../../core/models/user.model';

@Component({
  selector: 'app-user-form',
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
    MatSlideToggleModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatProgressBarModule
  ],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss']
})
export class UserFormComponent implements OnInit {
  userForm!: FormGroup;
  isEditMode = signal(false);
  loading = signal(false);
  saving = signal(false);
  userId: string | null = null;
  user: AdminUser | null = null;

  roles = Object.values(UserRole);
  teams = signal<AdminTeam[]>([]);
  languages = [
    { code: 'en', name: 'English' },
    { code: 'es', name: 'Spanish' },
    { code: 'fr', name: 'French' },
    { code: 'de', name: 'German' },
    { code: 'uz', name: 'Uzbek' },
    { code: 'ru', name: 'Russian' }
  ];

  passwordStrength = signal(0);
  passwordStrengthLabel = signal('');

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private adminService: AdminService,
    private snackBar: MatSnackBar
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadTeams();

    this.userId = this.route.snapshot.paramMap.get('id');
    if (this.userId && this.userId !== 'new') {
      this.isEditMode.set(true);
      this.loadUser(this.userId);
    }
  }

  private initForm(): void {
    this.userForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', this.isEditMode() ? [] : [Validators.required, Validators.minLength(8)]],
      role: [UserRole.AGENT, Validators.required],
      teamIds: [[]],
      phone: [''],
      language: ['en'],
      timezone: ['UTC'],
      active: [true]
    });

    // Password strength indicator
    this.userForm.get('password')?.valueChanges.subscribe(value => {
      this.calculatePasswordStrength(value);
    });
  }

  private loadTeams(): void {
    this.adminService.getTeams(0, 100).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.teams.set(response.data.content);
        }
      }
    });
  }

  private loadUser(id: string): void {
    this.loading.set(true);

    this.adminService.getUser(id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.user = response.data;
          this.patchForm(response.data);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading user:', error);
        this.snackBar.open('Failed to load user', 'Close', { duration: 3000 });
        this.loading.set(false);
        this.router.navigate(['/admin/users']);
      }
    });
  }

  private patchForm(user: AdminUser): void {
    this.userForm.patchValue({
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      role: user.role,
      teamIds: user.teams?.map(t => t.id) || [],
      phone: user.phone || '',
      language: user.language,
      timezone: user.timezone,
      active: user.active
    });

    // Make password optional in edit mode
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.updateValueAndValidity();
  }

  private calculatePasswordStrength(password: string): void {
    if (!password) {
      this.passwordStrength.set(0);
      this.passwordStrengthLabel.set('');
      return;
    }

    let strength = 0;
    if (password.length >= 8) strength += 25;
    if (password.length >= 12) strength += 10;
    if (/[a-z]/.test(password)) strength += 15;
    if (/[A-Z]/.test(password)) strength += 15;
    if (/[0-9]/.test(password)) strength += 15;
    if (/[^a-zA-Z0-9]/.test(password)) strength += 20;

    this.passwordStrength.set(Math.min(strength, 100));

    if (strength < 30) this.passwordStrengthLabel.set('Weak');
    else if (strength < 60) this.passwordStrengthLabel.set('Fair');
    else if (strength < 80) this.passwordStrengthLabel.set('Good');
    else this.passwordStrengthLabel.set('Strong');
  }

  getPasswordStrengthColor(): string {
    const strength = this.passwordStrength();
    if (strength < 30) return 'warn';
    if (strength < 60) return 'accent';
    return 'primary';
  }

  onSubmit(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const formValue = this.userForm.value;

    if (this.isEditMode() && this.userId) {
      const updateRequest: UpdateUserRequest = {
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        email: formValue.email,
        role: formValue.role,
        teamIds: formValue.teamIds,
        phone: formValue.phone || undefined,
        language: formValue.language,
        timezone: formValue.timezone,
        active: formValue.active
      };

      if (formValue.password) {
        updateRequest.password = formValue.password;
      }

      this.adminService.updateUser(this.userId, updateRequest).subscribe({
        next: () => {
          this.snackBar.open('User updated successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/admin/users']);
        },
        error: (error) => {
          console.error('Error updating user:', error);
          this.snackBar.open('Failed to update user', 'Close', { duration: 3000 });
          this.saving.set(false);
        }
      });
    } else {
      const createRequest: CreateUserRequest = {
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        email: formValue.email,
        password: formValue.password,
        role: formValue.role,
        teamIds: formValue.teamIds,
        phone: formValue.phone || undefined,
        language: formValue.language,
        timezone: formValue.timezone,
        active: formValue.active
      };

      this.adminService.createUser(createRequest).subscribe({
        next: () => {
          this.snackBar.open('User created successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/admin/users']);
        },
        error: (error) => {
          console.error('Error creating user:', error);
          this.snackBar.open('Failed to create user', 'Close', { duration: 3000 });
          this.saving.set(false);
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/admin/users']);
  }

  // Form field error helpers
  getFieldError(fieldName: string): string {
    const control = this.userForm.get(fieldName);
    if (control?.hasError('required')) return `${fieldName} is required`;
    if (control?.hasError('email')) return 'Invalid email format';
    if (control?.hasError('minlength')) {
      const minLength = control.errors?.['minlength'].requiredLength;
      return `Minimum ${minLength} characters required`;
    }
    return '';
  }
}
