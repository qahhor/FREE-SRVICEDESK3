import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { KnowledgeService } from '../../../core/services/knowledge.service';
import { KBArticle } from '../../../core/models';

@Component({
  selector: 'app-kb-article',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatSnackBarModule
  ],
  template: `
    <div class="kb-article">
      @if (loading) {
        <div class="loading-container">
          <mat-spinner diameter="40"></mat-spinner>
        </div>
      } @else if (!article) {
        <div class="container">
          <mat-card class="not-found">
            <mat-icon>article</mat-icon>
            <h2>Article Not Found</h2>
            <p>The article you're looking for doesn't exist or has been removed.</p>
            <a mat-flat-button color="primary" routerLink="/kb">Back to Knowledge Base</a>
          </mat-card>
        </div>
      } @else {
        <div class="article-layout">
          <!-- Main Content -->
          <article class="article-main">
            <!-- Breadcrumb -->
            <nav class="breadcrumb">
              <a routerLink="/kb">Knowledge Base</a>
              @if (article.category) {
                <mat-icon>chevron_right</mat-icon>
                <a [routerLink]="['/kb/category', article.category.slug]">{{ article.category.name }}</a>
              }
              <mat-icon>chevron_right</mat-icon>
              <span>{{ article.title }}</span>
            </nav>

            <!-- Article Header -->
            <header class="article-header">
              <h1>{{ article.title }}</h1>
              <div class="article-meta">
                <span class="date">
                  <mat-icon>calendar_today</mat-icon>
                  {{ formatDate(article.publishedAt || article.createdAt) }}
                </span>
                <span class="views">
                  <mat-icon>visibility</mat-icon>
                  {{ article.viewCount }} views
                </span>
                @if (article.author) {
                  <span class="author">
                    <mat-icon>person</mat-icon>
                    {{ article.author.fullName }}
                  </span>
                }
              </div>
            </header>

            <!-- Table of Contents -->
            @if (tableOfContents.length > 0) {
              <nav class="toc">
                <h3>Table of Contents</h3>
                <ul>
                  @for (heading of tableOfContents; track heading.id) {
                    <li [class]="'level-' + heading.level">
                      <a (click)="scrollTo(heading.id)">{{ heading.text }}</a>
                    </li>
                  }
                </ul>
              </nav>
            }

            <!-- Article Content -->
            <div class="article-content" [innerHTML]="renderedContent"></div>

            <mat-divider></mat-divider>

            <!-- Feedback Section -->
            <section class="feedback-section">
              <h3>Was this article helpful?</h3>
              <div class="feedback-buttons">
                <button mat-stroked-button (click)="submitFeedback(true)" [disabled]="feedbackSubmitted">
                  <mat-icon>thumb_up</mat-icon>
                  Yes
                </button>
                <button mat-stroked-button (click)="submitFeedback(false)" [disabled]="feedbackSubmitted">
                  <mat-icon>thumb_down</mat-icon>
                  No
                </button>
              </div>
              @if (feedbackSubmitted) {
                <p class="feedback-thanks">Thank you for your feedback!</p>
              }
            </section>

            <!-- Share Section -->
            <section class="share-section">
              <h3>Share this article</h3>
              <div class="share-buttons">
                <button mat-icon-button (click)="copyLink()">
                  <mat-icon>link</mat-icon>
                </button>
                <button mat-icon-button (click)="printArticle()">
                  <mat-icon>print</mat-icon>
                </button>
              </div>
            </section>
          </article>

          <!-- Sidebar -->
          <aside class="article-sidebar">
            @if (relatedArticles.length > 0) {
              <mat-card class="related-articles">
                <h3>Related Articles</h3>
                <ul>
                  @for (related of relatedArticles; track related.id) {
                    <li>
                      <a [routerLink]="['/kb/article', related.slug]">{{ related.title }}</a>
                    </li>
                  }
                </ul>
              </mat-card>
            }

            <mat-card class="need-help">
              <h3>Still need help?</h3>
              <p>Can't find what you're looking for?</p>
              <a mat-flat-button color="primary" routerLink="/tickets/submit">Submit a Ticket</a>
            </mat-card>
          </aside>
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
      max-width: 800px;
      margin: 0 auto;
      padding: 32px 16px;
    }
    
    .not-found {
      text-align: center;
      padding: 64px 24px;
    }
    
    .not-found mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #ccc;
    }
    
    .not-found h2 {
      margin: 16px 0 8px;
      color: #666;
    }
    
    .not-found p {
      margin: 0 0 24px;
      color: #999;
    }
    
    .article-layout {
      max-width: 1200px;
      margin: 0 auto;
      padding: 32px 16px;
      display: grid;
      grid-template-columns: 1fr 300px;
      gap: 32px;
    }
    
    .article-main {
      min-width: 0;
    }
    
    .breadcrumb {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 4px;
      margin-bottom: 24px;
      font-size: 0.875rem;
      color: #666;
    }
    
    .breadcrumb a {
      color: #3f51b5;
      text-decoration: none;
    }
    
    .breadcrumb a:hover {
      text-decoration: underline;
    }
    
    .breadcrumb mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      color: #ccc;
    }
    
    .article-header h1 {
      margin: 0 0 16px;
      font-size: 2rem;
      line-height: 1.3;
    }
    
    .article-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      color: #666;
      font-size: 0.875rem;
      margin-bottom: 24px;
    }
    
    .article-meta span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
    
    .article-meta mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }
    
    .toc {
      background: #f5f5f5;
      border-radius: 8px;
      padding: 16px 20px;
      margin-bottom: 24px;
    }
    
    .toc h3 {
      margin: 0 0 12px;
      font-size: 1rem;
      font-weight: 500;
    }
    
    .toc ul {
      list-style: none;
      margin: 0;
      padding: 0;
    }
    
    .toc li {
      padding: 4px 0;
    }
    
    .toc li.level-2 {
      padding-left: 16px;
    }
    
    .toc li.level-3 {
      padding-left: 32px;
    }
    
    .toc a {
      color: #3f51b5;
      text-decoration: none;
      cursor: pointer;
    }
    
    .toc a:hover {
      text-decoration: underline;
    }
    
    .article-content {
      line-height: 1.7;
      font-size: 1rem;
    }
    
    .article-content :deep(h1),
    .article-content :deep(h2),
    .article-content :deep(h3) {
      margin-top: 32px;
      margin-bottom: 16px;
    }
    
    .article-content :deep(p) {
      margin-bottom: 16px;
    }
    
    .article-content :deep(ul),
    .article-content :deep(ol) {
      margin-bottom: 16px;
      padding-left: 24px;
    }
    
    .article-content :deep(code) {
      background: #f5f5f5;
      padding: 2px 6px;
      border-radius: 4px;
      font-family: monospace;
    }
    
    .article-content :deep(pre) {
      background: #263238;
      color: #fff;
      padding: 16px;
      border-radius: 8px;
      overflow-x: auto;
    }
    
    .article-content :deep(pre code) {
      background: none;
      padding: 0;
    }
    
    mat-divider {
      margin: 32px 0;
    }
    
    .feedback-section,
    .share-section {
      margin-bottom: 24px;
    }
    
    .feedback-section h3,
    .share-section h3 {
      margin: 0 0 12px;
      font-size: 1rem;
      font-weight: 500;
    }
    
    .feedback-buttons {
      display: flex;
      gap: 12px;
    }
    
    .feedback-buttons button mat-icon {
      margin-right: 4px;
    }
    
    .feedback-thanks {
      margin: 12px 0 0;
      color: #4caf50;
    }
    
    .share-buttons {
      display: flex;
      gap: 8px;
    }
    
    .article-sidebar {
      position: sticky;
      top: 80px;
      height: fit-content;
    }
    
    .article-sidebar mat-card {
      padding: 20px;
      margin-bottom: 16px;
    }
    
    .article-sidebar h3 {
      margin: 0 0 12px;
      font-size: 1rem;
      font-weight: 500;
    }
    
    .related-articles ul {
      list-style: none;
      margin: 0;
      padding: 0;
    }
    
    .related-articles li {
      padding: 8px 0;
      border-bottom: 1px solid #eee;
    }
    
    .related-articles li:last-child {
      border-bottom: none;
    }
    
    .related-articles a {
      color: #3f51b5;
      text-decoration: none;
      font-size: 0.875rem;
    }
    
    .related-articles a:hover {
      text-decoration: underline;
    }
    
    .need-help {
      text-align: center;
    }
    
    .need-help p {
      color: #666;
      margin: 0 0 16px;
    }
    
    @media (max-width: 900px) {
      .article-layout {
        grid-template-columns: 1fr;
      }
      
      .article-sidebar {
        position: static;
      }
    }
    
    @media (max-width: 600px) {
      .article-header h1 {
        font-size: 1.5rem;
      }
    }
    
    @media print {
      .breadcrumb,
      .toc,
      .feedback-section,
      .share-section,
      .article-sidebar {
        display: none;
      }
    }
  `]
})
export class KbArticleComponent implements OnInit {
  article: KBArticle | null = null;
  relatedArticles: KBArticle[] = [];
  tableOfContents: { id: string; text: string; level: number }[] = [];
  renderedContent = '';
  loading = true;
  feedbackSubmitted = false;

