import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { KnowledgeService } from '../../../core/services/knowledge.service';
import { KBCategory, KBArticle } from '../../../core/models';

@Component({
  selector: 'app-kb-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    SearchBarComponent
  ],
  template: `
    <div class="kb-home">
      <!-- Hero Section -->
      <section class="kb-hero">
        <div class="container">
          <h1>Knowledge Base</h1>
          <p>Find answers to your questions</p>
          <app-search-bar 
            placeholder="Search articles..."
            size="large"
            (search)="onSearch($event)"
          ></app-search-bar>
        </div>
      </section>

      <div class="container content-area">
        @if (loading) {
          <div class="loading-container">
            <mat-spinner diameter="40"></mat-spinner>
          </div>
        } @else {
          <!-- Categories Grid -->
          @if (categories.length > 0) {
            <section class="categories-section">
              <h2>Browse by Category</h2>
              <div class="categories-grid">
                @for (category of categories; track category.id) {
                  <mat-card class="category-card" [routerLink]="['/kb/category', category.slug]">
                    <mat-icon>{{ category.icon || 'folder' }}</mat-icon>
                    <h3>{{ category.name }}</h3>
                    <p class="description">{{ category.description }}</p>
                    <span class="article-count">{{ category.articleCount }} articles</span>
                  </mat-card>
                }
              </div>
            </section>
          }

          <!-- Popular Articles -->
          @if (popularArticles.length > 0) {
            <section class="articles-section">
              <div class="section-header">
                <h2>Popular Articles</h2>
                <mat-icon>star</mat-icon>
              </div>
              <div class="articles-list">
                @for (article of popularArticles; track article.id) {
                  <mat-card class="article-card" [routerLink]="['/kb/article', article.slug]">
                    <div class="article-content">
                      <h3>{{ article.title }}</h3>
                      <p>{{ article.excerpt || (article.content | slice:0:150) }}...</p>
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
            </section>
          }

          <!-- Recent Articles -->
          @if (recentArticles.length > 0) {
            <section class="articles-section">
              <div class="section-header">
                <h2>Recent Articles</h2>
                <mat-icon>schedule</mat-icon>
              </div>
              <div class="articles-list">
                @for (article of recentArticles; track article.id) {
                  <mat-card class="article-card" [routerLink]="['/kb/article', article.slug]">
                    <div class="article-content">
                      <h3>{{ article.title }}</h3>
                      <p>{{ article.excerpt || (article.content | slice:0:150) }}...</p>
                      <div class="article-meta">
                        @if (article.category) {
                          <span class="category">{{ article.category.name }}</span>
                        }
                        <span class="date">{{ formatDate(article.publishedAt || article.createdAt) }}</span>
                      </div>
                    </div>
                    <mat-icon class="arrow-icon">chevron_right</mat-icon>
                  </mat-card>
                }
              </div>
            </section>
          }

          <!-- Empty State -->
          @if (categories.length === 0 && popularArticles.length === 0) {
            <mat-card class="empty-state">
              <mat-icon>menu_book</mat-icon>
              <h2>No Articles Yet</h2>
              <p>Our knowledge base is being built. Check back soon!</p>
            </mat-card>
          }
        }
      </div>
    </div>
  `,
  styles: [`
    .kb-hero {
      background: linear-gradient(135deg, #3f51b5 0%, #303f9f 100%);
      color: white;
      padding: 64px 16px;
      text-align: center;
    }
    
    .kb-hero h1 {
      margin: 0 0 8px 0;
      font-size: 2rem;
    }
    
    .kb-hero p {
      margin: 0 0 32px 0;
      font-size: 1.1rem;
      opacity: 0.9;
    }
    
    .kb-hero app-search-bar {
      display: flex;
      justify-content: center;
    }
    
    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 16px;
    }
    
    .content-area {
      padding: 48px 16px;
    }
    
    .loading-container {
      display: flex;
      justify-content: center;
      padding: 64px;
    }
    
    .categories-section,
    .articles-section {
      margin-bottom: 48px;
    }
    
    .section-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 24px;
    }
    
    .section-header h2,
    .categories-section h2 {
      margin: 0;
      font-size: 1.5rem;
    }
    
    .section-header mat-icon {
      color: #ffc107;
    }
    
    .categories-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 24px;
    }
    
    .category-card {
      padding: 24px;
      text-align: center;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    
    .category-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    }
    
    .category-card mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #3f51b5;
      margin-bottom: 16px;
    }
    
    .category-card h3 {
      margin: 0 0 8px 0;
      font-size: 1.1rem;
    }
    
    .category-card .description {
      margin: 0 0 12px 0;
      color: #666;
      font-size: 0.875rem;
    }
    
    .category-card .article-count {
      color: #999;
      font-size: 0.875rem;
    }
    
    .articles-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
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
      font-size: 1rem;
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
    
    .empty-state {
      text-align: center;
      padding: 64px 24px;
    }
    
    .empty-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #ccc;
    }
    
    .empty-state h2 {
      margin: 16px 0 8px;
      color: #666;
    }
    
    .empty-state p {
      margin: 0;
      color: #999;
    }
    
    @media (max-width: 768px) {
      .kb-hero {
        padding: 40px 16px;
      }
      
      .kb-hero h1 {
        font-size: 1.5rem;
      }
    }
  `]
})
export class KbHomeComponent implements OnInit {
  categories: KBCategory[] = [];
  popularArticles: KBArticle[] = [];
  recentArticles: KBArticle[] = [];
  loading = true;

  constructor(
    private router: Router,
    private knowledgeService: KnowledgeService
  ) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loading = true;
    
    // Load categories
    this.knowledgeService.getCategories().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.categories = response.data;
        }
        this.checkLoading();
      },
      error: () => this.checkLoading()
    });

    // Load popular articles
    this.knowledgeService.getPopularArticles(5).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.popularArticles = response.data;
        }
        this.checkLoading();
      },
      error: () => this.checkLoading()
    });

    // Load recent articles
    this.knowledgeService.getRecentArticles(5).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.recentArticles = response.data;
        }
        this.checkLoading();
      },
      error: () => this.checkLoading()
    });
  }

  private loadCount = 0;
  checkLoading() {
    this.loadCount++;
    if (this.loadCount >= 3) {
      this.loading = false;
    }
  }

  onSearch(query: string) {
    if (query.trim()) {
      this.router.navigate(['/kb/search'], { queryParams: { q: query } });
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString();
  }
}
