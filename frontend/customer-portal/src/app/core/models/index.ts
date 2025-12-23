// User models
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phone?: string;
  avatar?: string;
  role: UserRole;
  active: boolean;
  language?: string;
  timezone?: string;
}

export type UserRole = 'ADMIN' | 'MANAGER' | 'AGENT' | 'CUSTOMER';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  user: User;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  password: string;
  confirmPassword: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

// Ticket models
export interface Ticket {
  id: string;
  ticketNumber: string;
  subject: string;
  description?: string;
  status: TicketStatus;
  priority: TicketPriority;
  channel: ChannelType;
  project: Project;
  requester: User;
  assignee?: User;
  category?: string;
  tags?: string[];
  createdAt: string;
  updatedAt: string;
  resolvedAt?: string;
  closedAt?: string;
  dueDate?: string;
}

export type TicketStatus = 'NEW' | 'OPEN' | 'IN_PROGRESS' | 'PENDING' | 'RESOLVED' | 'CLOSED' | 'REOPENED' | 'ON_HOLD';
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT' | 'CRITICAL';
export type ChannelType = 'EMAIL' | 'TELEGRAM' | 'WHATSAPP' | 'WEB_WIDGET' | 'PHONE' | 'WEB_FORM' | 'API';

export interface CreateTicketRequest {
  projectId: string;
  subject: string;
  description?: string;
  priority?: TicketPriority;
  category?: string;
}

export interface TrackTicketRequest {
  email: string;
  ticketNumber: string;
}

export interface TrackTicketResponse {
  ticket: Ticket;
  comments: Comment[];
}

// Comment models
export interface Comment {
  id: string;
  ticketId: string;
  author: User;
  content: string;
  isInternal: boolean;
  isAutomated: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCommentRequest {
  content: string;
}

// Project models
export interface Project {
  id: string;
  key: string;
  name: string;
  description?: string;
  active: boolean;
  color?: string;
  icon?: string;
}

// Knowledge Base models
export interface KBCategory {
  id: string;
  name: string;
  description?: string;
  slug: string;
  icon?: string;
  parentId?: string;
  articleCount: number;
  children?: KBCategory[];
}

export interface KBArticle {
  id: string;
  title: string;
  slug: string;
  content: string;
  excerpt?: string;
  categoryId: string;
  category?: KBCategory;
  author?: User;
  status: ArticleStatus;
  viewCount: number;
  helpfulCount: number;
  notHelpfulCount: number;
  tags?: string[];
  publishedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export type ArticleStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface ArticleFeedback {
  articleId: string;
  helpful: boolean;
  comment?: string;
}

export interface KBSearchResult {
  articles: KBArticle[];
  totalCount: number;
  query: string;
}

// API Response wrapper
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
  timestamp: string;
}

// Pagination
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Attachment
export interface Attachment {
  id: string;
  filename: string;
  originalFilename: string;
  contentType: string;
  size: number;
  downloadUrl: string;
  isImage: boolean;
  createdAt: string;
}
