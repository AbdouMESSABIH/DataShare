import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';


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
}


export interface FileHistoryResponse {
  id: number;
  originalName: string;
  size: number;
  contentType: string;
  downloadToken: string;
  createdAt: string;
  expiresAt: string;
}


@Injectable({
  providedIn: 'root'
})
export class FileService {

  private readonly apiUrl = 'http://localhost:8080/api/files';

  private readonly downloadApiUrl =
    'http://localhost:8080/api/download';


  constructor(private http: HttpClient) {
  }


  upload(
    file: File,
    expirationDays: number
  ): Observable<UploadResponse> {

    const formData = new FormData();

    formData.append('file', file);
    formData.append('expirationDays', expirationDays.toString());

    const token = localStorage.getItem('token');

    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    return this.http.post<UploadResponse>(
      `${this.apiUrl}/upload`,
      formData,
      { headers }
    );
  }


  getDownloadInfo(token: string):
    Observable<DownloadInfoResponse> {

    return this.http.get<DownloadInfoResponse>(
      `${this.downloadApiUrl}/${token}`
    );
  }


  getDownloadUrl(token: string): string {

    return `${this.downloadApiUrl}/${token}/file`;
  }


  getHistory(): Observable<FileHistoryResponse[]> {

    const token = localStorage.getItem('token');

    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    return this.http.get<FileHistoryResponse[]>(
      this.apiUrl,
      { headers }
    );
  }

  deleteFile(id: number): Observable<void> {

    const token = localStorage.getItem('token');

    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    return this.http.delete<void>(
      `${this.apiUrl}/${id}`,
      { headers }
    );
}
}