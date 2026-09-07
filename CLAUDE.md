# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ajudaqui-fiscal-lambda** is a Java 17 backend service that processes fiscal documents (cupons fiscais) and manages user access to them. It integrates with PostgreSQL for data persistence and uses stored procedures/functions for complex business logic.

The service is structured as a Lambda-compatible API that handles:
- User registration and management with role-based access control
- Fiscal document parsing and validation via the `ajudaqui-fiscal` external library
- User status and profile management through database procedures
- Query validation against geographic regions (estados)

## Build & Common Commands

### Build
```bash
mvn clean install
```

### Run tests
```bash
mvn test
```

### Run a single test
```bash
mvn test -Dtest=AppTest
```

### Compile without running tests
```bash
mvn clean compile
```

### Build JAR artifact
```bash
mvn clean package
```

### Run the application
```bash
mvn exec:java -Dexec.mainClass="com.ajudaqui.App"
```

### View project dependencies
```bash
mvn dependency:tree
```

## Architecture

### Lambda Flow

The project is designed for AWS Lambda deployment with the following request flow:

```
AWS Lambda Event
    ↓
LambdaHandler (com.ajudaqui.handler.LambdaHandler)
    ├─ Receives request from AWS
    └─ Forwards to appropriate service method
    ↓
CuponFiscalService (com.ajudaqui.service.CuponFiscalService)
    ├─ Contains business logic
    ├─ Orchestrates repository and external service calls
    └─ Returns result
    ↓
UsuarioRepository (com.ajudaqui.repository.UsuarioRepository)
    └─ Accesses PostgreSQL database
    ↓
Database / External Services
```

**Responsibility Separation:**
- `LambdaHandler`: Only receives event and routes to correct service method (does NOT contain business logic)
- `CuponFiscalService`: Contains all business logic and orchestration
- `UsuarioRepository`: Database access only

### Layer Structure

1. **Lambda Handler Layer** (`com.ajudaqui.handler.LambdaHandler`) — **TODO: Create this**
   - Implements AWS Lambda `RequestHandler<Object, Object>` interface
   - Single method: `handleRequest(Object input, Context context)`
   - Routes incoming events to appropriate service methods
   - Handles Lambda-specific concerns (logging, context)

2. **Service Layer** (`com.ajudaqui.service.CuponFiscalService`) — **TODO: Create/Refactor this**
   - Contains all business logic
   - Public method: `processar(Object input)` (or method-specific handlers)
   - Delegates data access to repository layer
   - Calls external `FiscalParseService` from `ajudaqui-fiscal` library

3. **Resource Layer** (`com.ajudaqui.resource.CuponFiscalResource`)
   - Currently contains business logic — should be refactored into `CuponFiscalService`
   - Methods: `buscaCupon()`, `getAllUsers()`, `register()`, `mudatrPerfil()`, `mudarStatus()`

4. **Repository Layer** (`com.ajudaqui.repository.UsuarioRepository`)
   - Data access through PostgreSQL via JDBC
   - All database operations are SQL-based using prepared statements
   - Manages connection pool via HikariCP
   - Database queries defined in `com.ajudaqui.enums.Query` enum

5. **Domain Layer** (`com.ajudaqui.domain.Usuario`)
   - Simple POJO representing a user record
   - Fields: id, token (UUID), registro (timestamp), ultimaSolicitacao, intervalo, ativo

6. **Configuration** (`com.ajudaqui.config.Database`)
   - HikariCP connection pool configuration (max 5 connections, min 1)
   - Static initialization block creates datasource on class load
   - **Security Note**: Database credentials are hardcoded (localhost:5432, postgres/123456) — these must be moved to environment variables for production

### Database Procedures

All business logic for user operations is encapsulated in PostgreSQL stored procedures/functions:

- `criar_usuario(perfil)` — Creates a new user and returns UUID token
- `busca_usuarios(ativo)` — Retrieves users, optionally filtered by active status
- `validador_consulta(token, estado)` — Validates user can query a specific region
- `alternar_status(token_solicitante, token_usuario)` — Toggles user active/inactive status
- `alterar_perfil(token_solicitante, token_usuario, perfil)` — Changes user's role/profile

All database scripts are versioned in `/database/` directory (001-008 numbered migrations).

## Key Files

- `pom.xml` — Maven configuration with Java 17 target
- `src/main/java/com/ajudaqui/resource/` — API endpoints
- `src/main/java/com/ajudaqui/repository/` — Database access layer
- `src/main/java/com/ajudaqui/config/Database.java` — Connection pool setup
- `src/main/java/com/ajudaqui/enums/Query.java` — All SQL queries and procedures
- `database/` — PostgreSQL migration scripts (must be applied in order)

## Database Setup

