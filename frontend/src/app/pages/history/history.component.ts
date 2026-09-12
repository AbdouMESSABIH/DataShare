import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';

import {
  FileHistoryResponse,
  FileService
} from '../../services/file.service';

@Component({
  selector: 'app-history',
  imports: [CommonModule],
  templateUrl: './history.component.html',
  styleUrl: './history.component.scss'
})
export class HistoryComponent implements OnInit {

  files: FileHistoryResponse[] = [];

  errorMessage = '';

  constructor(private fileService: FileService) {
  }

  ngOnInit(): void {

    this.fileService
      .getHistory()
      .subscribe({

        next: (response) => {
          this.files = response;
        },

        error: (error) => {

          if (error.status === 401 || error.status === 403) {
            this.errorMessage = 'Vous devez être connecté';
          } else {
            this.errorMessage = 'Impossible de charger l’historique';
          }
        }
      });
  }

  deleteFile(id: number): void {

    const confirmed = window.confirm(
      'Voulez-vous vraiment supprimer ce fichier ?'
    );

    if (!confirmed) {
      return;
    }

    this.fileService
      .deleteFile(id)
      .subscribe({

        next: () => {
          this.files = this.files.filter(
            file => file.id !== id
          );
        },

        error: () => {
          this.errorMessage =
            'Impossible de supprimer le fichier';
        }
      });
  }
}