import { Component } from '@angular/core';
import { StatusBadge } from '../components/status-badge';
import { IntegrationRow } from '../types';

@Component({
  selector: 'app-tp-integration-page',
  imports: [StatusBadge],
  templateUrl: './tp-integration.html',
  styleUrl: './tp-integration.css'
})
export class TpIntegrationPage {
  protected readonly nationalIntegrations: IntegrationRow[] = [
    {
      name: 'Integration Fichier TP',
      status: 'Done',
      startDate: '-',
      endDate: '-'
    },
    {
      name: 'Integration Fichier TPM',
      status: 'Error',
      startDate: '-',
      endDate: '-'
    }
  ];

  protected readonly internationalIntegrations: IntegrationRow[] = [
    {
      name: 'Integration VISA',
      status: 'Validating',
      startDate: '-',
      endDate: '-'
    }
  ];

  protected onStart(row: IntegrationRow): void {
    alert(`Start: ${row.name}`);
  }

  protected onBypass(row: IntegrationRow): void {
    alert(`Bypass: ${row.name}`);
  }

  protected onView(row: IntegrationRow): void {
    alert(`View: ${row.name}`);
  }
}
