import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 10,
    duration: '20s',

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1000'],
    },
};

export default function () {
    const token = __ENV.DOWNLOAD_TOKEN;
    const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

    const response = http.get(
        `${baseUrl}/api/download/${token}/file`
    );

    check(response, {
        'status HTTP 200': (res) => res.status === 200,
        'fichier non vide': (res) => res.body.length > 0,
    });
}