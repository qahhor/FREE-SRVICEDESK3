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

import { AdminService } from '../../services/admin.service';
import { AdminProject, AdminTeam, CreateProjectRequest, UpdateProjectRequest } from '../../services/admin.models';

@Component({
  selector: 'app-project-form',
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
    MatSnackBarModule
  ],
  templateUrl: './project-form.component.html',
  styleUrls: ['./project-form.component.scss']
})
export class ProjectFormComponent implements OnInit {
  projectForm!: FormGroup;
  isEditMode = signal(false);
  loading = signal(false);
  saving = signal(false);
  projectId: string | null = null;
  project: AdminProject | null = null;

  teams = signal<AdminTeam[]>([]);

  colorOptions = [
    { value: '#3f51b5', name: 'Indigo' },
    { value: '#6366f1', name: 'Purple' },
    { value: '#2196f3', name: 'Blue' },
    { value: '#009688', name: 'Teal' },
    { value: '#4caf50', name: 'Green' },
    { value: '#ff9800', name: 'Orange' },
    { value: '#f44336', name: 'Red' },
    { value: '#9c27b0', name: 'Deep Purple' }
  ];

  iconOptions = [
    { value: 'folder', name: 'Folder' },
    { value: 'work', name: 'Work' },
    { value: 'code', name: 'Code' },
    { value: 'bug_report', name: 'Bug' },
    { value: 'support', name: 'Support' },
    { value: 'engineering', name: 'Engineering' },
    { value: 'science', name: 'Science' },
    { value: 'design_services', name: 'Design' }
  ];

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

    this.projectId = this.route.snapshot.paramMap.get('id');
    if (this.projectId && this.projectId !== 'new') {
      this.isEditMode.set(true);
      this.loadProject(this.projectId);
    }
  }

  private initForm(): void {
    this.projectForm = this.fb.group({
      key: ['', [Validators.required, Validators.pattern(/^[A-Z][A-Z0-9]{1,9}$/)]],
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      defaultTeamId: [null],
      color: ['#6366f1'],
      icon: ['folder'],
      active: [true]
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

  private loadProject(id: string): void {
    this.loading.set(true);

    this.adminService.getProject(id).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.project = response.data;
          this.patchForm(response.data);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading project:', error);
        this.snackBar.open('Failed to load project', 'Close', { duration: 3000 });
        this.loading.set(false);
        this.router.navigate(['/admin/projects']);
      }
    });
  }

  private patchForm(project: AdminProject): void {
    this.projectForm.patchValue({
      key: project.key,
      name: project.name,
      description: project.description || '',
      defaultTeamId: project.defaultTeam?.id || null,
      color: project.color || '#6366f1',
      icon: project.icon || 'folder',
      active: project.active
    });

    // Disable key field in edit mode
    this.projectForm.get('key')?.disable();
  }

  onKeyInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.toUpperCase().replace(/[^A-Z0-9]/g, '');
    this.projectForm.get('key')?.setValue(input.value);
  }

  onSubmit(): void {
    if (this.projectForm.invalid) {
      this.projectForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const formValue = this.projectForm.getRawValue();

    if (this.isEditMode() && this.projectId) {
      const updateRequest: UpdateProjectRequest = {
        name: formValue.name,
        description: formValue.description || undefined,
        defaultTeamId: formValue.defaultTeamId || undefined,
        color: formValue.color,
        icon: formValue.icon,
        active: formValue.active
      };

      this.adminService.updateProject(this.projectId, updateRequest).subscribe({
        next: () => {
          this.snackBar.open('Project updated successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/admin/projects']);
        },
        error: (error) => {
          console.error('Error updating project:', error);
          this.snackBar.open('Failed to update project', 'Close', { duration: 3000 });
          this.saving.set(false);
        }
      });
    } else {
      const createRequest: CreateProjectRequest = {
        key: formValue.key,
        name: formValue.name,
        description: formValue.description || undefined,
        defaultTeamId: formValue.defaultTeamId || undefined,
        color: formValue.color,
        icon: formValue.icon,
        active: formValue.active
      };

      this.adminService.createProject(createRequest).subscribe({
        next: () => {
          this.snackBar.open('Project created successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/admin/projects']);
        },
        error: (error) => {
          console.error('Error creating project:', error);
          this.snackBar.open('Failed to create project', 'Close', { duration: 3000 });
          this.saving.set(false);
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/admin/projects']);
  }
}
