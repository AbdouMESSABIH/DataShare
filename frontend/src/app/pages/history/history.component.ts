import { CommonModule } from '@angular/common';

import {
  Component,
  OnInit
} from '@angular/core';

import {
  RouterLink
} from '@angular/router';

import {
  FileHistoryResponse,
  FileService
} from '../../services/file.service';

import {
  formatFileSize
} from '../../utils/file-size.util';


@Component({
  selector: 'app-history',

  imports: [
    CommonModule,
    RouterLink
  ],

  templateUrl:
    './history.component.html',

  styleUrl:
    './history.component.scss'
})
export class HistoryComponent
  implements OnInit {

  files:
    FileHistoryResponse[] = [];


  page = 0;

  size = 10;

  totalPages = 0;

  totalElements = 0;


  isLoading = false;

  errorMessage = '';

  copyMessage = '';


  readonly formatFileSize =
    formatFileSize;


  constructor(
    private fileService:
      FileService
  ) {
  }


  ngOnInit(): void {

    this.loadHistory(0);
  }


  loadHistory(
    page: number
  ): void {

    if (page < 0) {

      return;
    }


    this.isLoading = true;

    this.errorMessage = '';


    this.fileService
      .getHistory(
        page,
        this.size
      )
      .subscribe({

        next: (response) => {

          this.files =
            response.content;

          this.page =
            response.page;

          this.size =
            response.size;

          this.totalElements =
            response.totalElements;

          this.totalPages =
            response.totalPages;

          this.isLoading = false;
        },


        error: (error) => {

          this.isLoading = false;


          if (
            error.status === 401
            || error.status === 403
          ) {

            this.errorMessage =
              'Vous devez être connecté';

          } else {

            this.errorMessage =
              'Impossible de charger l’historique';
          }
        }
      });
  }


  previousPage(): void {

    if (
      this.page > 0
    ) {

      this.loadHistory(
        this.page - 1
      );
    }
  }


  nextPage(): void {

    if (
      this.page + 1
      < this.totalPages
    ) {

      this.loadHistory(
        this.page + 1
      );
    }
  }


  hasPreviousPage(): boolean {

    return this.page > 0;
  }


  hasNextPage(): boolean {

    return this.page + 1
      < this.totalPages;
  }


  getShareUrl(
    token: string
  ): string {

    return `${window.location.origin}/download/${token}`;
  }


  async copyShareLink(
    token: string
  ): Promise<void> {

    try {

      await navigator.clipboard
        .writeText(
          this.getShareUrl(
            token
          )
        );


      this.copyMessage =
        'Lien copié dans le presse-papiers';

    } catch {

      this.copyMessage =
        'Impossible de copier automatiquement le lien';
    }
  }


  deleteFile(
    id: number
  ): void {

    const confirmed =
      window.confirm(
        'Voulez-vous vraiment supprimer ce fichier ?'
      );


    if (!confirmed) {

      return;
    }


    this.fileService
      .deleteFile(id)
      .subscribe({

        next: () => {

          if (
            this.files.length === 1
            && this.page > 0
          ) {

            this.loadHistory(
              this.page - 1
            );

          } else {

            this.loadHistory(
              this.page
            );
          }
        },


        error: () => {

          this.errorMessage =
            'Impossible de supprimer le fichier';
        }
      });
  }
}