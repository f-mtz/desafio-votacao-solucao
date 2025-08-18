# Instalações

- Java 17 LTS
- Maven 3.2.6
- SO: Windows 11
- Gerenciador de versões: [SDKMan](https://sdkman.io/install/)
- Docker Engine 28.3.2
- docker-compose: v2.39.1-desktop

---
# Deploy localhost

```bash
mvn clean install -- build
```

```bash
mvn clean install -DskipTests -- ignorando tests unitários
```

subindo a aplicação localhost sem docker

```bash
mvn spring-boot:run
```

Subindo com docker

```bash
docker compose up --build -d api
```

```bash
docker stop CONTAINER_ID
```

# Banco H2

Acesse o banco H2: http://localhost:8080/h2-console

**jdbc url

```jdbc-url
 jdbc:h2:file:./data/desafio-votacao
```

**user**:

```login
sa
```

**password**: Não se aplica


---
# Documentação e Testes da API REST

Para testar os endpoints acesse:

- Acesse a UI: [Swagger API Votação UI](http://localhost:8080/swagger-ui/index.html)
- **Documentos JSON**: [API Docs Json](http://localhost:8080/v3/api-docs)

> [!tip] Swagger
> 
> Ao invés escrever as requests em markdown, implemente o Swagger


# Testes de API via Swagger

## Desafio Principal
### Votar

1. Crie um Agenda (pauta), que representa o evento de votação: POST /api/agenda
2. Crie uma sessão (janela de votação): POST /api/agenda/abrir-sessao
3. Realize o voto: POST /api/agenda/votar

> [!NOTE] Comportamentos do Produto
> 
> - Não criei uma regra para fechamento automático da sessão pois, não há convenção especificada sobre quantas sessões serão abertas e nem qual sessão determina o encerramento das votações
> - ad

## Tarefa Bônus 1

Emulação de microserviço de validação com padrão Façade

GET

```http
/api/associates/{associateId}/status
```

## Tarefa Bônus 2

Testes de performance: K6 para teste de cargas (emulação de acessos simultâneos)

### Motivos da adoção do K6

- Super leve, fácil de rodar em containers (ex: Docker, Kubernetes).
- Muito usado por times **DevOps/Cloud** (CICD, pipelines).
- Foco em **simplicidade e integração**.

### k6 (resumo no terminal ou export JSON)

Usando docker compose (já configurado no projeto):

```bash
docker compose up --build k6
```

Exemplo de log que deve aparecer ao subir a aplicação:

```bash
#15 resolving provenance for metadata file
#15 DONE 0.0s
[+] Running 3/3
 ✔ desafio-votacao-api:latest       Built                                                                              0.0s 
 ✔ Container desafio-votacao-api-1  Recreated                                                                          0.7s 
 ✔ Container desafio-votacao-k6-1   Created                                                                            0.1s 
Attaching to k6-1
k6-1  |
k6-1  |           /\      |‾‾| /‾‾/   /‾‾/   
k6-1  |      /\  /  \     |  |/  /   /  /                                                                                   
k6-1  |     /  \/    \    |     (   /   ‾‾\                                                                                 
k6-1  |    /          \   |  |\  \ |  (‾)  |                                                                                
k6-1  |   / __________ \  |__| \__\ \_____/ .io
k6-1  |                                                                                                                     
k6-1  |      execution: local                                                                                               
k6-1  |         script: /scripts/votar.js
k6-1  |         output: -
k6-1  |
k6-1  |      scenarios: (100.00%) 1 scenario, 200 max VUs, 2m15s max duration (incl. graceful stop):
k6-1  |               * default: Up to 200 looping VUs for 1m45s over 4 stages (gracefulRampDown: 30s, gracefulStop: 30s)   
k6-1  |
k6-1  |
k6-1  | running (0m01.0s), 002/200 VUs, 3 complete and 0 interrupted iterations
k6-1  | default   [   1% ] 002/200 VUs  0m00.8s/1m45.0s
k6-1  |
k6-1  | running (0m02.0s), 003/200 VUs, 14 complete and 0 interrupted iterations
k6-1  | default   [   2% ] 003/200 VUs  0m01.8s/1m45.0s
k6-1  |
k6-1  | running (0m03.0s), 004/200 VUs, 30 complete and 0 interrupted iterations
```
- Ver o resumo no terminal:

```bash
docker compose logs -f k6
```

- Exportar um JSON com o resumo (salvo em api/perf/k6/summary.json):

```
docker compose run --rm \
```

```bash
 k6 run --summary-export=/scripts/summary.json /scripts/votar.js
```


Depois abra `~\desafio-votacao\api\perf\k6\summary.json.`

## Tarefa Bônus 3


- **Estratégia de commits**: Conventional Commits
- **Documentação e Testes da API**: Swagger para documentação
- **Testes Unitário**: JUnit5 + Mockito
- **Limpeza de código**: Clean Code, S.O.L.I.D, Lombok...
- **Arquitetura do Projeto**: API REST + MVC
- **Logs de aplicação**: log4j
- CHANGELOG.md para versionamento e histórico de releases

### ### Explicação breve das escolhas

**Stack e arquitetura**

- Spring Boot + JPA/Hibernate + H2: rapidez de desenvolvimento, banco embarcado fácil de versionar e persistido via arquivo para não perder dados em restart.
- Arquitetura MVC: separação de responsabilidades, testabilidade e manutenção.

**Modelo de domínio**

- Constraint de unicidade em Vote (session_id, associate_id): garante regra de 1 voto por associado/sessão no nível de banco.
- Entidades simples e relacionamentos explícitos: VotingSession referencia Agenda, e Vote referencia VotingSession.

**Validação de CPF (facade mock)**

- MockAssociateValidatorClient com decisão aleatória: atende ao bônus de integração externa sem dependência real.
- Semântica de erro: retorna 404 tanto para CPF inválido quanto para UNABLE_TO_VOTE, conforme requisito do bônus.
- Endpoint dedicado: GET /api/associates/{associateId}/status para testes e integração.

 **Erros e validações**

- ApiException centralizada e @ExceptionHandler: padroniza respostas e códigos HTTP.
- DTOs com bean validation: contratos de entrada/saída estáveis e documentados.

**Observabilidade**

- Logs nas camadas service/controller: entradas, decisões e resultados para troubleshooting em incidentes.
- Healthcheck no Docker: verificação automática de prontidão.

**Documentação**

- OpenAPI/Swagger: descoberta e teste das rotas.
- app.base-url + UrlBuilder: domínio configurável para callbacks, facilitando testes em emuladores e dispositivos (via APP_BASE_URL no compose).

**Empacotamento e deploy**

- Docker multi-stage: imagem menor e build reprodutível.
- Volume para H2 (/app/data): dados persistentes entre reinícios.

**Performance**

- Gatling e k6: dois enfoques complementares para carga; relatórios HTML (Gatling) e thresholds (k6).
- Plugin Gatling em profile: move para perf para não quebrar builds de imagem no CI/CD.

**Boas práticas de projeto**

- Conventional Commits + CHANGELOG: histórico claro e geração de release/changelog.
- docker-compose.yml com serviços api e k6: execução simples de ponta a ponta.


# Possíveis melhorias a adotar


- Integrar com Apache Kafka para ... em caso de a escala/volume de votos sejam na ordem de centenas
- Kibana ou Dynatrace para captura de logs de aplicação

## 🧩 Por que adotar o Kafka nesse contexto?

Um sistema de votação parece simples (CRUD de votos), mas quando você coloca em perspectiva:

- **Milhares ou milhões de votos chegando ao mesmo tempo**
- **Necessidade de garantir que nenhum voto se perca**
- **Votos sendo consumidos por diferentes serviços (resultado, auditoria, notificações)**
- **Reprocessamento em caso de falha**

O **banco de dados sozinho** pode virar gargalo.  
É aí que entra o **Kafka como um buffer + log distribuído** que garante **escala e resiliência**.