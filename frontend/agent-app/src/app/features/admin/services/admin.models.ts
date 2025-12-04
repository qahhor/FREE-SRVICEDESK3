import { UserRole } from '../../../core/models/user.model';

// User Management Models
export interface AdminUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phone?: string;
  avatar?: string;
  role: UserRole;
  active: boolean;
  language: string;
  timezone: string;
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
  teams?: AdminTeam[];
}

export interface CreateUserRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
  role: UserRole;
  active: boolean;
  language?: string;
  timezone?: string;
  teamIds?: string[];
}

export interface UpdateUserRequest {
  email?: string;
  password?: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  role?: UserRole;
  active?: boolean;
  language?: string;
  timezone?: string;
  teamIds?: string[];
}

// Team Management Models
export interface AdminTeam {
  id: string;
  name: string;
  description?: string;
  membersCount: number;
  manager?: AdminUser;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  members?: AdminUser[];
}

export interface CreateTeamRequest {
  name: string;
  description?: string;
  managerId?: string;
  active: boolean;
}

export interface UpdateTeamRequest {
  name?: string;
  description?: string;
  managerId?: string;
  active?: boolean;
}

export interface AddTeamMemberRequest {
  userId: string;
}

// Project Management Models
export interface AdminProject {
  id: string;
  key: string;
  name: string;
  description?: string;
  defaultTeam?: AdminTeam;
  active: boolean;
  color?: string;
  icon?: string;
  ticketsCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProjectRequest {
  key: string;
  name: string;
  description?: string;
  defaultTeamId?: string;
  active: boolean;
  color?: string;
  icon?: string;
}

export interface UpdateProjectRequest {
  name?: string;
  description?: string;
  defaultTeamId?: string;
  active?: boolean;
  color?: string;
  icon?: string;
}

// Settings Models
export interface SystemSettings {
  companyName: string;
  companyLogo?: string;
  defaultLanguage: string;
  timezone: string;
  dateFormat: string;
  timeFormat: string;
  paginationDefault: number;
}

export interface EmailSettings {
  smtpHost: string;
  smtpPort: number;
  smtpUsername: string;
  smtpPassword?: string;
  smtpSecure: boolean;
  fromEmail: string;
  fromName: string;
}

export interface SecuritySettings {
  passwordMinLength: number;
  passwordRequireUppercase: boolean;
  passwordRequireNumber: boolean;
  passwordRequireSpecial: boolean;
  sessionTimeoutMinutes: number;
  twoFactorEnabled: boolean;
  ipWhitelist?: string[];
  apiRateLimitPerMinute: number;
}

// Filter/Search Models
export interface UserFilter {
  search?: string;
  role?: UserRole;
  teamId?: string;
  active?: boolean;
}

export interface TeamFilter {
  search?: string;
  active?: boolean;
}

export interface ProjectFilter {
  search?: string;
  active?: boolean;
}
