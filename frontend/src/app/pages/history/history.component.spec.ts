import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';

import {
  provideHttpClient
} from '@angular/common/http';

import {
  provideHttpClientTesting
} from '@angular/common/http/testing';

import {
  provideRouter
} from '@angular/router';

import {
  HistoryComponent
} from './history.component';


describe('HistoryComponent', () => {

  let component: HistoryComponent;
  let fixture: ComponentFixture<HistoryComponent>;


  beforeEach(async () => {

    await TestBed.configureTestingModule({
      imports: [
        HistoryComponent
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();


    fixture =
      TestBed.createComponent(
        HistoryComponent
      );

    component =
      fixture.componentInstance;

    fixture.detectChanges();
  });


  it('should create', () => {

    expect(component)
      .toBeTruthy();
  });


  it(
    'should filter active and expired files',
    () => {

      const baseFile = {
        id: 1,
        originalName: 'test.txt',
        size: 100,
        contentType: 'text/plain',
        downloadToken: 'token',
        createdAt:
          '2026-09-01T12:00:00',
        passwordProtected: false
      };


      component.files = [
        {
          ...baseFile,
          expiresAt:
            '2000-01-01T00:00:00'
        },
        {
          ...baseFile,
          id: 2,
          expiresAt:
            '2999-01-01T00:00:00'
        }
      ];


      component.statusFilter =
        'all';

      expect(
        component.filteredFiles.length
      ).toBe(2);


      component.statusFilter =
        'expired';

      expect(
        component.filteredFiles
          .map(file => file.id)
      ).toEqual([1]);


      component.statusFilter =
        'active';

      expect(
        component.filteredFiles
          .map(file => file.id)
      ).toEqual([2]);
    }
  );


  it(
    'should shorten a very long file name in the mobile label',
    () => {

      const longFileName =
        'rapport-projet-devops-version-finale-corrigee-avec-un-nom-vraiment-tres-long-jhvgvvyufvu.pdf';


      component.files = [
        {
          id: 3,
          originalName:
            longFileName,
          size: 100,
          contentType:
            'application/pdf',
          downloadToken:
            'long-token',
          createdAt:
            '2026-10-06T00:00:00',
          expiresAt:
            '2999-01-01T00:00:00',
          passwordProtected:
            false
        }
      ];


      component.totalElements = 1;

      component.totalPages = 1;

      component.isLoading = false;

      component.errorMessage = '';

      component.statusFilter =
        'active';


      fixture.detectChanges();


      const mobileName:
        HTMLElement | null =
        fixture.nativeElement
          .querySelector(
            '.file-name-mobile'
          );


      expect(
        mobileName
      ).not.toBeNull();


      const renderedName =
        mobileName
          ?.textContent
          ?.replace(
            /\s+/g,
            ''
          )
          .trim();


      expect(
        renderedName
      ).toBe(
        'rapport-projet-devops.....jhvgvvyufvu.pdf'
      );
    }
  );

});