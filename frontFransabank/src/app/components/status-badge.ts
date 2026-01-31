import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';
import { Status } from '../types';

@Component({
  selector: 'app-status-badge',
  imports: [NgClass],
  templateUrl: './status-badge.html',
  styleUrl: './status-badge.css'
})
export class StatusBadge {
  @Input({ required: true }) status!: Status;

  protected get statusClass(): string {
    switch (this.status) {
      case 'Done':
        return 'status-done';
      case 'Pending':
        return 'status-pending';
      case 'Error':
        return 'status-error';
      case 'Validating':
        return 'status-validating';
      default:
        return '';
    }
  }
}
