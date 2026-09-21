import {
  TestBed
} from '@angular/core/testing';

import {
  provideHttpClient
} from '@angular/common/http';

import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import {
  DownloadInfoResponse,
  FileHistoryPageResponse,
  FileService
} from './file.service';


describe(
  'FileService',
  () => {

    let service:
      FileService;

    let httpMock:
      HttpTestingController;


    beforeEach(() => {

      TestBed.configureTestingModule({
        providers: [

          FileService,

          provideHttpClient(),

          provideHttpClientTesting()
        ]
      });


      service =
        TestBed.inject(
          FileService
        );


      httpMock =
        TestBed.inject(
          HttpTestingController
        );
    });


    afterEach(() => {

      httpMock.verify();
    });


    it(
      'should upload a file',
      () => {

        const file =
          new File(
            ['contenu'],
            'test.txt',
            {
              type: 'text/plain'
            }
          );


        service
          .upload(
            file,
            3
          )
          .subscribe(
            response => {

              expect(
                response.originalName
              ).toBe(
                'test.txt'
              );
            }
          );


        const request =
          httpMock.expectOne(
            'http://localhost:8080/api/files/upload'
          );


        expect(
          request.request.method
        ).toBe(
          'POST'
        );


        expect(
          request.request.body
          instanceof FormData
        ).toBeTrue();


        request.flush({
          id: 1,
          originalName: 'test.txt',
          size: 7,
          downloadToken: 'download-token',
          expiresAt:
            '2026-09-23T12:00:00'
        });
      }
    );


    it(
      'should retrieve download information',
      () => {

        const response:
          DownloadInfoResponse = {

            originalName:
              'document.pdf',

            size: 1000,

            contentType:
              'application/pdf',

            expiresAt:
              '2026-09-23T12:00:00'
          };


        service
          .getDownloadInfo(
            'download-token'
          )
          .subscribe(
            result => {

              expect(
                result
              ).toEqual(
                response
              );
            }
          );


        const request =
          httpMock.expectOne(
            'http://localhost:8080/api/download/download-token'
          );


        expect(
          request.request.method
        ).toBe(
          'GET'
        );


        request.flush(
          response
        );
      }
    );


    it(
      'should generate the download URL',
      () => {

        expect(
          service.getDownloadUrl(
            'abc123'
          )
        ).toBe(
          'http://localhost:8080/api/download/abc123/file'
        );
      }
    );


    it(
      'should retrieve a paginated file history',
      () => {

        const response:
          FileHistoryPageResponse = {

            content: [
              {
                id: 1,

                originalName:
                  'document.pdf',

                size: 1000,

                contentType:
                  'application/pdf',

                downloadToken:
                  'download-token',

                createdAt:
                  '2026-09-20T12:00:00',

                expiresAt:
                  '2026-09-23T12:00:00'
              }
            ],

            page: 1,

            size: 10,

            totalElements: 11,

            totalPages: 2
          };


        service
          .getHistory(
            1,
            10
          )
          .subscribe(
            result => {

              expect(
                result
              ).toEqual(
                response
              );
            }
          );


        const request =
          httpMock.expectOne(
            req =>
              req.url ===
                'http://localhost:8080/api/files'
              &&
              req.params.get(
                'page'
              ) === '1'
              &&
              req.params.get(
                'size'
              ) === '10'
          );


        expect(
          request.request.method
        ).toBe(
          'GET'
        );


        request.flush(
          response
        );
      }
    );


    it(
      'should delete a file',
      () => {

        service
          .deleteFile(42)
          .subscribe();


        const request =
          httpMock.expectOne(
            'http://localhost:8080/api/files/42'
          );


        expect(
          request.request.method
        ).toBe(
          'DELETE'
        );


        request.flush(
          null
        );
      }
    );
  }
);