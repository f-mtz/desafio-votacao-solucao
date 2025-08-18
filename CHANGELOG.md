# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato segue as recomendações de Keep a Changelog e Conventional Commits.

## [0.1.0] - 2025-08-18

### Added
- feat(domain): atualizar mapeamentos e regras de Vote/VotingSession
- feat(api): endpoint `GET /api/associates/{associateId}/status` para expor o facade de CPF com payload 404 estruturado (`message`, `code`, `status: UNABLE_TO_VOTE`).
- docs(swagger): descrições e exemplos nas rotas de agenda/sessão/voto.
- perf(gatling): simulação `VotoSimulation.scala` para carga.
- perf(k6): script `api/perf/k6/votar.js` com estágios e thresholds.

### Changed
- build(maven): mover `io.gatling:gatling-maven-plugin` para o profile `perf` (desativado por padrão) para não quebrar build em Docker.

### Ops
- chore(h2): habilitar `spring.h2.console.settings.web-allow-others=true` para acesso remoto ao H2 Console.
- chore(docker): `Dockerfile` multi-stage e `docker-compose.yml` com serviços `api` e `k6` e volume para dados do H2.
