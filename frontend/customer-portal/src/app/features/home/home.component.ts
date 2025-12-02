import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { SearchBarComponent } from '../../shared/components/search-bar/search-bar.component';
import { KnowledgeService } from '../../core/services/knowledge.service';
import { KBArticle, KBCategory } from '../../core/models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    SearchBarComponent
  ],
  template: `
    <!-- Hero Section -->
    <section class="hero">
      <div class="container">
        <h1>How can we help you today?</h1>
        <p>Search our knowledge base or submit a support request</p>
        <app-search-bar 
          placeholder="Search for help articles..."
          size="large"
          (search)="onSearch($event)"
        ></app-search-bar>
      </div>
    </section>

    <!-- Quick Actions -->
    <section class="quick-actions container">
      <div class="actions-grid">
        <mat-card class="action-card" routerLink="/tickets/submit">
          <mat-icon>add_circle</mat-icon>
          <h3>Submit Ticket</h3>
          <p>Create a new support request</p>
        </mat-card>
        
        <mat-card class="action-card" routerLink="/track">
          <mat-icon>search</mat-icon>
          <h3>Track Ticket</h3>
          <p>Check your ticket status</p>
        </mat-card>
        
        <mat-card class="action-card" routerLink="/kb">
          <mat-icon>menu_book</mat-icon>
          <h3>Knowledge Base</h3>
          <p>Browse help articles</p>
        </mat-card>
      </div>
    </section>

    <!-- Categories Section -->
    @if (categories.length > 0) {
      <section class="categories-section container">
        <h2>Browse by Category</h2>
        <div class="categories-grid">
          @for (category of categories; track category.id) {
            <mat-card class="category-card" [routerLink]="['/kb/category', category.slug]">
              <mat-icon>{{ category.icon || 'folder' }}</mat-icon>
              <h3>{{ category.name }}</h3>
              <p>{{ category.articleCount }} articles</p>
            </mat-card>
          }
        </div>
      </section>
    }

    <!-- Popular Articles -->
    @if (popularArticles.length > 0) {
      <section class="articles-section container">
        <h2>Popular Articles</h2>
        <div class="articles-list">
          @for (article of popularArticles; track article.id) {
            <mat-card class="article-card" [routerLink]="['/kb/article', article.slug]">
              <mat-card-content>
                <h3>{{ article.title }}</h3>
                <p>{{ article.excerpt || (article.content | slice:0:150) }}...</p>
                <div class="article-meta">
                  <span class="category">{{ article.category?.name }}</span>
                  <span class="views">{{ article.viewCount }} views</span>
                </div>
              </mat-card-content>
            </mat-card>
          }
        </div>
      </section>
    }

    <!-- Contact Section -->
    <section class="contact-section">
      <div class="container">
        <h2>Still need help?</h2>
        <p>Our support team is here to assist you</p>
        <div class="contact-options">
          <div class="contact-option">
            <mat-icon>email</mat-icon>
            <h4>Email Us</h4>
            <a href="mailto:support&#64;servicedesk.io">support&#64;servicedesk.io</a>
          </div>
          <div class="contact-option">
            <mat-icon>phone</mat-icon>
            <h4>Call Us</h4>
            <a href="tel:+15551234567">+1 (555) 123-4567</a>
          </div>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .hero {
      background: linear-gradient(135deg, #3f51b5 0%, #303f9f 100%);
      color: white;
      padding: 80px 16px;
      text-align: center;
    }
    
    .hero h1 {
      font-size: 2.5rem;
      margin: 0 0 16px 0;
    }
    
    .hero p {
      font-size: 1.25rem;
      opacity: 0.9;
      margin: 0 0 32px 0;
    }
    
    .hero app-search-bar {
      display: flex;
      justify-content: center;
    }
    
    .quick-actions {
      margin-top: -40px;
      padding-bottom: 48px;
    }
    
    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 24px;
    }
    
    .action-card {
      padding: 32px;
      text-align: center;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    
    .action-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    }
    
    .action-card mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #3f51b5;
      margin-bottom: 16px;
    }
    
    .action-card h3 {
      margin: 0 0 8px 0;
      font-size: 1.25rem;
    }
    
    .action-card p {
      margin: 0;
      color: #666;
    }
    
    .categories-section,
    .articles-section {
      padding: 48px 16px;
    }
    
    .categories-section h2,
    .articles-section h2 {
      margin: 0 0 24px 0;
      font-size: 1.5rem;
    }
    
    .categories-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
    }
    
    .category-card {
      padding: 24px;
      text-align: center;
      cursor: pointer;
      transition: transform 0.2s;
    }
    
    .category-card:hover {
      transform: translateY(-2px);
    }
    
    .category-card mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #3f51b5;
    }
    
    .category-card h3 {
      margin: 8px 0 4px 0;
      font-size: 1rem;
    }
    
    .category-card p {
      margin: 0;
      color: #666;
      font-size: 0.875rem;
    }
    
    .articles-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    
    .article-card {
      cursor: pointer;
      transition: transform 0.2s;
    }
    
    .article-card:hover {
      transform: translateX(4px);
    }
    
    .article-card h3 {
      margin: 0 0 8px 0;
      font-size: 1.1rem;
      color: #3f51b5;
    }
    
    .article-card p {
      margin: 0 0 12px 0;
      color: #666;
    }
    
    .article-meta {
      display: flex;
      gap: 16px;
      font-size: 0.875rem;
      color: #999;
    }
    
    .contact-section {
      background: #f5f5f5;
      padding: 64px 16px;
      text-align: center;
    }
    
    .contact-section h2 {
      margin: 0 0 8px 0;
    }
    
    .contact-section > div > p {
      margin: 0 0 32px 0;
      color: #666;
    }
    
    .contact-options {
      display: flex;
      justify-content: center;
      gap: 64px;
    }
    
    .contact-option {
      text-align: center;
    }
    
    .contact-option mat-icon {
      font-size: 36px;
      width: 36px;
      height: 36px;
      color: #3f51b5;
    }
    
    .contact-option h4 {
      margin: 8px 0;
    }
    
    .contact-option a {
      color: #3f51b5;
      text-decoration: none;
    }
    
    @media (max-width: 768px) {
      .hero {
        padding: 48px 16px;
      }
      
      .hero h1 {
        font-size: 1.75rem;
      }
      
      .contact-options {
        flex-direction: column;
        gap: 32px;
      }
    }
  `]
})
export class HomeComponent implements OnInit {
  categories: KBCategory[] = [];
  popularArticles: KBArticle[] = [];

  constructor(
    private router: Router,
    private knowledgeService: KnowledgeService
  ) {}

  ngOnInit() {
    this.loadCategories();
    this.loadPopularArticles();
  }

  loadCategories() {
    this.knowledgeService.getCategories().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.categories = response.data.slice(0, 6);
        }
      }
    });
  }

  loadPopularArticles() {
    this.knowledgeService.getPopularArticles(5).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.popularArticles = response.data;
        }
      }
    });
  }

  onSearch(query: string) {
    if (query.trim()) {
      this.router.navigate(['/kb/search'], { queryParams: { q: query } });
    }
  }
}
