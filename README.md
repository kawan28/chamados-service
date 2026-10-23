# ThinDesk · Microsserviço de Chamados

Microsserviço em **Java 17 + Spring Boot 3 + API REST + MongoDB** responsável pela
**abertura, acompanhamento e resolução de chamados técnicos** do ThinDesk.

Ele **não tem login próprio**: reutiliza a autenticação do serviço de login já existente
(Spring Security + Spring Session no MongoDB).

## Arquitetura

```
                 ┌──────────────────────────┐
  1. login       │  Serviço de login        │   grava a sessão
 ─────────────►  │  (porta 8080)            │ ─────────────────┐
  cookie SESSION │  Spring Security         │                  ▼
 ◄─────────────  └──────────────────────────┘        ┌───────────────────┐
                                                     │  MongoDB          │
                 ┌──────────────────────────┐        │  ├─ users         │
  2. /api/...    │  Microsserviço Chamados  │  lê    │  ├─ sessions  ◄───┤
 ─────────────►  │  (porta 8081)            │ ◄──────┤  └─ chamados      │
  mesmo cookie   │  ESTE PROJETO            │ grava  └───────────────────┘
                 └──────────────────────────┘
```

**Reutilização da autenticação:** o usuário faz login no serviço de login, que grava a
sessão na coleção `sessions`. O microsserviço lê essa mesma sessão (Spring Session,
cookie `SESSION`) e obtém o id, o nome e os perfis do usuário. Sem login, qualquer rota
responde **401**.

Para ler a sessão, a classe `br.com.securelogin.security.SecurityUser` existe aqui com
o mesmo pacote, campos e `serialVersionUID` do serviço de login (contrato compartilhado).

**Perfis:** `USER` abre e acompanha os próprios chamados. `MANAGER` e `ADMIN` são
**técnicos**, podem consultar qualquer chamado e resolvê-lo (configurável em
`thindesk.perfis-tecnico`).

## Endpoints

| Método | Rota | Quem pode | Descrição | Sucesso |
|---|---|---|---|---|
| `POST` | `/api/chamados` | autenticado | Abre um chamado (título e descrição) | `201` |
| `GET` | `/api/chamados/usuario/{id}` | o próprio usuário ou técnico | Lista os chamados do usuário | `200` |
| `GET` | `/api/chamados/{id}` | solicitante ou técnico | Detalhes e status do chamado | `200` |
| `PUT` | `/api/chamados/{id}/resolver` | técnico | Registra a solução, status vira `RESOLVIDO` | `200` |
| `GET` | `/api/me` | autenticado | Mostra o usuário reconhecido pela sessão | `200` |

### Exemplos

**Abrir chamado:** `POST /api/chamados`
```json
{ "titulo": "Computador não liga", "descricao": "O computador da sala 12 não liga desde hoje de manhã." }
```
Resposta `201 Created`:
```json
{
  "id": "6718f2c1a3b4c5d6e7f80912",
  "titulo": "Computador não liga",
  "descricao": "O computador da sala 12 não liga desde hoje de manhã.",
  "status": "ABERTO",
  "solicitanteId": "6705a1b2c3d4e5f6a7b8c9d0",
  "solicitanteNome": "Maria",
  "dataAbertura": "2026-10-23T10:15:30.123",
  "solucao": null, "tecnicoId": null, "tecnicoNome": null, "dataResolucao": null
}
```

**Resolver chamado:** `PUT /api/chamados/{id}/resolver`
```json
{ "solucao": "Fonte de alimentação substituída e equipamento testado." }
```

## Regras de negócio

- Solicitante, data de abertura e status `ABERTO` são definidos **pelo sistema** a partir
  do usuário logado. Campos extras enviados no corpo são ignorados.
- Título: obrigatório, 5 a 100 caracteres. Descrição: obrigatória, 10 a 2000. Solução:
  obrigatória, 10 a 2000. Se a validação falhar, responde `400` com o erro de cada campo.
- Usuário comum só lista e consulta os **próprios** chamados (`403` caso contrário).
- Só **técnico** resolve chamados (`403`).
- Chamado `RESOLVIDO` **não pode ser alterado novamente** (`409 Conflict`).
- Controle de concorrência otimista (`@Version`): duas resoluções simultâneas não se sobrescrevem.

Formato de erro:
```json
{ "timestamp": "...", "status": 409, "erro": "Conflict",
  "mensagem": "Este chamado já foi resolvido e não pode ser alterado novamente" }
```

## Como executar

**Pré-requisitos:** Java 17, IntelliJ IDEA (ou Maven 3.9+), MongoDB Atlas, e o serviço
de login rodando.

1. **Configuração:** o microsserviço precisa do **mesmo** `MONGODB_URI` e
   `MONGODB_DATABASE` do serviço de login. Se o serviço de login estiver na pasta vizinha
   `../secure-login` com um `.env`, ele é lido automaticamente. Senão, copie
   `.env.example` para `.env` e preencha.
2. **Suba o serviço de login** (porta 8080).
3. **Suba este microsserviço:** no IntelliJ, abra a pasta e rode `ChamadosServiceApplication`.
   Pelo terminal, use `mvn spring-boot:run`. Ele sobe na porta **8081**.

## Testando com Postman

Importe `postman/ThinDesk-Chamados.postman_collection.json` e preencha as variáveis da
collection (`usuarioEmail`, `usuarioSenha`, `tecnicoEmail`, `tecnicoSenha`). Depois rode
na ordem:

1. **Login:** "Abrir tela de login" → "Entrar como USUÁRIO" → "Quem sou eu"
2. **Usuário:** abrir chamado, validação (400), listar, consultar, tentar resolver (403)
3. **Técnico:** "Abrir tela de login" → "Entrar como TÉCNICO" e depois resolver (200),
   resolver de novo (409), consultar
4. **Sem login:** abrir chamado sem cookie (401)

A persistência pode ser conferida no MongoDB Atlas (*Data Explorer* → banco
`securelogin` → coleção `chamados`).

## Estrutura

```
src/main/java/
├── br/com/thindesk/chamados/
│   ├── config/SecurityConfig.java      # rotas x perfis, sessão compartilhada, erros 401/403 em JSON
│   ├── controller/                     # ChamadoController (API REST), UsuarioController (/api/me)
│   ├── dto/                            # requests com validação e respostas
│   ├── exception/                      # exceções + GlobalExceptionHandler
│   ├── model/                          # Chamado, StatusChamado
│   ├── repository/                     # ChamadoRepository (MongoDB)
│   ├── security/                       # UsuarioLogado, UsuarioLogadoProvider
│   └── service/ChamadoService.java     # regras de negócio
└── br/com/securelogin/security/
    └── SecurityUser.java               # contrato compartilhado com o serviço de login
```
