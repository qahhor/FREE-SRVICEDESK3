import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse, Page } from '../../../core/models/ticket.model';
import {
  AdminUser,
  AdminTeam,
  AdminProject,
  CreateUserRequest,
  UpdateUserRequest,
  CreateTeamRequest,
  UpdateTeamRequest,
  CreateProjectRequest,
  UpdateProjectRequest,
  AddTeamMemberRequest,
  SystemSettings,
  EmailSettings,
  SecuritySettings,
  UserFilter,
  TeamFilter,
  ProjectFilter
} from './admin.models';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly baseUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  // ============ USER MANAGEMENT ============

  getUsers(
    page: number = 0,
    size: number = 20,
    filter?: UserFilter
  ): Observable<ApiResponse<Page<AdminUser>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filter?.search) params = params.set('search', filter.search);
    if (filter?.role) params = params.set('role', filter.role);
    if (filter?.teamId) params = params.set('teamId', filter.teamId);
    if (filter?.active !== undefined) params = params.set('active', filter.active.toString());

    return this.http.get<ApiResponse<Page<AdminUser>>>(`${this.baseUrl}/users`, { params });
  }

  getUser(id: string): Observable<ApiResponse<AdminUser>> {
    return this.http.get<ApiResponse<AdminUser>>(`${this.baseUrl}/users/${id}`);
  }

  createUser(request: CreateUserRequest): Observable<ApiResponse<AdminUser>> {
    return this.http.post<ApiResponse<AdminUser>>(`${this.baseUrl}/users`, request);
  }

  updateUser(id: string, request: UpdateUserRequest): Observable<ApiResponse<AdminUser>> {
    return this.http.put<ApiResponse<AdminUser>>(`${this.baseUrl}/users/${id}`, request);
  }

  deleteUser(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/users/${id}`);
  }

  updateUserStatus(id: string, active: boolean): Observable<ApiResponse<AdminUser>> {
    return this.http.patch<ApiResponse<AdminUser>>(
      `${this.baseUrl}/users/${id}/status`,
      null,
      { params: new HttpParams().set('active', active.toString()) }
    );
  }

  resetUserPassword(id: string, newPassword: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(
      `${this.baseUrl}/users/${id}/reset-password`,
      { newPassword }
    );
  }

  // ============ TEAM MANAGEMENT ============

  getTeams(
    page: number = 0,
    size: number = 20,
    filter?: TeamFilter
  ): Observable<ApiResponse<Page<AdminTeam>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filter?.search) params = params.set('search', filter.search);
    if (filter?.active !== undefined) params = params.set('active', filter.active.toString());

    return this.http.get<ApiResponse<Page<AdminTeam>>>(`${this.baseUrl}/teams`, { params });
  }

  getTeam(id: string): Observable<ApiResponse<AdminTeam>> {
    return this.http.get<ApiResponse<AdminTeam>>(`${this.baseUrl}/teams/${id}`);
  }

  createTeam(request: CreateTeamRequest): Observable<ApiResponse<AdminTeam>> {
    return this.http.post<ApiResponse<AdminTeam>>(`${this.baseUrl}/teams`, request);
  }

  updateTeam(id: string, request: UpdateTeamRequest): Observable<ApiResponse<AdminTeam>> {
    return this.http.put<ApiResponse<AdminTeam>>(`${this.baseUrl}/teams/${id}`, request);
  }

  deleteTeam(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/teams/${id}`);
  }

  addTeamMember(teamId: string, request: AddTeamMemberRequest): Observable<ApiResponse<AdminTeam>> {
    return this.http.post<ApiResponse<AdminTeam>>(
      `${this.baseUrl}/teams/${teamId}/members`,
      request
    );
  }

  removeTeamMember(teamId: string, userId: string): Observable<ApiResponse<AdminTeam>> {
    return this.http.delete<ApiResponse<AdminTeam>>(
      `${this.baseUrl}/teams/${teamId}/members/${userId}`
    );
  }

  // ============ PROJECT MANAGEMENT ============

  getProjects(
    page: number = 0,
    size: number = 20,
    filter?: ProjectFilter
  ): Observable<ApiResponse<Page<AdminProject>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filter?.search) params = params.set('search', filter.search);
    if (filter?.active !== undefined) params = params.set('active', filter.active.toString());

    return this.http.get<ApiResponse<Page<AdminProject>>>(`${this.baseUrl}/projects`, { params });
  }

  getProject(id: string): Observable<ApiResponse<AdminProject>> {
    return this.http.get<ApiResponse<AdminProject>>(`${this.baseUrl}/projects/${id}`);
  }

  createProject(request: CreateProjectRequest): Observable<ApiResponse<AdminProject>> {
    return this.http.post<ApiResponse<AdminProject>>(`${this.baseUrl}/projects`, request);
  }

  updateProject(id: string, request: UpdateProjectRequest): Observable<ApiResponse<AdminProject>> {
    return this.http.put<ApiResponse<AdminProject>>(`${this.baseUrl}/projects/${id}`, request);
  }

  deleteProject(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/projects/${id}`);
  }

  // ============ SETTINGS ============

  getSystemSettings(): Observable<ApiResponse<SystemSettings>> {
    return this.http.get<ApiResponse<SystemSettings>>(`${this.baseUrl}/settings/general`);
  }

  updateSystemSettings(settings: SystemSettings): Observable<ApiResponse<SystemSettings>> {
    return this.http.put<ApiResponse<SystemSettings>>(`${this.baseUrl}/settings/general`, settings);
  }

  getEmailSettings(): Observable<ApiResponse<EmailSettings>> {
    return this.http.get<ApiResponse<EmailSettings>>(`${this.baseUrl}/settings/email`);
  }

  updateEmailSettings(settings: EmailSettings): Observable<ApiResponse<EmailSettings>> {
    return this.http.put<ApiResponse<EmailSettings>>(`${this.baseUrl}/settings/email`, settings);
  }

  testEmailSettings(settings: EmailSettings): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/settings/test-email`, settings);
  }

  getSecuritySettings(): Observable<ApiResponse<SecuritySettings>> {
    return this.http.get<ApiResponse<SecuritySettings>>(`${this.baseUrl}/settings/security`);
  }

  updateSecuritySettings(settings: SecuritySettings): Observable<ApiResponse<SecuritySettings>> {
    return this.http.put<ApiResponse<SecuritySettings>>(`${this.baseUrl}/settings/security`, settings);
  }
}
