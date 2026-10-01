import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { HomeComponent } from './home.component';

describe('HomeComponent', () => {

  it('should show the visitor homepage', async () => {

    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [provideRouter([])]
    }).compileComponents();

    const fixture = TestBed.createComponent(HomeComponent);

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent)
      .toContain('Partagez vos fichiers simplement');

    const uploadLink: HTMLAnchorElement =
      fixture.nativeElement.querySelector('.cloud-cta');

    expect(uploadLink.getAttribute('href'))
      .toBe('/upload');
  });

});
