import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Comment, CommentRequest } from '../models/comment.model';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/tickets`;

  /**
   * Add a comment to a ticket
   */
  addComment(ticketId: string, request: CommentRequest): Observable<ApiResponse<Comment>> {
    return this.http.post<ApiResponse<Comment>>(
      `${this.apiUrl}/${ticketId}/comments`,
      request
    );
  }

  /**
   * Get all comments for a ticket
   */
  getComments(ticketId: string, includeInternal: boolean = true): Observable<ApiResponse<Comment[]>> {
    const params = new HttpParams().set('includeInternal', includeInternal.toString());
    return this.http.get<ApiResponse<Comment[]>>(
      `${this.apiUrl}/${ticketId}/comments`,
      { params }
    );
  }

  /**
   * Get a specific comment
   */
  getComment(commentId: string): Observable<ApiResponse<Comment>> {
    return this.http.get<ApiResponse<Comment>>(
      `${environment.apiUrl}/comments/${commentId}`
    );
  }

  /**
   * Update a comment
   */
  updateComment(commentId: string, request: CommentRequest): Observable<ApiResponse<Comment>> {
    return this.http.put<ApiResponse<Comment>>(
      `${environment.apiUrl}/comments/${commentId}`,
      request
    );
  }

  /**
   * Delete a comment
   */
  deleteComment(commentId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${environment.apiUrl}/comments/${commentId}`
    );
  }

  /**
   * Get comment count for a ticket
   */
  getCommentCount(ticketId: string): Observable<ApiResponse<number>> {
    return this.http.get<ApiResponse<number>>(
      `${this.apiUrl}/${ticketId}/comments/count`
    );
  }
}
