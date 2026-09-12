import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import {
  DownloadInfoResponse,
  FileService
} from '../../services/file.service';


@Component({
  selector: 'app-download',
  imports: [CommonModule],
  templateUrl: './download.component.html',
  styleUrl: './download.component.scss'
})
export class DownloadComponent implements OnInit {

  fileInfo: DownloadInfoResponse | null = null;

  downloadUrl = '';

  errorMessage = '';


  constructor(
    private route: ActivatedRoute,
    private fileService: FileService
  ) {
  }


  ngOnInit(): void {

    const token =
      this.route.snapshot.paramMap.get('token');


    if (!token) {

      this.errorMessage =
        'Lien de téléchargement invalide';

      return;
    }


    this.downloadUrl =
      this.fileService.getDownloadUrl(token);


    this.fileService
      .getDownloadInfo(token)
      .subscribe({

        next: (response) => {

          this.fileInfo = response;
        },

        error: (error) => {

          if (error.status === 404) {

            this.errorMessage =
              'Lien de téléchargement invalide';

          } else if (error.status === 410) {

            this.errorMessage =
              'Ce lien de téléchargement a expiré';

          } else {

            this.errorMessage =
              'Une erreur est survenue';
          }
        }
      });
  }
}