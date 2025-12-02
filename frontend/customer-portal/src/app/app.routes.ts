import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  // Public routes
  {
    path: '',
    loadComponent: () => import('./shared/layouts/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent)
      },
      // Knowledge Base routes (public)
      {
        path: 'kb',
        loadComponent: () => import('./features/knowledge-base/kb-home/kb-home.component').then(m => m.KbHomeComponent)
      },
      {
        path: 'kb/search',
        loadComponent: () => import('./features/knowledge-base/kb-search/kb-search.component').then(m => m.KbSearchComponent)
      },
      {
        path: 'kb/category/:slug',
        loadComponent: () => import('./features/knowledge-base/kb-category/kb-category.component').then(m => m.KbCategoryComponent)
      },
      {
        path: 'kb/article/:slug',
        loadComponent: () => import('./features/knowledge-base/kb-article/kb-article.component').then(m => m.KbArticleComponent)
      },
      // Track ticket (public)
      {
        path: 'track',
        loadComponent: () => import('./features/tickets/track-ticket/track-ticket.component').then(m => m.TrackTicketComponent)
      },
      // Auth routes
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
      },
      {
        path: 'forgot-password',
        loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
      },
      // Authenticated routes
      {
        path: 'tickets',
        canActivate: [authGuard],
        children: [
          {
            path: '',
            loadComponent: () => import('./features/tickets/my-tickets/my-tickets.component').then(m => m.MyTicketsComponent)
          },
          {
            path: 'submit',
            loadComponent: () => import('./features/tickets/submit-ticket/submit-ticket.component').then(m => m.SubmitTicketComponent)
          },
          {
            path: ':id',
            loadComponent: () => import('./features/tickets/ticket-detail/ticket-detail.component').then(m => m.TicketDetailComponent)
          }
        ]
      },
      {
        path: 'profile',
        canActivate: [authGuard],
        children: [
          {
            path: '',
            loadComponent: () => import('./features/profile/profile-view/profile-view.component').then(m => m.ProfileViewComponent)
          },
          {
            path: 'edit',
            loadComponent: () => import('./features/profile/profile-edit/profile-edit.component').then(m => m.ProfileEditComponent)
          }
        ]
      }
    ]
  },
  // Fallback
  {
    path: '**',
    redirectTo: ''
  }
];