  constructor(
    private route: ActivatedRoute,
    private knowledgeService: KnowledgeService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const slug = params.get('slug');
      if (slug) {
        this.loadArticle(slug);
      }
    });
  }

  loadArticle(slug: string) {
    this.loading = true;
    this.feedbackSubmitted = false;
    
    this.knowledgeService.getArticle(slug).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.data) {
          this.article = response.data;
          this.processContent();
          this.loadRelatedArticles();
        }
      },
      error: () => {
        this.loading = false;
        this.article = null;
      }
    });
  }

  processContent() {
    if (!this.article) return;
    
    // Simple markdown-like rendering
    let content = this.article.content;
    
    // Generate table of contents and add IDs to headings
    const headingRegex = /^(#{1,3})\s+(.+)$/gm;
    let match;
    let headingId = 0;
    
    while ((match = headingRegex.exec(content)) !== null) {
      const level = match[1].length;
      const text = match[2];
      const id = `heading-${headingId++}`;
      
      this.tableOfContents.push({ id, text, level });
    }
    
    // Convert markdown to HTML (basic implementation)
    content = content
      // Headings
      .replace(/^### (.+)$/gm, '<h3 id="heading-$1">$1</h3>')
      .replace(/^## (.+)$/gm, '<h2 id="heading-$1">$1</h2>')
      .replace(/^# (.+)$/gm, '<h1 id="heading-$1">$1</h1>')
      // Bold and italic
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.+?)\*/g, '<em>$1</em>')
      // Code blocks
      .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code class="language-$1">$2</code></pre>')
      // Inline code
      .replace(/`(.+?)`/g, '<code>$1</code>')
      // Links
      .replace(/\[(.+?)\]\((.+?)\)/g, '<a href="$2" target="_blank">$1</a>')
      // Line breaks
      .replace(/\n\n/g, '</p><p>')
      .replace(/\n/g, '<br>');
    
    this.renderedContent = `<p>${content}</p>`;
  }

  loadRelatedArticles() {
    if (!this.article) return;
    
    this.knowledgeService.getRelatedArticles(this.article.id, 5).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.relatedArticles = response.data;
        }
      }
    });
  }

  scrollTo(id: string) {
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }

  submitFeedback(helpful: boolean) {
    if (!this.article) return;
    
    this.knowledgeService.submitFeedback({
      articleId: this.article.id,
      helpful
    }).subscribe({
      next: () => {
        this.feedbackSubmitted = true;
      },
      error: () => {
        this.feedbackSubmitted = true;
      }
    });
  }

  copyLink() {
    navigator.clipboard.writeText(window.location.href);
    this.snackBar.open('Link copied to clipboard', 'OK', { duration: 2000 });
  }

  printArticle() {
    window.print();
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
  }
}
