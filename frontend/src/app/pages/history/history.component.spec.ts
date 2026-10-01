import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { HistoryComponent } from './history.component';

describe('HistoryComponent', () => {
  let component: HistoryComponent;
  let fixture: ComponentFixture<HistoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HistoryComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HistoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should filter active and expired files', () => {
    const baseFile = {
      id: 1,
      originalName: 'test.txt',
      size: 100,
      contentType: 'text/plain',
      downloadToken: 'token',
      createdAt: '2026-09-01T12:00:00',
      passwordProtected: false
    };

    component.files = [
      {
        ...baseFile,
        expiresAt: '2000-01-01T00:00:00'
      },
      {
        ...baseFile,
        id: 2,
        expiresAt: '2999-01-01T00:00:00'
      }
    ];

    component.statusFilter = 'all';
    expect(component.filteredFiles.length).toBe(2);

    component.statusFilter = 'expired';
    expect(component.filteredFiles.map(file => file.id))
      .toEqual([1]);

    component.statusFilter = 'active';
    expect(component.filteredFiles.map(file => file.id))
      .toEqual([2]);
  });
});