import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Ticket,
  CreateTicketRequest,
  ApiResponse,
  Page,
  TicketStatus
} from '../models/ticket.model';

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  private apiUrl = `${environment.apiUrl}/tickets`;

  constructor(private http: HttpClient) {}

  getTickets(page: number = 0, size: number = 20): Observable<ApiResponse<Page<Ticket>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<Page<Ticket>>>(this.apiUrl, { params });
  }

  getTicket(id: string): Observable<ApiResponse<Ticket>> {
    return this.http.get<ApiResponse<Ticket>>(`${this.apiUrl}/${id}`);
  }

  getTicketByNumber(ticketNumber: string): Observable<ApiResponse<Ticket>> {
    return this.http.get<ApiResponse<Ticket>>(`${this.apiUrl}/number/${ticketNumber}`);
  }

  searchTickets(
    status?: TicketStatus,
    assigneeId?: string,
    projectId?: string,
    page: number = 0,
    size: number = 20
  ): Observable<ApiResponse<Page<Ticket>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) params = params.set('status', status);
    if (assigneeId) params = params.set('assigneeId', assigneeId);
    if (projectId) params = params.set('projectId', projectId);

    return this.http.get<ApiResponse<Page<Ticket>>>(`${this.apiUrl}/search`, { params });
  }

  createTicket(request: CreateTicketRequest): Observable<ApiResponse<Ticket>> {
    return this.http.post<ApiResponse<Ticket>>(this.apiUrl, request);
  }

  updateTicketStatus(id: string, status: TicketStatus): Observable<ApiResponse<Ticket>> {
    return this.http.patch<ApiResponse<Ticket>>(
      `${this.apiUrl}/${id}/status`,
      null,
      { params: new HttpParams().set('status', status) }
    );
  }

  assignTicket(id: string, assigneeId: string): Observable<ApiResponse<Ticket>> {
    return this.http.patch<ApiResponse<Ticket>>(
      `${this.apiUrl}/${id}/assign`,
      null,
      { params: new HttpParams().set('assigneeId', assigneeId) }
    );
  }
}
