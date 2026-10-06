import http from 'k6/http';
import { check } from 'k6';

const tokens = (__ENV.DOWNLOAD_TOKENS || '')
    .split(',')
    .map(token => token.trim())
    .filter(Boolean);

const password = __ENV.DOWNLOAD_PASSWORD;
const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
    scenarios: {
        sustained_download: {
            executor: 'constant-arrival-rate',

            // Environ 1 requête toutes les 2 secondes
            rate: 1,
            timeUnit: '2s',

            // Charge soutenue pendant 60 secondes
            duration: '60s',

            preAllocatedVUs: 5,
            maxVUs: 10,
        },
    },

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1000'],
    },
};

export default function () {
    if (tokens.length === 0) {
        throw new Error(
            'DOWNLOAD_TOKENS doit contenir au moins un token.'
        );
    }

    // Répartition des requêtes entre plusieurs liens.
    const token = tokens[__ITER % tokens.length];

    const headers = {};

    /*
     * Campagne protégée :
     * DOWNLOAD_PASSWORD est défini.
     *
     * Campagne non protégée :
     * DOWNLOAD_PASSWORD n'est pas défini.
     */
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
