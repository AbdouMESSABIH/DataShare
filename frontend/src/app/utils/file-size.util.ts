export function formatFileSize(bytes: number): string {

  if (bytes < 1024) {
    return `${bytes} octets`;
  }

  const kilobytes = bytes / 1024;

  if (kilobytes < 1024) {
    return `${kilobytes.toFixed(1)} Ko`;
  }

  const megabytes = kilobytes / 1024;

  if (megabytes < 1024) {
    return `${megabytes.toFixed(1)} Mo`;
  }

  const gigabytes = megabytes / 1024;

  return `${gigabytes.toFixed(2)} Go`;
}