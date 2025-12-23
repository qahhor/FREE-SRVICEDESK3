import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { KnowledgeService } from '../../../core/services/knowledge.service';
import { KBCategory, KBArticle } from '../../../core/models';

@Component({
  selector: 'app-kb-category',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="kb-category">
      @if (loading) {
        <div class="loading-container">
          <mat-spinner diameter="40"></mat-spinner>
        </div>
      } @else if (!category) {
        <div class="container">
          <mat-card class="not-found">
            <mat-icon>folder_off</mat-icon>
            <h2>Category Not Found</h2>
            <p>The category you're looking for doesn't exist.</p>
            <a mat-flat-button color="primary" routerLink="/kb">Back to Knowledge Base</a>
          </mat-card>
        </div>
      } @else {
        <!-- Category Header -->
        <section class="category-header">
          <div class="container">
            <!-- Breadcrumb -->
            <nav class="breadcrumb">
              <a routerLink="/kb">Knowledge Base</a>
              <mat-icon>chevron_right</mat-icon>
              <span>{{ category.name }}</span>
            </nav>
            
            <div class="header-content">
              <mat-icon class="category-icon">{{ category.icon || 'folder' }}</mat-icon>
              <div>
                <h1>{{ category.name }}</h1>
                @if (category.description) {
                  <p>{{ category.description }}</p>
                }
              </div>
            </div>
          </div>
        </section>

        <div class="container content-area">
          <!-- Subcategories -->
          @if (category.children && category.children.length > 0) {
            <section class="subcategories-section">
              <h2>Subcategories</h2>
              <div class="subcategories-grid">
                @for (sub of category.children; track sub.id) {
                  <mat-card class="subcategory-card" [routerLink]="['/kb/category', sub.slug]">
                    <mat-icon>{{ sub.icon || 'folder' }}</mat-icon>
                    <h3>{{ sub.name }}</h3>
                    <span>{{ sub.articleCount }} articles</span>
                  </mat-card>
                }
              </div>
            </section>
          }

          <!-- Articles List -->
          @if (articles.length > 0) {
            <section class="articles-section">
              <h2>Articles in this Category</h2>
              <div class="articles-list">
                @for (article of articles; track article.id) {
                  <mat-card class="article-card" [routerLink]="['/kb/article', article.slug]">
                    <div class="article-content">
                      <h3>{{ article.title }}</h3>
                      <p>{{ article.excerpt || (article.content | slice:0:150) }}...</p>
                      <div class="article-meta">
                        <span class="views">
                          <mat-icon>visibility</mat-icon>
                          {{ article.viewCount }} views
                        </span>
                        @if (article.helpfulCount > 0) {
                          <span class="helpful">
                            <mat-icon>thumb_up</mat-icon>
                            {{ article.helpfulCount }} found this helpful
                          </span>
                        }
                      </div>
                    </div>
                    <mat-icon class="arrow-icon">chevron_right</mat-icon>
                  </mat-card>
                }
              </div>
            </section>
          } @else if (!category.children || category.children.length === 0) {
            <mat-card class="empty-state">
              <mat-icon>article</mat-icon>
              <h2>No Articles Yet</h2>
              <p>There are no articles in this category yet.</p>
              <a mat-stroked-button routerLink="/kb">Browse Other Categories</a>
            </mat-card>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .loading-container {
      display: flex;
      justify-content: center;
      padding: 64px;
    }
    
    .container {
      max-width: 1000px;
      margin: 0 auto;
      padding: 0 16px;
    }
    
    .not-found,
    .empty-state {
      text-align: center;
      padding: 64px 24px;
      margin-top: 32px;
    }
    
    .not-found mat-icon,
    .empty-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #ccc;
    }
    
    .not-found h2,
    .empty-state h2 {
      margin: 16px 0 8px;
      color: #666;
    }
    
    .not-found p,
    .empty-state p {
      margin: 0 0 24px;
      color: #999;
    }
    
    .category-header {
      background: linear-gradient(135deg, #3f51b5 0%, #303f9f 100%);
      color: white;
      padding: 32px 16px;
    }
    
    .breadcrumb {
      display: flex;
      align-items: center;
      gap: 4px;
      margin-bottom: 16px;
      font-size: 0.875rem;
    }
    
    .breadcrumb a {
      color: rgba(255, 255, 255, 0.8);
      text-decoration: none;
    }
    
    .breadcrumb a:hover {
      color: white;
    }
    
    .breadcrumb mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      opacity: 0.7;
    }
    
    .header-content {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    
    .category-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      opacity: 0.9;
    }
    
    .header-content h1 {
      margin: 0 0 8px 0;
      font-size: 1.5rem;
    }
    
    .header-content p {
      margin: 0;
      opacity: 0.9;
    }
    
    .content-area {
      padding: 32px 16px;
    }
    
    .subcategories-section,
    .articles-section {
      margin-bottom: 32px;
    }
    
    .subcategories-section h2,
    .articles-section h2 {
      margin: 0 0 16px;
      font-size: 1.25rem;
    }
    
    .subcategories-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 16px;
    }
    
    .subcategory-card {
      padding: 20px;
      text-align: center;
      cursor: pointer;
      transition: transform 0.2s;
    }
    
    .subcategory-card:hover {
      transform: translateY(-2px);
    }
    
    .subcategory-card mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #3f51b5;
    }
    
    .subcategory-card h3 {
      margin: 8px 0 4px;
      font-size: 1rem;
    }
    
    .subcategory-card span {
      color: #999;
      font-size: 0.875rem;
    }
    
    .articles-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    
    .article-card {
      display: flex;
      align-items: center;
      padding: 16px 20px;
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
      margin: 0 0 8px 0;
      color: #666;
      font-size: 0.875rem;
    }
    
    .article-meta {
      display: flex;
      gap: 16px;
      font-size: 0.8rem;
      color: #999;
    }
    
    .article-meta span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
    
    .article-meta mat-icon {
      font-size: 14px;
      width: 14px;
      height: 14px;
    }
    
    .arrow-icon {
      color: #ccc;
    }
  `]
})
export class KbCategoryComponent implements OnInit {
  category: KBCategory | null = null;
  articles: KBArticle[] = [];
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private knowledgeService: KnowledgeService
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const slug = params.get('slug');
      if (slug) {
        this.loadCategory(slug);
      }
    });
  }

  loadCategory(slug: string) {
    this.loading = true;
    
    this.knowledgeService.getCategory(slug).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.category = response.data;
          this.loadArticles(slug);
        } else {
          this.loading = false;
          this.category = null;
        }
      },
      error: () => {
        this.loading = false;
        this.category = null;
      }
    });
  }

  loadArticles(categorySlug: string) {
    this.knowledgeService.getArticles(0, 50, categorySlug).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.data) {
          this.articles = response.data.content;
        }
      },
      error: () => {
        this.loading = false;
        this.articles = [];
      }
    });
  }
}
