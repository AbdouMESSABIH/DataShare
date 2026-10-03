import {
  ActivatedRoute,
  convertToParamMap
} from '@angular/router';

import {
  HttpResponse
} from '@angular/common/http';

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
          'downloadFile'
        ]
      );
  });


  it(
    'should load file information for a valid token',
    () => {

      fileService
        .getDownloadInfo
        .and.returnValue(
          of({
            originalName: 'document.pdf',
            size: 1000,
            contentType: 'application/pdf',
            expiresAt: '2026-09-23T12:00:00',
            passwordProtected: true
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
        component.token
      ).toBe(
        'valid-token'
      );
    }
  );


  it(
    'should display an error when the token is missing',
    () => {

      const component =
        createComponent(
          null
        );


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
    'should display a generic message for an unexpected info error',
    () => {

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


  it(
    'should require a password before downloading',
    () => {

      const component =
        createComponent(
          'valid-token'
        );


      component.token =
        'valid-token';

      component.password =
        '';

      component.fileInfo = {
        originalName: 'document.pdf',
        size: 100,
        contentType: 'application/pdf',
        expiresAt: '2026-09-30T12:00:00',
        passwordProtected: true
      };


      component.download();


      expect(
        component.downloadErrorMessage
      ).toBe(
        'Veuillez saisir le mot de passe'
      );


      expect(
        fileService.downloadFile
      ).not.toHaveBeenCalled();
    }
  );


  it(
    'should allow a public file without a password',
    () => {

      fileService.downloadFile.and.returnValue(
        of(new HttpResponse<Blob>({
          body: null,
          status: 200
        }))
      );

      const component = createComponent('public-token');

      component.token = 'public-token';
      component.password = '';

      component.fileInfo = {
        originalName: 'public.pdf',
        size: 100,
        contentType: 'application/pdf',
        expiresAt: '2026-09-30T12:00:00',
        passwordProtected: false
      };

      component.download();

      expect(fileService.downloadFile)
        .toHaveBeenCalledWith('public-token', '');

      expect(component.downloading).toBeFalse();
    }
  );


  it(
    'should send the token and password when downloading',
    () => {

      const blob =
        new Blob(
          ['test'],
          {
            type: 'application/pdf'
          }
        );


      fileService
        .downloadFile
        .and.returnValue(
          of(
            new HttpResponse<Blob>({
              body: blob,
              status: 200
            })
          )
        );


      const component =
        createComponent(
          'valid-token'
        );


      component.token =
        'valid-token';

      component.password =
        'Secret123!';

      component.fileInfo = {
        originalName: 'document.pdf',
        size: 4,
        contentType: 'application/pdf',
        expiresAt: '2026-09-30T12:00:00',
        passwordProtected: true
      };


      spyOn(
        URL,
        'createObjectURL'
      ).and.returnValue(
        'blob:test'
      );


      spyOn(
        URL,
        'revokeObjectURL'
      );


      const originalCreateElement =
        document.createElement.bind(
          document
        );


      const anchor =
        originalCreateElement(
          'a'
        );


      spyOn(
        anchor,
        'click'
      );


      spyOn(
        document,
        'createElement'
      ).and.callFake(
        (
          tagName: string,
          options?: ElementCreationOptions
        ): HTMLElement => {

          if (
            tagName.toLowerCase()
            === 'a'
          ) {

            return anchor;
          }


          return originalCreateElement(
            tagName,
            options
          );
        }
      );


      component.download();


      expect(
        fileService.downloadFile
      ).toHaveBeenCalledWith(
        'valid-token',
        'Secret123!'
      );


      expect(
        anchor.download
      ).toBe(
        'document.pdf'
      );


      expect(
        anchor.click
      ).toHaveBeenCalled();


      expect(
        component.downloading
      ).toBeFalse();
    }
  );


  it(
    'should display an incorrect password message for HTTP 403',
    () => {

      fileService
        .downloadFile
        .and.returnValue(
          throwError(
            () => ({
              status: 403
            })
          )
        );


      const component =
        createComponent(
          'valid-token'
        );


      component.token =
        'valid-token';

      component.password =
        'WrongPassword';


      component.download();


      expect(
        fileService.downloadFile
      ).toHaveBeenCalledWith(
        'valid-token',
        'WrongPassword'
      );


      expect(
        component.downloadErrorMessage
      ).toBe(
        'Mot de passe incorrect'
      );


      expect(
        component.downloading
      ).toBeFalse();
    }
  );


  it(
    'should display an expired message for HTTP 410 during download',
    () => {

      fileService
        .downloadFile
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


      component.token =
        'expired-token';

      component.password =
        'Secret123!';


      component.download();


      expect(
        component.downloadErrorMessage
      ).toBe(
        'Ce lien de téléchargement a expiré'
      );
    }
  );


  it(
    'should display an invalid link message for HTTP 404 during download',
    () => {

      fileService
        .downloadFile
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


      component.token =
        'invalid-token';

      component.password =
        'Secret123!';


      component.download();


      expect(
        component.downloadErrorMessage
      ).toBe(
        'Lien de téléchargement invalide'
      );
    }
  );


  describe('expirationNotice', () => {

    const referenceTime = Date.UTC(2026, 9, 2, 12, 0, 0);
    const oneHour = 60 * 60 * 1000;
    const oneDay = 24 * oneHour;

    function createFileWithExpiration(
      offset: number
    ): DownloadComponent {
      const component = createComponent('test-token');

      component.fileInfo = {
        originalName: 'document.pdf',
        size: 100,
        contentType: 'application/pdf',
        expiresAt: new Date(
          referenceTime + offset
        ).toISOString(),
        passwordProtected: false
      };

      return component;
    }

    beforeEach(() => {
      spyOn(Date, 'now').and.returnValue(referenceTime);
    });

    it(
      'should show an informational notice for a link valid for three days',
      () => {
        const component = createFileWithExpiration(3 * oneDay);

        expect(component.expirationNotice).toEqual({
          text: 'Ce lien expirera dans 3 jours.',
          urgent: false
        });
      }
    );

    it('should warn when the link expires within 24 hours', () => {
      const component = createFileWithExpiration(12 * oneHour);

      expect(component.expirationNotice).toEqual({
        text: 'Attention : ce lien expire dans moins de 24 heures.',
        urgent: true
      });
    });

    it('should flag an already expired link', () => {
      const component = createFileWithExpiration(-oneHour);

      expect(component.expirationNotice).toEqual({
        text: 'Ce lien a expiré et ne peut plus être utilisé.',
        urgent: true
      });
    });

    it('should not show a notice without file information', () => {
      const component = createComponent('test-token');

      expect(component.expirationNotice).toBeNull();
    });

  });


});
