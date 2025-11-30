export interface Comment {
  id: string;
  ticketId: string;
  ticketNumber: string;
  author: CommentAuthor;
  content: string;
  isInternal: boolean;
  isAutomated: boolean;
  attachments?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CommentAuthor {
  id: string;
  username: string;
  email: string;
  fullName: string;
  role: string;
}

export interface CommentRequest {
  content: string;
  isInternal?: boolean;
  attachments?: string;
}

export interface CommentResponse {
  id: string;
  ticketId: string;
  ticketNumber: string;
  author: CommentAuthor;
  content: string;
  isInternal: boolean;
  isAutomated: boolean;
  attachments?: string;
  createdAt: string;
  updatedAt: string;
}
