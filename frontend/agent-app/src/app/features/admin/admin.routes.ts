import { Routes } from '@angular/router';

export const adminRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./admin-layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    children: [
      {
        path: '',
        redirectTo: 'users',
        pathMatch: 'full'
      },
      // User Management
      {
        path: 'users',
        loadComponent: () => import('./users/user-list/user-list.component').then(m => m.UserListComponent)
      },
      {
        path: 'users/new',
        loadComponent: () => import('./users/user-form/user-form.component').then(m => m.UserFormComponent)
      },
      {
        path: 'users/:id',
        loadComponent: () => import('./users/user-detail/user-detail.component').then(m => m.UserDetailComponent)
      },
      {
        path: 'users/:id/edit',
        loadComponent: () => import('./users/user-form/user-form.component').then(m => m.UserFormComponent)
      },
      // Team Management
      {
        path: 'teams',
        loadComponent: () => import('./teams/team-list/team-list.component').then(m => m.TeamListComponent)
      },
      {
        path: 'teams/new',
        loadComponent: () => import('./teams/team-form/team-form.component').then(m => m.TeamFormComponent)
      },
      {
        path: 'teams/:id',
        loadComponent: () => import('./teams/team-form/team-form.component').then(m => m.TeamFormComponent)
      },
      {
        path: 'teams/:id/edit',
        loadComponent: () => import('./teams/team-form/team-form.component').then(m => m.TeamFormComponent)
      },
      {
        path: 'teams/:id/members',
        loadComponent: () => import('./teams/team-members/team-members.component').then(m => m.TeamMembersComponent)
      },
      // Project Management
      {
        path: 'projects',
        loadComponent: () => import('./projects/project-list/project-list.component').then(m => m.ProjectListComponent)
      },
      {
        path: 'projects/new',
        loadComponent: () => import('./projects/project-form/project-form.component').then(m => m.ProjectFormComponent)
      },
      {
        path: 'projects/:id',
        loadComponent: () => import('./projects/project-form/project-form.component').then(m => m.ProjectFormComponent)
      },
      {
        path: 'projects/:id/edit',
        loadComponent: () => import('./projects/project-form/project-form.component').then(m => m.ProjectFormComponent)
      },
      {
        path: 'projects/:id/settings',
        loadComponent: () => import('./projects/project-settings/project-settings.component').then(m => m.ProjectSettingsComponent)
      },
      // Settings
      {
        path: 'settings',
        redirectTo: 'settings/general',
        pathMatch: 'full'
      },
      {
        path: 'settings/general',
        loadComponent: () => import('./settings/general-settings/general-settings.component').then(m => m.GeneralSettingsComponent)
      },
      {
        path: 'settings/email',
        loadComponent: () => import('./settings/email-settings/email-settings.component').then(m => m.EmailSettingsComponent)
      },
      {
        path: 'settings/security',
        loadComponent: () => import('./settings/security-settings/security-settings.component').then(m => m.SecuritySettingsComponent)
      }
    ]
  }
];
