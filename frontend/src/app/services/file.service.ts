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

@Injectable({
  providedIn: 'root'
})
export class FileService {

  private readonly apiUrl = 'http://localhost:8080/api/files';

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
}