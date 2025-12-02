import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TicketService } from '../../../core/services/ticket.service';
import { Project, TicketPriority } from '../../../core/models';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models';

@Component({
  selector: 'app-submit-ticket',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <div class="submit-ticket-container">
      <mat-card class="submit-card">
        <mat-card-header>
          <mat-card-title>Submit a Support Request</mat-card-title>
          <mat-card-subtitle>Please provide details about your issue</mat-card-subtitle>
        </mat-card-header>
        
        <mat-card-content>
          <form [formGroup]="ticketForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Project</mat-label>
              <mat-select formControlName="projectId">
                @for (project of projects; track project.id) {
                  <mat-option [value]="project.id">{{ project.name }}</mat-option>
                }
              </mat-select>
              @if (ticketForm.get('projectId')?.hasError('required') && ticketForm.get('projectId')?.touched) {
                <mat-error>Please select a project</mat-error>
              }
            </mat-form-field>
            
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Subject</mat-label>
              <input matInput formControlName="subject" placeholder="Brief summary of your issue">
              @if (ticketForm.get('subject')?.hasError('required') && ticketForm.get('subject')?.touched) {
                <mat-error>Subject is required</mat-error>
              }
              @if (ticketForm.get('subject')?.hasError('minlength') && ticketForm.get('subject')?.touched) {
                <mat-error>Subject must be at least 5 characters</mat-error>
              }
            </mat-form-field>
            
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Description</mat-label>
              <textarea matInput formControlName="description" rows="6" placeholder="Please describe your issue in detail"></textarea>
            </mat-form-field>
            
            <div class="form-row">
              <mat-form-field appearance="outline">
                <mat-label>Priority</mat-label>
                <mat-select formControlName="priority">
                  <mat-option value="LOW">Low</mat-option>
                  <mat-option value="MEDIUM">Medium</mat-option>
                  <mat-option value="HIGH">High</mat-option>
                  <mat-option value="URGENT">Urgent</mat-option>
                </mat-select>
              </mat-form-field>
              
              <mat-form-field appearance="outline">
                <mat-label>Category</mat-label>
                <mat-select formControlName="category">
                  <mat-option value="">-- Select Category --</mat-option>
                  <mat-option value="General">General Inquiry</mat-option>
                  <mat-option value="Technical">Technical Issue</mat-option>
                  <mat-option value="Billing">Billing</mat-option>
                  <mat-option value="Feature">Feature Request</mat-option>
                  <mat-option value="Bug">Bug Report</mat-option>
                </mat-select>
              </mat-form-field>
            </div>
            
            <!-- File upload area -->
            <div class="file-upload-area" 
                 (dragover)="onDragOver($event)" 
                 (drop)="onDrop($event)"
                 (dragleave)="onDragLeave($event)"
                 [class.dragover]="isDragOver">
              <mat-icon>cloud_upload</mat-icon>
              <p>Drag and drop files here or</p>
              <button mat-stroked-button type="button" (click)="fileInput.click()">
                Browse Files
              </button>
              <input #fileInput type="file" multiple hidden (change)="onFileSelect($event)">
              <small>Max 10MB per file. Allowed: jpg, png, pdf, doc, docx, txt</small>
            </div>
            
            @if (selectedFiles.length > 0) {
              <div class="selected-files">
                @for (file of selectedFiles; track file.name; let i = $index) {
                  <div class="file-item">
                    <mat-icon>insert_drive_file</mat-icon>
                    <span>{{ file.name }}</span>
                    <span class="file-size">{{ formatFileSize(file.size) }}</span>
                    <button mat-icon-button type="button" (click)="removeFile(i)">
                      <mat-icon>close</mat-icon>
                    </button>
                  </div>
                }
              </div>
            }
            
            <button mat-flat-button color="primary" type="submit" class="full-width submit-btn" [disabled]="loading">
              @if (loading) {
                <mat-spinner diameter="20"></mat-spinner>
              } @else {
                Submit Ticket
              }
            </button>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .submit-ticket-container {
      max-width: 800px;
      margin: 32px auto;
      padding: 0 16px;
    }
    
    .submit-card {
      padding: 24px;
    }
    
    mat-card-header {
      margin-bottom: 24px;
    }
    
    mat-card-title {
      font-size: 1.5rem !important;
    }
    
    .full-width {
      width: 100%;
    }
    
    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    
    .file-upload-area {
      border: 2px dashed #ccc;
      border-radius: 8px;
      padding: 32px;
      text-align: center;
      margin-bottom: 24px;
      transition: all 0.2s;
    }
    
    .file-upload-area.dragover {
      border-color: #3f51b5;
      background: #e8eaf6;
    }
    
    .file-upload-area mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #999;
    }
    
    .file-upload-area p {
      margin: 8px 0 16px;
      color: #666;
    }
    
    .file-upload-area small {
      display: block;
      margin-top: 8px;
      color: #999;
    }
    
    .selected-files {
      margin-bottom: 24px;
    }
    
    .file-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px;
      background: #f5f5f5;
      border-radius: 4px;
      margin-bottom: 8px;
    }
    
    .file-item mat-icon {
      color: #666;
    }
    
    .file-item span {
      flex: 1;
    }
    
    .file-size {
      color: #999;
      font-size: 0.875rem;
    }
    
    .submit-btn {
      height: 48px;
      font-size: 1rem;
    }
    
    @media (max-width: 600px) {
      .form-row {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class SubmitTicketComponent implements OnInit {
  ticketForm: FormGroup;
  projects: Project[] = [];
  selectedFiles: File[] = [];
  loading = false;
  isDragOver = false;

  constructor(
    private fb: FormBuilder,
    private ticketService: TicketService,
    private http: HttpClient,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.ticketForm = this.fb.group({
      projectId: ['', Validators.required],
      subject: ['', [Validators.required, Validators.minLength(5)]],
      description: [''],
      priority: ['MEDIUM'],
      category: ['']
    });
  }

  ngOnInit() {
    this.loadProjects();
  }

  loadProjects() {
    this.http.get<ApiResponse<Project[]>>(`${environment.apiUrl}/projects`).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.projects = response.data.filter(p => p.active);
          if (this.projects.length === 1) {
            this.ticketForm.patchValue({ projectId: this.projects[0].id });
          }
        }
      }
    });
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = false;
    
    if (event.dataTransfer?.files) {
      this.addFiles(event.dataTransfer.files);
    }
  }

  onFileSelect(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.addFiles(input.files);
    }
  }

  addFiles(files: FileList) {
    const allowedTypes = ['image/jpeg', 'image/png', 'application/pdf', 'application/msword', 
                          'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/plain'];
    const maxSize = 10 * 1024 * 1024; // 10MB
    
    Array.from(files).forEach(file => {
      if (file.size > maxSize) {
        this.snackBar.open(`File ${file.name} is too large (max 10MB)`, 'OK', { duration: 3000 });
        return;
      }
      if (!allowedTypes.includes(file.type)) {
        this.snackBar.open(`File type not allowed: ${file.name}`, 'OK', { duration: 3000 });
        return;
      }
      this.selectedFiles.push(file);
    });
  }

  removeFile(index: number) {
    this.selectedFiles.splice(index, 1);
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  onSubmit() {
    if (this.ticketForm.valid) {
      this.loading = true;
      
      const request = {
        projectId: this.ticketForm.value.projectId,
        subject: this.ticketForm.value.subject,
        description: this.ticketForm.value.description,
        priority: this.ticketForm.value.priority as TicketPriority,
        category: this.ticketForm.value.category
      };
      
      this.ticketService.submitTicket(request).subscribe({
        next: (response) => {
          if (response.success && response.data) {
            // Upload attachments if any
            if (this.selectedFiles.length > 0) {
              this.uploadAttachments(response.data.id);
            } else {
              this.loading = false;
              this.snackBar.open('Ticket submitted successfully!', 'OK', { duration: 3000 });
              this.router.navigate(['/tickets', response.data.id]);
            }
          } else {
            this.loading = false;
            this.snackBar.open(response.error || 'Failed to submit ticket', 'OK', { duration: 3000 });
          }
        },
        error: (error) => {
          this.loading = false;
          this.snackBar.open(error.error?.error || 'Failed to submit ticket', 'OK', { duration: 3000 });
        }
      });
    } else {
      this.ticketForm.markAllAsTouched();
    }
  }

  private uploadAttachments(ticketId: string) {
    let uploaded = 0;
    const total = this.selectedFiles.length;
    
    this.selectedFiles.forEach(file => {
      this.ticketService.uploadAttachment(ticketId, file).subscribe({
        next: () => {
          uploaded++;
          if (uploaded === total) {
            this.loading = false;
            this.snackBar.open('Ticket submitted successfully!', 'OK', { duration: 3000 });
            this.router.navigate(['/tickets', ticketId]);
          }
        },
        error: () => {
          uploaded++;
          if (uploaded === total) {
            this.loading = false;
            this.snackBar.open('Ticket submitted, but some attachments failed to upload', 'OK', { duration: 3000 });
            this.router.navigate(['/tickets', ticketId]);
          }
        }
      });
    });
  }
}
