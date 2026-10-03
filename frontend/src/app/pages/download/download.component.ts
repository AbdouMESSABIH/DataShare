import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import {
  DownloadInfoResponse,
  FileService
} from '../../services/file.service';

import {
  formatFileSize
} from '../../utils/file-size.util';


@Component({
  selector: 'app-download',

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './download.component.html',

  styleUrl:
    './download.component.scss'
})
export class DownloadComponent
  implements OnInit {

  fileInfo:
    DownloadInfoResponse | null = null;

  token = '';

  password = '';

  errorMessage = '';

  downloadErrorMessage = '';

  downloading = false;


  readonly formatFileSize =
    formatFileSize;


  /**
   * Message calcule depuis la date d'expiration renvoyee par l'API.
   * Aucune date ni duree restante n'est codee en dur.
   */
  get expirationNotice(): {
    text: string;
    urgent: boolean;
  } | null {
    if (!this.fileInfo) {
      return null;
    }

    const expiration = new Date(this.fileInfo.expiresAt).getTime();

    if (!Number.isFinite(expiration)) {
      return null;
    }

    const remaining = expiration - Date.now();
    const oneDay = 24 * 60 * 60 * 1000;

    if (remaining <= 0) {
      return {
        text: 'Ce lien a expiré et ne peut plus être utilisé.',
        urgent: true
      };
    }

    if (remaining <= oneDay) {
      return {
        text: 'Attention : ce lien expire dans moins de 24 heures.',
        urgent: true
      };
    }

    const days = Math.ceil(remaining / oneDay);

    return {
      text: `Ce lien expirera dans ${days} jours.`,
      urgent: false
    };
  }


  constructor(
    private route: ActivatedRoute,
    private fileService: FileService
  ) {
  }


  ngOnInit(): void {

    const token =
      this.route.snapshot
        .paramMap
        .get('token');


    if (!token) {

      this.errorMessage =
        'Lien de téléchargement invalide';

      return;
    }


    this.token = token;


    this.fileService
      .getDownloadInfo(
        token
      )
      .subscribe({

        next: (response) => {

          this.fileInfo =
            response;
        },


        error: (error) => {

          if (
            error.status === 404
          ) {

            this.errorMessage =
              'Lien de téléchargement invalide';

          } else if (
            error.status === 410
          ) {

            this.errorMessage =
              'Ce lien de téléchargement a expiré';

          } else {

            this.errorMessage =
              'Une erreur est survenue';
          }
        }
      });
  }


  download(): void {

    this.downloadErrorMessage = '';


    if (
      this.fileInfo?.passwordProtected
      && !this.password.trim()
    ) {

      this.downloadErrorMessage =
        'Veuillez saisir le mot de passe';

      return;
    }


    this.downloading = true;


    this.fileService
      .downloadFile(
        this.token,
        this.password
      )
      .subscribe({

        next: (response) => {

          this.downloading = false;


          if (!response.body) {

            this.downloadErrorMessage =
              'Impossible de télécharger le fichier';

            return;
          }


          const url =
            window.URL.createObjectURL(
              response.body
            );


          const link =
            document.createElement(
              'a'
            );


          link.href =
            url;


          link.download =
            this.fileInfo
              ?.originalName
            ?? 'fichier';


          document.body
            .appendChild(
              link
            );


          link.click();


          document.body
            .removeChild(
              link
            );


          window.URL
            .revokeObjectURL(
              url
            );
        },


        error: (error) => {

          this.downloading =
            false;


          if (
            error.status === 403
          ) {

            this.downloadErrorMessage =
              'Mot de passe incorrect';

          } else if (
            error.status === 404
          ) {

            this.downloadErrorMessage =
              'Lien de téléchargement invalide';

          } else if (
            error.status === 410
          ) {

            this.downloadErrorMessage =
              'Ce lien de téléchargement a expiré';

          } else if (
            error.status === 429
          ) {

            this.downloadErrorMessage =
              'Trop de tentatives. Réessayez plus tard';

          } else {

            this.downloadErrorMessage =
              'Une erreur est survenue';
          }
        }
      });
  }
}
