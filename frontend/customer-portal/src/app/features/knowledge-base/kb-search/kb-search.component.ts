import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { KnowledgeService } from '../../../core/services/knowledge.service';
import { KBArticle, KBSearchResult } from '../../../core/models';

@Component({
  selector: 'app-kb-search',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatPaginatorModule,
    SearchBarComponent
  ],
  template: `
    <div class="kb-search">
      <!-- Search Header -->
      <section class="search-header">
        <div class="container">
          <a routerLink="/kb" class="back-link">
            <mat-icon>arrow_back</mat-icon>
            Back to Knowledge Base
          </a>
          <h1>Search Results</h1>
          <app-search-bar 
            [initialValue]="searchQuery"
            placeholder="Search articles..."
            (search)="onSearch($event)"
          ></app-search-bar>
        </div>
      </section>

      <div class="container content-area">
        @if (loading) {
          <div class="loading-container">
            <mat-spinner diameter="40"></mat-spinner>
          </div>
        } @else if (searchQuery && results.length === 0) {
          <mat-card class="no-results">
            <mat-icon>search_off</mat-icon>
            <h2>No results found</h2>
            <p>We couldn't find any articles matching "{{ searchQuery }}"</p>
            <h3>Suggestions:</h3>
            <ul>
              <li>Check your spelling</li>
              <li>Try different keywords</li>
              <li>Try more general terms</li>
            </ul>
            <a mat-stroked-button routerLink="/kb">Browse All Categories</a>
          </mat-card>
        } @else if (results.length > 0) {
          <p class="results-count">Found {{ totalCount }} article{{ totalCount !== 1 ? 's' : '' }} for "{{ searchQuery }}"</p>
          
          <div class="results-list">
            @for (article of results; track article.id) {
              <mat-card class="article-card" [routerLink]="['/kb/article', article.slug]">
                <div class="article-content">
                  <h3>{{ article.title }}</h3>
                  <p>{{ article.excerpt || (article.content | slice:0:200) }}...</p>
                  <div class="article-meta">
                    @if (article.category) {
                      <span class="category">{{ article.category.name }}</span>
                    }
                    <span class="views">
                      <mat-icon>visibility</mat-icon>
                      {{ article.viewCount }} views
                    </span>
                  </div>
                </div>
                <mat-icon class="arrow-icon">chevron_right</mat-icon>
              </mat-card>
            }
          </div>
          
          @if (totalCount > pageSize) {
            <mat-paginator
              [length]="totalCount"
              [pageSize]="pageSize"
              [pageIndex]="currentPage"
              [pageSizeOptions]="[10, 25, 50]"
              (page)="onPageChange($event)"
              showFirstLastButtons>
            </mat-paginator>
          }
        } @else {
          <mat-card class="prompt-card">
            <mat-icon>search</mat-icon>
            <h2>Search the Knowledge Base</h2>
            <p>Enter your search terms above to find relevant articles</p>
          </mat-card>
        }
      </div>
    </div>
  `,
  styles: [`
    .search-header {
      background: #f5f5f5;
      padding: 32px 16px;
    }
    
    .container {
      max-width: 900px;
      margin: 0 auto;
      padding: 0 16px;
    }
    
    .back-link {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      color: #3f51b5;
      text-decoration: none;
      margin-bottom: 16px;
      font-size: 0.875rem;
    }
    
    .back-link mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }
    
    .search-header h1 {
      margin: 0 0 24px 0;
      font-size: 1.5rem;
    }
    
    .content-area {
      padding: 32px 16px;
    }
    
    .loading-container {
      display: flex;
      justify-content: center;
      padding: 64px;
    }
    
    .results-count {
      color: #666;
      margin: 0 0 24px;
    }
    
    .no-results,
    .prompt-card {
      text-align: center;
      padding: 48px 24px;
    }
    
    .no-results mat-icon,
    .prompt-card mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #ccc;
    }
    
    .no-results h2,
    .prompt-card h2 {
      margin: 16px 0 8px;
      color: #666;
    }
    
    .no-results > p,
    .prompt-card p {
      margin: 0 0 24px;
      color: #999;
    }
    
    .no-results h3 {
      font-size: 1rem;
      color: #666;
      margin: 24px 0 8px;
    }
    
    .no-results ul {
      list-style: none;
      padding: 0;
      margin: 0 0 24px;
      color: #999;
    }
    
    .no-results ul li {
      padding: 4px 0;
    }
    
    .results-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-bottom: 24px;
    }
    
    .article-card {
      display: flex;
      align-items: center;
      padding: 20px;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    
    .article-card:hover {
      transform: translateX(4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .article-content {
      flex: 1;
    }
    
    .article-card h3 {
      margin: 0 0 8px 0;
      font-size: 1.1rem;
      color: #3f51b5;
    }
    
    .article-card p {
      margin: 0 0 12px 0;
      color: #666;
      font-size: 0.875rem;
      line-height: 1.5;
    }
    
    .article-meta {
      display: flex;
      gap: 16px;
      font-size: 0.8rem;
      color: #999;
    }
    
    .article-meta .category {
      background: #e8eaf6;
      color: #3f51b5;
      padding: 2px 8px;
      border-radius: 4px;
    }
    
    .article-meta .views {
      display: flex;
      align-items: center;
      gap: 4px;
    }
    
    .article-meta .views mat-icon {
      font-size: 14px;
      width: 14px;
      height: 14px;
    }
    
    .arrow-icon {
      color: #ccc;
    }
  `]
})
export class KbSearchComponent implements OnInit {
  searchQuery = '';
  results: KBArticle[] = [];
  totalCount = 0;
  loading = false;
  currentPage = 0;
  pageSize = 10;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private knowledgeService: KnowledgeService
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.searchQuery = params['q'] || '';
      if (this.searchQuery) {
        this.search();
      }
    });
  }

  search() {
    this.loading = true;
    this.knowledgeService.searchArticles(this.searchQuery, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.data) {
          this.results = response.data.articles;
          this.totalCount = response.data.totalCount;
        }
      },
      error: () => {
        this.loading = false;
        this.results = [];
        this.totalCount = 0;
      }
    });
  }

  onSearch(query: string) {
    this.searchQuery = query;
    this.currentPage = 0;
    this.router.navigate(['/kb/search'], { queryParams: { q: query } });
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.search();
  }
}
