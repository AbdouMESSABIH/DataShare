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
    const password = __ENV.DOWNLOAD_PASSWORD;
    const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

    if (!token) {
        throw new Error(
            'La variable DOWNLOAD_TOKEN est obligatoire.'
        );
    }

    const headers = {};

    if (password) {
        headers['X-Download-Password'] = password;
    }

    const response = http.get(
        `${baseUrl}/api/download/${token}/file`,
        {
            headers,
        }
    );

    check(response, {
        'status HTTP 200': (res) =>
            res.status === 200,

        'fichier non vide': (res) =>
            res.body !== null &&
            res.body.length > 0,
    });
}
