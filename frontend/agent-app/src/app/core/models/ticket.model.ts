export interface Ticket {
  id: string;
  ticketNumber: string;
  subject: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  channel: ChannelType;
  projectId: string;
  projectName: string;
  requesterId: string;
  requesterName: string;
  assigneeId?: string;
  assigneeName?: string;
  teamId?: string;
  teamName?: string;
  category?: string;
  tags?: string;
  createdAt: string;
  updatedAt: string;
  firstResponseAt?: string;
  resolvedAt?: string;
  closedAt?: string;
  dueDate?: string;
  isPublic: boolean;
}

export enum TicketStatus {
  NEW = 'NEW',
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  PENDING = 'PENDING',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED',
  REOPENED = 'REOPENED',
  ON_HOLD = 'ON_HOLD'
}

export enum TicketPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  URGENT = 'URGENT',
  CRITICAL = 'CRITICAL'
}

export enum ChannelType {
  EMAIL = 'EMAIL',
  TELEGRAM = 'TELEGRAM',
  WHATSAPP = 'WHATSAPP',
  WEB_WIDGET = 'WEB_WIDGET',
  PHONE = 'PHONE',
  WEB_FORM = 'WEB_FORM',
  API = 'API'
}

export interface CreateTicketRequest {
  subject: string;
  description: string;
  priority: TicketPriority;
  channel: ChannelType;
  projectId: string;
  assigneeId?: string;
  teamId?: string;
  category?: string;
  tags?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  error?: string;
  timestamp: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
