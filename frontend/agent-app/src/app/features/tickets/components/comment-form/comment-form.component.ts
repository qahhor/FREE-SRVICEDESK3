import { Component, Input, Output, EventEmitter, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CommentService } from '../../../../core/services/comment.service';
import { CommentRequest } from '../../../../core/models/comment.model';

@Component({
  selector: 'app-comment-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatSnackBarModule
  ],
  templateUrl: './comment-form.component.html',
  styleUrl: './comment-form.component.scss'
})
export class CommentFormComponent {
  @Input() ticketId!: string;
  @Output() commentAdded = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  private readonly commentService = inject(CommentService);
  private readonly snackBar = inject(MatSnackBar);

  commentForm: FormGroup;
  submitting = signal<boolean>(false);

  constructor() {
    this.commentForm = this.fb.group({
      content: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(10000)]],
      isInternal: [false]
    });
  }

  onSubmit(): void {
    if (this.commentForm.invalid || !this.ticketId) {
      return;
    }

    this.submitting.set(true);

    const request: CommentRequest = {
      content: this.commentForm.value.content.trim(),
      isInternal: this.commentForm.value.isInternal || false
    };

    this.commentService.addComment(this.ticketId, request).subscribe({
      next: (response) => {
        if (response.success) {
          this.snackBar.open('Comment added successfully', 'Close', {
            duration: 3000,
            horizontalPosition: 'end',
            verticalPosition: 'top',
            panelClass: ['success-snackbar']
          });

          this.commentForm.reset({ isInternal: false });
          this.commentAdded.emit();
        }
        this.submitting.set(false);
      },
      error: (err) => {
        console.error('Error adding comment:', err);
        this.snackBar.open('Failed to add comment. Please try again.', 'Close', {
          duration: 5000,
          horizontalPosition: 'end',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        });
        this.submitting.set(false);
      }
    });
  }

  cancel(): void {
    this.commentForm.reset({ isInternal: false });
  }

  get contentControl() {
    return this.commentForm.get('content');
  }

  get characterCount(): number {
    return this.contentControl?.value?.length || 0;
  }
}
