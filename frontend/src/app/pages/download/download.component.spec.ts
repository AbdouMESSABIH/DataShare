import {
  ActivatedRoute,
  convertToParamMap
} from '@angular/router';

import {
  of,
  throwError
} from 'rxjs';

import {
  DownloadComponent
} from './download.component';

import {
  FileService
} from '../../services/file.service';


describe('DownloadComponent', () => {

  let fileService:
    jasmine.SpyObj<FileService>;


  function createComponent(
    token: string | null
  ): DownloadComponent {

    const route = {
      snapshot: {
        paramMap: convertToParamMap(
          token
            ? { token }
            : {}
        )
      }
    };


    return new DownloadComponent(
      route as unknown as ActivatedRoute,
      fileService
    );
  }


  beforeEach(() => {

    fileService =
      jasmine.createSpyObj<FileService>(
        'FileService',
        [
          'getDownloadInfo',
          'getDownloadUrl'
        ]
      );
  });


  it(
    'should load file information for a valid token',
    () => {

      fileService
        .getDownloadUrl
        .and.returnValue(
          'http://localhost:8080/api/download/valid-token/file'
        );


      fileService
        .getDownloadInfo
        .and.returnValue(
          of({
            originalName: 'document.pdf',
            size: 1000,
            contentType: 'application/pdf',
            expiresAt: '2026-09-23T12:00:00'
          })
        );


      const component =
        createComponent(
          'valid-token'
        );


      component.ngOnInit();


      expect(
        fileService.getDownloadInfo
      ).toHaveBeenCalledWith(
        'valid-token'
      );


      expect(
        component.fileInfo?.originalName
      ).toBe(
        'document.pdf'
      );


      expect(
        component.downloadUrl
      ).toContain(
        'valid-token/file'
      );
    }
  );


  it(
    'should display an error when the token is missing',
    () => {

      const component =
        createComponent(null);


      component.ngOnInit();


      expect(
        component.errorMessage
      ).toBe(
        'Lien de téléchargement invalide'
      );


      expect(
        fileService.getDownloadInfo
      ).not.toHaveBeenCalled();
    }
  );


  it(
    'should translate HTTP 404 into an invalid link message',
    () => {

      fileService
        .getDownloadUrl
        .and.returnValue('');


      fileService
        .getDownloadInfo
        .and.returnValue(
          throwError(
            () => ({
              status: 404
            })
          )
        );


      const component =
        createComponent(
          'invalid-token'
        );


      component.ngOnInit();


      expect(
        component.errorMessage
      ).toBe(
        'Lien de téléchargement invalide'
      );
    }
  );


  it(
    'should translate HTTP 410 into an expired link message',
    () => {

      fileService
        .getDownloadUrl
        .and.returnValue('');


      fileService
        .getDownloadInfo
        .and.returnValue(
          throwError(
            () => ({
              status: 410
            })
          )
        );


      const component =
        createComponent(
          'expired-token'
        );


      component.ngOnInit();


      expect(
        component.errorMessage
      ).toBe(
        'Ce lien de téléchargement a expiré'
      );
    }
  );


  it(
    'should display a generic message for an unexpected error',
    () => {

      fileService
        .getDownloadUrl
        .and.returnValue('');


      fileService
        .getDownloadInfo
        .and.returnValue(
          throwError(
            () => ({
              status: 500
            })
          )
        );


      const component =
        createComponent(
          'token'
        );


      component.ngOnInit();


      expect(
        component.errorMessage
      ).toBe(
        'Une erreur est survenue'
      );
    }
  );
});