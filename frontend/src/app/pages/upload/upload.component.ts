import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  FileService,
  UploadResponse
} from '../../services/file.service';

import {
  formatFileSize
} from '../../utils/file-size.util';


@Component({
  selector: 'app-upload',

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl: './upload.component.html',
  styleUrl: './upload.component.scss'
})
export class UploadComponent {

  selectedFile: File | null = null;

  expirationDays = 7;

  message = '';
  errorMessage = '';
  copyMessage = '';

  uploadResult:
    UploadResponse | null = null;


  readonly formatFileSize =
    formatFileSize;


  constructor(
    private fileService: FileService
  ) {
  }


  onFileSelected(
    event: Event
  ): void {

    const input =
      event.target as HTMLInputElement;


    if (
      input.files
      && input.files.length > 0
    ) {

      this.selectedFile =
        input.files[0];
    }
  }


  onSubmit(): void {

    this.message = '';
    this.errorMessage = '';
    this.copyMessage = '';

    this.uploadResult = null;


    if (!this.selectedFile) {

      this.errorMessage =
        'Veuillez sélectionner un fichier';

      return;
    }


    this.fileService
      .upload(
        this.selectedFile,
        this.expirationDays
      )
      .subscribe({

        next: (response) => {

          this.uploadResult =
            response;

          this.message =
            'Fichier téléversé avec succès';
        },


        error: (error) => {

          if (error.status === 415) {

            this.errorMessage =
              'Ce type de fichier est interdit';

          } else if (
            error.status === 413
          ) {

            this.errorMessage =
              'Le fichier dépasse la taille maximale autorisée';

          } else if (
            error.status === 400
          ) {

            this.errorMessage =
              'Les informations envoyées sont invalides';

          } else if (
            error.status === 401
            || error.status === 403
          ) {

            this.errorMessage =
              'Vous devez être connecté';

          } else if (
            error.status === 429
          ) {

            this.errorMessage =
              'Trop de téléversements. Réessayez dans une minute';

          } else {

            this.errorMessage =
              'Une erreur est survenue';
          }
        }
      });
  }


  getShareUrl(
    token: string
  ): string {

    return `${window.location.origin}/download/${token}`;
  }


  async copyShareLink(
    token: string
  ): Promise<void> {

    const url =
      this.getShareUrl(token);


    try {

      await navigator.clipboard
        .writeText(url);

      this.copyMessage =
        'Lien copié dans le presse-papiers';

    } catch {

      this.copyMessage =
        'Impossible de copier automatiquement le lien';
    }
  }
}