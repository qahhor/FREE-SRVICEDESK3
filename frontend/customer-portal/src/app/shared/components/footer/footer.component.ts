import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterLink, MatIconModule],
  template: `
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-section">
          <h3>
            <mat-icon>support_agent</mat-icon>
            Service Desk
          </h3>
          <p>Your trusted support platform for all your service needs.</p>
        </div>
        
        <div class="footer-section">
          <h4>Quick Links</h4>
          <nav>
            <a routerLink="/">Home</a>
            <a routerLink="/kb">Knowledge Base</a>
            <a routerLink="/track">Track Ticket</a>
            <a routerLink="/tickets/submit">Submit Ticket</a>
          </nav>
        </div>
        
        <div class="footer-section">
          <h4>Support</h4>
          <nav>
            <a routerLink="/kb">FAQs</a>
            <a href="mailto:support&#64;servicedesk.io">Contact Us</a>
          </nav>
        </div>
        
        <div class="footer-section">
          <h4>Contact</h4>
          <p>
            <mat-icon>email</mat-icon>
            support&#64;servicedesk.io
          </p>
          <p>
            <mat-icon>phone</mat-icon>
            +1 (555) 123-4567
          </p>
        </div>
      </div>
      
      <div class="footer-bottom">
        <p>&copy; {{ currentYear }} Service Desk Platform. All rights reserved.</p>
      </div>
    </footer>
  `,
  styles: [`
    .footer {
      background: #333;
      color: white;
      margin-top: auto;
    }
    
    .footer-content {
      max-width: 1200px;
      margin: 0 auto;
      padding: 48px 16px;
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 32px;
    }
    
    .footer-section h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0 0 16px 0;
      font-size: 1.25rem;
    }
    
    .footer-section h4 {
      margin: 0 0 16px 0;
      font-size: 1rem;
      font-weight: 500;
    }
    
    .footer-section p {
      margin: 0 0 8px 0;
      color: rgba(255, 255, 255, 0.7);
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .footer-section p mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }
    
    .footer-section nav {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    
    .footer-section nav a {
      color: rgba(255, 255, 255, 0.7);
      text-decoration: none;
      transition: color 0.2s;
    }
    
    .footer-section nav a:hover {
      color: white;
    }
    
    .footer-bottom {
      background: rgba(0, 0, 0, 0.2);
      padding: 16px;
      text-align: center;
    }
    
    .footer-bottom p {
      margin: 0;
      color: rgba(255, 255, 255, 0.5);
      font-size: 0.875rem;
    }
  `]
})
export class FooterComponent {
  currentYear = new Date().getFullYear();
}
