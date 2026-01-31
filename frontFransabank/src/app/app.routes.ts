import { Routes } from '@angular/router';

import { TpIntegrationPage } from './pages/tp-integration';
import { MatchingPage } from './pages/matching';

export const routes: Routes = [
  {
    path: '',
    component: TpIntegrationPage
  },
  {
    path: 'matching',
    component: MatchingPage
  },
  {
    path: '**',
    redirectTo: ''
  }
];
