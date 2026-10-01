import { Injectable } from '@angular/core';

import {
  HttpClient,
  HttpHeaders,
  HttpParams,
  HttpResponse
} from '@angular/common/http';

import { Observable } from 'rxjs';

import {
  environment
} from '../../environments/environment';


export interface UploadResponse {

  id: number;

  originalName: string;

  size: number;

  downloadToken: string;

  expiresAt: string;
}


export interface DownloadInfoResponse {

  originalName: string;

  size: number;

  contentType: string;

  expiresAt: string;

  passwordProtected: boolean;
}


export interface FileHistoryResponse {

  id: number;

  originalName: string;

  size: number;

  contentType: string;

  downloadToken: string;

  createdAt: string;

  expiresAt: string;

  passwordProtected?: boolean;
}


export interface FileHistoryPageResponse {

  content:
    FileHistoryResponse[];

  page: number;

  size: number;

  totalElements: number;

  totalPages: number;
}


@Injectable({
  providedIn: 'root'
})
export class FileService {

  private readonly apiUrl =
    `${environment.apiUrl}/files`;


  private readonly downloadApiUrl =
    `${environment.apiUrl}/download`;


  constructor(
    private http: HttpClient
  ) {
  }


  upload(
    file: File,
    expirationDays: number,
    password = ''
  ): Observable<UploadResponse> {

    const formData =
      new FormData();


    formData.append(
      'file',
      file
    );


    formData.append(
      'expirationDays',
      expirationDays.toString()
    );


    if (password.trim()) {

      formData.append(
        'password',
        password
      );
    }


    return this.http.post<UploadResponse>(
      `${this.apiUrl}/upload`,
      formData
    );
  }


  getDownloadInfo(
    token: string
  ): Observable<DownloadInfoResponse> {

    return this.http
      .get<DownloadInfoResponse>(
        `${this.downloadApiUrl}/${token}`
      );
  }


  getDownloadUrl(
    token: string
  ): string {

    return `${this.downloadApiUrl}/${token}/file`;
  }


  downloadFile(
    token: string,
    password: string
  ): Observable<HttpResponse<Blob>> {

    let headers =
      new HttpHeaders();


    if (password.trim()) {

      headers =
        headers.set(
          'X-Download-Password',
          password
        );
    }


    return this.http.get(
      `${this.downloadApiUrl}/${token}/file`,
      {
        headers,
        observe: 'response',
        responseType: 'blob'
      }
    );
  }


  getHistory(
    page = 0,
    size = 10
  ): Observable<FileHistoryPageResponse> {

    const params =
      new HttpParams()
        .set(
          'page',
          page.toString()
        )
        .set(
          'size',
          size.toString()
        );


    return this.http
      .get<FileHistoryPageResponse>(
        this.apiUrl,
        {
          params
        }
      );
  }


  deleteFile(
    id: number
  ): Observable<void> {

    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );
  }
}
