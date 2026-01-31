export type Status = 'Done' | 'Pending' | 'Error' | 'Validating';

export interface IntegrationRow {
  name: string;
  status: Status;
  startDate: string;
  endDate: string;
}

export interface MatchingRow {
  name: string;
  status: Status;
  startDate: string;
  endDate: string;
}

export interface FileExchangeRow {
  name: string;
  action: 'Send' | 'Get';
  startDate: string;
  endDate: string;
}

export interface AccountingRow {
  name: string;
  status: Status;
  startDate: string;
  endDate: string;
}
