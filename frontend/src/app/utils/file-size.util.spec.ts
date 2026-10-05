import { formatFileSize } from './file-size.util';

describe('formatFileSize', () => {

  it('formats bytes', () => {
    expect(formatFileSize(500))
      .toBe('500 octets');
  });

  it('formats kilobytes', () => {
    expect(formatFileSize(2048))
      .toBe('2.0 Ko');
  });

  it('formats megabytes', () => {
    expect(formatFileSize(2 * 1024 * 1024))
      .toBe('2.0 Mo');
  });

  it('formats gigabytes', () => {
    expect(formatFileSize(2 * 1024 * 1024 * 1024))
      .toBe('2.00 Go');
  });
});
