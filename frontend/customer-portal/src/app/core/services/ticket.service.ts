import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  Ticket, 
  CreateTicketRequest, 
  TrackTicketRequest,
  TrackTicketResponse,
  Comment,
  CreateCommentRequest,
  Attachment,
  ApiResponse,
  Page 
} from '../models';

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  
  constructor(private http: HttpClient) {}

  // Get customer's tickets
  getMyTickets(page = 0, size = 10, status?: string, search?: string): Observable<ApiResponse<Page<Ticket>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (status) {
      params = params.set('status', status);
    }
    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<ApiResponse<Page<Ticket>>>(
      `${environment.apiUrl}/customer/tickets`,
      { params }
    );
  }

  // Get single ticket detail
  getTicket(id: string): Observable<ApiResponse<Ticket>> {
    return this.http.get<ApiResponse<Ticket>>(
      `${environment.apiUrl}/customer/tickets/${id}`
    );
  }

  // Submit new ticket
  submitTicket(request: CreateTicketRequest): Observable<ApiResponse<Ticket>> {
    return this.http.post<ApiResponse<Ticket>>(
      `${environment.apiUrl}/customer/tickets`,
      request
    );
  }

  // Get ticket comments
  getTicketComments(ticketId: string): Observable<ApiResponse<Comment[]>> {
    return this.http.get<ApiResponse<Comment[]>>(
      `${environment.apiUrl}/customer/tickets/${ticketId}/comments`
    );
  }

  // Add comment to ticket
  addComment(ticketId: string, request: CreateCommentRequest): Observable<ApiResponse<Comment>> {
    return this.http.post<ApiResponse<Comment>>(
      `${environment.apiUrl}/customer/tickets/${ticketId}/comments`,
      request
    );
  }

  // Reopen a closed ticket
  reopenTicket(ticketId: string): Observable<ApiResponse<Ticket>> {
    return this.http.post<ApiResponse<Ticket>>(
      `${environment.apiUrl}/customer/tickets/${ticketId}/reopen`,
      {}
    );
  }

  // Upload attachment to ticket
  uploadAttachment(ticketId: string, file: File): Observable<ApiResponse<Attachment>> {
    const formData = new FormData();
    formData.append('file', file);
    
    return this.http.post<ApiResponse<Attachment>>(
      `${environment.apiUrl}/customer/tickets/${ticketId}/attachments`,
      formData
    );
  }

  // Get ticket attachments
  getTicketAttachments(ticketId: string): Observable<ApiResponse<Attachment[]>> {
    return this.http.get<ApiResponse<Attachment[]>>(
      `${environment.apiUrl}/customer/tickets/${ticketId}/attachments`
    );
  }

  // Track ticket by email and number (public endpoint)
  trackTicket(request: TrackTicketRequest): Observable<ApiResponse<TrackTicketResponse>> {
    return this.http.post<ApiResponse<TrackTicketResponse>>(
      `${environment.apiUrl}/public/tickets/track`,
      request
    );
  }
}
