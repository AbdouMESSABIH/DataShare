import { of, throwError } from 'rxjs';

import { UploadComponent } from './upload.component';
import { FileService } from '../../services/file.service';

describe('UploadComponent', () => {

  let component: UploadComponent;
  let fileService: jasmine.SpyObj<FileService>;

  beforeEach(() => {
    fileService = jasmine.createSpyObj<FileService>(
      'FileService',
      ['upload']
    );

    component = new UploadComponent(fileService);
  });

  function selectFile(): void {
    component.selectedFile = new File(
      ['contenu'],
      'document.txt',
      { type: 'text/plain' }
    );
  }

  it('should reject submission without a file', () => {
    component.onSubmit();

    expect(component.errorMessage)
      .toBe('Veuillez sélectionner un fichier');

    expect(fileService.upload).not.toHaveBeenCalled();
  });

  it('should reject a password shorter than six characters', () => {
    selectFile();
    component.password = '12345';

    component.onSubmit();

    expect(component.errorMessage)
      .toBe('Le mot de passe doit contenir au moins 6 caractères');

    expect(fileService.upload).not.toHaveBeenCalled();
  });

  it('should allow upload without a password', () => {
    selectFile();
    component.password = '';

    fileService.upload.and.returnValue(
      of({
        id: 1,
        originalName: 'document.txt',
        size: 7,
        downloadToken: 'public-token',
        expiresAt: '2026-10-05T12:00:00'
      })
    );

    component.onSubmit();

    expect(fileService.upload).toHaveBeenCalledWith(
      component.selectedFile as File,
      7,
      ''
    );

    expect(component.uploadResult?.downloadToken)
      .toBe('public-token');
  });

  it('should allow upload with a valid password', () => {
    selectFile();
    component.password = 'Secret123!';

    fileService.upload.and.returnValue(
      of({
        id: 2,
        originalName: 'document.txt',
        size: 7,
        downloadToken: 'protected-token',
        expiresAt: '2026-10-05T12:00:00'
      })
    );

    component.onSubmit();

    expect(fileService.upload).toHaveBeenCalledWith(
      component.selectedFile as File,
      7,
      'Secret123!'
    );

    expect(component.message)
      .toBe('Fichier téléversé avec succès');
  });

  it('should display an error for HTTP 429', () => {
    selectFile();

    fileService.upload.and.returnValue(
      throwError(() => ({ status: 429 }))
    );

    component.onSubmit();

    expect(component.errorMessage)
      .toBe('Trop de téléversements. Réessayez dans une minute');
  });

  it('should reset the upload form', () => {
    selectFile();

    component.password = 'Secret123!';
    component.expirationDays = 2;
    component.errorMessage = 'Erreur';

    component.resetUpload();

    expect(component.selectedFile).toBeNull();
    expect(component.password).toBe('');
    expect(component.expirationDays).toBe(7);
    expect(component.errorMessage).toBe('');
  });

});
