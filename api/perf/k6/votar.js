import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  // Ajuste conforme necessidade
  stages: [
    { duration: '15s', target: 20 },
    { duration: '45s', target: 200 },
    { duration: '30s', target: 200 },
    { duration: '15s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<400'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Util: gerar CPF simples (11 dígitos, evitando todos iguais)
function genCpf(seed) {
  let s = String(seed).padStart(11, '0');
  if (/^(\d)\1{10}$/.test(s)) {
    return '12345678901';
  }
  return s;
}

export function setup() {
  // 1) cria pauta
  const createRes = http.post(`${BASE_URL}/api/agenda`, JSON.stringify({
    title: 'Perf Test',
    description: 'Carga k6',
  }), { headers: { 'Content-Type': 'application/json' } });

  check(createRes, { 'criar pauta 201': (r) => r.status === 201 });
  const agendaId = createRes.json('id');

  // 2) abre sessão (10 min)
  const openRes = http.post(`${BASE_URL}/api/agenda/abrir-sessao`, JSON.stringify({
    agendaId: agendaId,
    durationSeconds: 600,
  }), { headers: { 'Content-Type': 'application/json' } });

  check(openRes, { 'abrir sessao 200': (r) => r.status === 200 });
  const sessionId = openRes.json('sessionId');

  return { sessionId };
}

export default function (data) {
  const sessionId = data.sessionId;
  // CPF único por VU/iteração
  const cpf = genCpf(__VU * 100000 + __ITER);
  const payload = JSON.stringify({ sessionId, associateId: cpf, vote: 'SIM' });
  const res = http.post(`${BASE_URL}/api/agenda/votar`, payload, { headers: { 'Content-Type': 'application/json' } });

  // Serviço fake pode retornar 200 (voto) ou 404 (UNABLE_TO_VOTE/CPF inválido)
  check(res, {
    'voto 200 ou 404': (r) => r.status === 200 || r.status === 404,
  });

  sleep(0.2);
}

export function teardown(data) {
  // opcional: finalizar pauta
}



