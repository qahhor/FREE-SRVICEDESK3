import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  KBArticle, 
  KBCategory, 
  KBSearchResult,
  ArticleFeedback,
  ApiResponse,
  Page 
} from '../models';

@Injectable({
  providedIn: 'root'
})
export class KnowledgeService {
  
  constructor(private http: HttpClient) {}

  // Get all categories
  getCategories(): Observable<ApiResponse<KBCategory[]>> {
    return this.http.get<ApiResponse<KBCategory[]>>(
      `${environment.apiUrl}/public/kb/categories`
    );
  }

  // Get category by slug
  getCategory(slug: string): Observable<ApiResponse<KBCategory>> {
    return this.http.get<ApiResponse<KBCategory>>(
      `${environment.apiUrl}/public/kb/categories/${slug}`
    );
  }

  // Get articles list
  getArticles(page = 0, size = 10, categorySlug?: string): Observable<ApiResponse<Page<KBArticle>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (categorySlug) {
      params = params.set('category', categorySlug);
    }

    return this.http.get<ApiResponse<Page<KBArticle>>>(
      `${environment.apiUrl}/public/kb/articles`,
      { params }
    );
  }

  // Get popular articles
  getPopularArticles(limit = 5): Observable<ApiResponse<KBArticle[]>> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<ApiResponse<KBArticle[]>>(
      `${environment.apiUrl}/public/kb/articles/popular`,
      { params }
    );
  }

  // Get recent articles
  getRecentArticles(limit = 5): Observable<ApiResponse<KBArticle[]>> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<ApiResponse<KBArticle[]>>(
      `${environment.apiUrl}/public/kb/articles/recent`,
      { params }
    );
  }

  // Get article by slug
  getArticle(slug: string): Observable<ApiResponse<KBArticle>> {
    return this.http.get<ApiResponse<KBArticle>>(
      `${environment.apiUrl}/public/kb/articles/${slug}`
    );
  }

  // Search articles
  searchArticles(query: string, page = 0, size = 10): Observable<ApiResponse<KBSearchResult>> {
    const params = new HttpParams()
      .set('q', query)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<KBSearchResult>>(
      `${environment.apiUrl}/public/kb/search`,
      { params }
    );
  }

  // Get related articles
  getRelatedArticles(articleId: string, limit = 5): Observable<ApiResponse<KBArticle[]>> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<ApiResponse<KBArticle[]>>(
      `${environment.apiUrl}/public/kb/articles/${articleId}/related`,
      { params }
    );
  }

  // Submit article feedback
  submitFeedback(feedback: ArticleFeedback): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(
      `${environment.apiUrl}/public/kb/articles/${feedback.articleId}/feedback`,
      feedback
    );
  }
}
