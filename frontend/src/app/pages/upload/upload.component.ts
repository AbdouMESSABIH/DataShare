import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  FileService,
  UploadResponse
} from '../../services/file.service';

@Component({
  selector: 'app-upload',
  imports: [CommonModule, FormsModule],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.scss'
})
export class UploadComponent {

  selectedFile: File | null = null;
  expirationDays = 7;

  message = '';
  errorMessage = '';

  uploadResult: UploadResponse | null = null;

  constructor(private fileService: FileService) {
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }

  onSubmit(): void {

    this.message = '';
    this.errorMessage = '';
    this.uploadResult = null;

    if (!this.selectedFile) {
      this.errorMessage = 'Veuillez sélectionner un fichier';
      return;
    }

    this.fileService
      .upload(this.selectedFile, this.expirationDays)
      .subscribe({

        next: (response) => {
          this.uploadResult = response;
          this.message = 'Fichier téléversé avec succès';
        },

        error: (error) => {

          if (error.status === 415) {
            this.errorMessage = 'Ce type de fichier est interdit';
          } else if (error.status === 413) {
            this.errorMessage = 'Le fichier dépasse la taille maximale autorisée';
          } else if (error.status === 400) {
            this.errorMessage = 'Les informations envoyées sont invalides';
          } else if (error.status === 401 || error.status === 403) {
            this.errorMessage = 'Vous devez être connecté';
          } else {
            this.errorMessage = 'Une erreur est survenue';
          }
        }
      });
  }
}