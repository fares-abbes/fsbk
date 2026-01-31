import { Component, OnDestroy } from '@angular/core';
import { StatusBadge } from '../components/status-badge';
import { AccountingRow, FileExchangeRow, MatchingRow, Status } from '../types';

type TabKey = 'matching' | 'accounting' | 'deletion';

@Component({
  selector: 'app-matching-page',
  imports: [StatusBadge],
  templateUrl: './matching.html',
  styleUrl: './matching.css'
})
export class MatchingPage implements OnDestroy {
  protected activeTab: TabKey = 'matching';

  protected matchingRows: MatchingRow[] = [
    {
      name: 'Matching CRA',
      status: 'Pending',
      startDate: '-',
      endDate: '-'
    },
    {
      name: 'Matching CRO',
      status: 'Done',
      startDate: this.formatDate(new Date('2026-01-14T10:00:00')),
      endDate: this.formatDate(new Date('2026-01-14T10:30:00'))
    },
    {
      name: 'Matching CRA Rejet',
      status: 'Pending',
      startDate: '-',
      endDate: '-'
    }
  ];

  protected fileExchangeRows: FileExchangeRow[] = [
    {
      name: 'Send LOT',
      action: 'Send',
      startDate: this.formatDate(new Date('2026-01-14T08:00:00')),
      endDate: this.formatDate(new Date('2026-01-14T08:05:00'))
    },
    {
      name: 'Send ORD',
      action: 'Send',
      startDate: '-',
      endDate: '-'
    },
    {
      name: 'Get CRA',
      action: 'Get',
      startDate: '-',
      endDate: '-'
    },
    {
      name: 'Get CRO',
      action: 'Get',
      startDate: this.formatDate(new Date('2026-01-14T10:00:00')),
      endDate: this.formatDate(new Date('2026-01-14T10:02:00'))
    },
    {
      name: 'Get CRA Rejet',
      action: 'Get',
      startDate: '-',
      endDate: '-'
    }
  ];

  protected dayOpRows: AccountingRow[] = [
    {
      name: 'Day OP National',
      status: 'Pending',
      startDate: '-',
      endDate: '-'
    },
    {
      name: 'Day OP Mobile',
      status: 'Pending',
      startDate: '-',
      endDate: '-'
    },
    {
      name: 'Day OP International',
      status: 'Pending',
      startDate: '-',
      endDate: '-'
    }
  ];

  protected mouvementRows: AccountingRow[] = [
    {
      name: 'Mouvement National',
      status: 'Pending',
      startDate: '-',
      endDate: '-'
    },
    {
      name: 'Mouvement Mobile',
      status: 'Pending',
      startDate: '-',
      endDate: '-'
    },
    {
      name: 'Mouvement International',
      status: 'Pending',
      startDate: '-',
      endDate: '-'
    },
    {
      name: 'Final Archivage',
      status: 'Pending',
      startDate: '-',
      endDate: '-'
    }
  ];

  private readonly timers = new Set<number>();

  ngOnDestroy(): void {
    for (const timerId of this.timers) {
      clearTimeout(timerId);
    }
    this.timers.clear();
  }

  protected setTab(tab: TabKey): void {
    this.activeTab = tab;
  }

  protected onRunMatching(row: MatchingRow): void {
    if (row.status === 'Validating') {
      return;
    }

    row.status = 'Validating';
    row.startDate = this.formatDate(new Date());
    row.endDate = '-';

    const timerId = window.setTimeout(() => {
      row.status = 'Done';
      row.endDate = this.formatDate(new Date());
      this.timers.delete(timerId);
    }, 2000);

    this.timers.add(timerId);
  }

  protected isMatchingDisabled(row: MatchingRow): boolean {
    return row.status === 'Validating';
  }

  protected onRunExchange(row: FileExchangeRow): void {
    row.startDate = this.formatDate(new Date());
    row.endDate = '-';

    const timerId = window.setTimeout(() => {
      row.endDate = this.formatDate(new Date());
      this.timers.delete(timerId);
    }, 1500);

    this.timers.add(timerId);
  }

  protected onRunDayOp(): void {
    this.runAccountingBatch(this.dayOpRows);
  }

  protected onRunMouvement(): void {
    this.runAccountingBatch(this.mouvementRows);
  }

  protected onClearDateChange(value: string): void {
    if (!value) {
      return;
    }
    alert(`Selected date: ${value}`);
  }

  private runAccountingBatch(rows: AccountingRow[]): void {
    const start = this.formatDate(new Date());

    for (const row of rows) {
      row.status = 'Validating';
      row.startDate = start;
      row.endDate = '-';
    }

    const timerId = window.setTimeout(() => {
      const end = this.formatDate(new Date());
      for (const row of rows) {
        row.status = 'Done';
        row.endDate = end;
      }
      this.timers.delete(timerId);
    }, 2000);

    this.timers.add(timerId);
  }

  private formatDate(date: Date): string {
    const formatter = new Intl.DateTimeFormat('fr-FR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });

    return formatter.format(date);
  }
}
