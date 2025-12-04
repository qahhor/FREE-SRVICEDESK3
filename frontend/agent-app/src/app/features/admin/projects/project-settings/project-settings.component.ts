import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AdminService } from '../../services/admin.service';
import { AdminProject, AdminTeam } from '../../services/admin.models';

@Component({
  selector: 'app-project-settings',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './project-settings.component.html',
  styleUrls: ['./project-settings.component.scss']
})
export class ProjectSettingsComponent implements OnInit {
  project = signal<AdminProject | null>(null);
  loading = signal(true);
  saving = signal(false);
  projectId: string | null = null;

  teams = signal<AdminTeam[]>([]);

  // Forms for different settings sections
  generalForm!: FormGroup;
  workflowForm!: FormGroup;
  notificationForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private adminService: AdminService,
    private snackBar: MatSnackBar
  ) {
    this.initForms();
  }

  ngOnInit(): void {
    this.loadTeams();

    this.projectId = this.route.snapshot.paramMap.get('id');
    if (this.projectId) {
      this.loadProject(this.projectId);
    }
  }

  private initForms(): void {
    this.generalForm = this.fb.group({
      defaultTeamId: [null],
      autoAssign: [false],
      defaultPriority: ['MEDIUM']
    });

    this.workflowForm = this.fb.group({
      allowReopen: [true],
      requireResolutionNote: [false],
      autoCloseAfterDays: [7]
    });

    this.notificationForm = this.fb.group({
      notifyOnCreate: [true],
      notifyOnUpdate: [true],
      notifyOnComment: [true],
      notifyOnResolve: [true]
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
          this.project.set(response.data);
          this.patchForms(response.data);
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

  private patchForms(project: AdminProject): void {
    this.generalForm.patchValue({
      defaultTeamId: project.defaultTeam?.id || null
    });
  }

  saveGeneralSettings(): void {
    this.saving.set(true);
    const formValue = this.generalForm.value;

    if (this.projectId) {
      this.adminService.updateProject(this.projectId, {
        defaultTeamId: formValue.defaultTeamId
      }).subscribe({
        next: () => {
          this.snackBar.open('Settings saved successfully', 'Close', { duration: 3000 });
          this.saving.set(false);
        },
        error: () => {
          this.snackBar.open('Failed to save settings', 'Close', { duration: 3000 });
          this.saving.set(false);
        }
      });
    }
  }

  saveWorkflowSettings(): void {
    this.snackBar.open('Workflow settings saved (demo)', 'Close', { duration: 3000 });
  }

  saveNotificationSettings(): void {
    this.snackBar.open('Notification settings saved (demo)', 'Close', { duration: 3000 });
  }

  goBack(): void {
    this.router.navigate(['/admin/projects']);
  }
}
