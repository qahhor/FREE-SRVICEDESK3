import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule
  ],
  template: `
    <div class="search-container" [class.large]="size === 'large'">
      <mat-form-field appearance="outline" class="search-field">
        <mat-icon matPrefix>search</mat-icon>
        <input 
          matInput 
          [placeholder]="placeholder"
          [(ngModel)]="searchQuery"
          (keyup.enter)="onSearch()"
        >
        @if (searchQuery) {
          <button matSuffix mat-icon-button (click)="clearSearch()">
            <mat-icon>clear</mat-icon>
          </button>
        }
      </mat-form-field>
      <button mat-flat-button color="primary" (click)="onSearch()" class="search-btn">
        Search
      </button>
    </div>
  `,
  styles: [`
    .search-container {
      display: flex;
      gap: 8px;
      width: 100%;
      max-width: 600px;
    }
    
    .search-container.large {
      max-width: 800px;
    }
    
    .search-field {
      flex: 1;
    }
    
    .search-field ::ng-deep .mat-mdc-form-field-subscript-wrapper {
      display: none;
    }
    
    .search-container.large .search-field ::ng-deep .mat-mdc-text-field-wrapper {
      height: 56px;
    }
    
    .search-btn {
      height: 56px;
    }
    
    @media (max-width: 480px) {
      .search-container {
        flex-direction: column;
      }
      
      .search-btn {
        width: 100%;
      }
    }
  `]
})
export class SearchBarComponent {
  @Input() placeholder = 'Search...';
  @Input() size: 'normal' | 'large' = 'normal';
  @Input() initialValue = '';
  @Output() search = new EventEmitter<string>();

  searchQuery = '';

  ngOnInit() {
    this.searchQuery = this.initialValue;
  }

  onSearch() {
    this.search.emit(this.searchQuery.trim());
  }

  clearSearch() {
    this.searchQuery = '';
    this.search.emit('');
  }
}