1. Ensure PostgreSQL is running locally on port 5432
2. Create a database named `postgres` (or update `Database.java` URL)
3. Apply migrations in order:
   ```bash
   psql -U postgres -d postgres -f database/001_create_table_perfil.sql
   psql -U postgres -d postgres -f database/002_create_table_usuario.sql
   psql -U postgres -d postgres -f database/003_create_table_estados.sql
   psql -U postgres -d postgres -f database/004_create_function_registro_usuario.sql
   psql -U postgres -d postgres -f database/005_create_function_buscar_usuario.sql
   psql -U postgres -d postgres -f database/006_create_procedure_valida_consulta.sql
   psql -U postgres -d postgres -f database/007_create_procedure_alterar_status.sql
   psql -U postgres -d postgres -f database/008_create_procedure_alterar_perfil.sql
   ```

## Testing Notes

- Project uses JUnit 5 (Jupiter) for testing
- Current test coverage is minimal (only `AppTest.java` with a placeholder test)
- All database operations are direct JDBC calls — consider integration tests using an in-memory database or test containers for robust testing
- Note: `FiscalParseService` is from external library and cannot be easily tested in isolation

## Dependencies to Know

- **PostgreSQL Driver 42.7.8** — JDBC connectivity
- **HikariCP 7.0.2** — Connection pooling
- **ajudaqui-fiscal 1.0.1** (from jitpack.io) — External library for parsing fiscal documents
- **JUnit 5.11.0** — Test framework

## Next Steps for Lambda Deployment

To deploy this to AWS Lambda, you need to:

1. ✅ **LambdaHandler criado** (`com.ajudaqui.handler.LambdaHandler`)
   - Implementa `RequestHandler<Map<String, Object>, Map<String, Object>>`
   - Recebe eventos da AWS Lambda e roteia para `CuponFiscalResource`
   - Suporta operações: `buscarCupon`, `registrar`, `mudarStatus`, `mudatrPerfil`, `getAllUsers`
   - Tratamento de erros com HTTP status codes (400, 500)

2. ✅ **Dependências AWS Lambda adicionadas ao `pom.xml`**
   - `aws-lambda-java-core` (v1.2.3)
   - `aws-lambda-java-events` (v3.11.3)

3. **Externalize database configuration**
   - Move credentials from hardcoded values to environment variables
   - Use System.getenv() in `Database.java`

4. **Build and package for Lambda**
   - Create a fat JAR (includes all dependencies)
   - Set Lambda handler to: `com.ajudaqui.handler.LambdaHandler::handleRequest`

## Como Usar o LambdaHandler

### Formato do Evento (JSON)

O Lambda recebe eventos com este formato:

```json
{
  "operacao": "buscarCupon",
  "token": "seu-token-uuid",
  "url": "https://exemplo.com/cupom"
}
```

### Operações Suportadas

| Operação | Parâmetros | Retorno |
|----------|-----------|---------|
| `buscarCupon` | token, url | JSON do cupom fiscal |
| `registrar` | perfil | UUID do novo usuário |
| `mudarStatus` | tokenSolicitante, tokenUsuario | Mensagem de sucesso |
| `mudatrPerfil` | tokenSolicitante, tokenUsuario, perfil | Mensagem de sucesso |
| `getAllUsers` | ativo (opcional) | Lista de usuários |

### Resposta do Lambda

**Sucesso (HTTP 200):**
```json
{
  "statusCode": 200,
  "body": { ... resultado ... }
}
```

**Erro (HTTP 400/500):**
```json
{
  "statusCode": 400,
  "error": "Mensagem de erro"
}
```

### Deploy na AWS Lambda

1. **Build do projeto:**
   ```bash
   mvn clean package
   ```

2. **Criar arquivo JAR com dependências:**
   - Usar plugin `maven-shade-plugin` (opcional, mas recomendado)

3. **Upload para AWS Lambda:**
   - Criar função Lambda com runtime Java 17
   - Handler: `com.ajudaqui.handler.LambdaHandler::handleRequest`
   - Variáveis de ambiente: `DB_URL`, `DB_USER`, `DB_PASSWORD`

4. **Configurar Environment Variables na Lambda:**
   ```
   DB_URL=jdbc:postgresql://seu-rds-endpoint:5432/postgres
   DB_USER=seu_usuario
   DB_PASSWORD=sua_senha
   ```

## Development Tips

- Database credentials in `Database.java` must be externalized before production deployment
- All SQL is parameterized using `PreparedStatement` to prevent SQL injection — maintain this pattern
- The `Query` enum centralizes all database operations; add new procedures here
- Remember to apply new database migrations if you add new tables or stored procedures
- Lambda has 15-minute timeout limit — monitor long-running operations
- Use Lambda context for structured logging (context.getLogger())
