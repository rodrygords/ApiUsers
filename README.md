# API de Usuários

API REST acadêmica para cadastro e gerenciamento de usuários, desenvolvida com Java 21 e Spring Boot. O projeto usa persistência em MySQL e mantém a senha armazenada como hash BCrypt, sem retorná-la nas respostas.

## Tecnologias

- Java 21
- Spring Boot 4.0.0
- Spring Web e Spring Validation
- Spring Data JPA e Hibernate
- MySQL
- BCrypt (`spring-security-crypto`)
- Maven Wrapper
- JUnit 5, Mockito e MockMvc

## Funcionalidades

- Cadastro, consulta, listagem, atualização e exclusão de usuários.
- DTOs separados para criação, atualização e resposta; o DTO de resposta não possui senha ou hash.
- Validação de nome, e-mail e senha com Jakarta Bean Validation.
- Normalização do e-mail com remoção de espaços externos e conversão para minúsculas.
- Restrição de unicidade do e-mail no banco e resposta `409 Conflict` para duplicidade.
- Senhas transformadas em hash BCrypt antes da persistência.
- Timestamps separados para criação e atualização.
- Tratamento global de erros com respostas consistentes para validação, UUID inválido, usuário inexistente, duplicidade e erros inesperados.

## Endpoints

| Método | Rota | Descrição | Resposta esperada |
| --- | --- | --- | --- |
| `POST` | `/api/users` | Cadastra um usuário | `201 Created` com `Location` |
| `GET` | `/api/users` | Lista os usuários | `200 OK` |
| `GET` | `/api/users/{userId}` | Busca um usuário pelo UUID | `200 OK` ou `404 Not Found` |
| `PUT` | `/api/users/{userId}` | Atualiza dados e, opcionalmente, a senha | `200 OK` ou `404 Not Found` |
| `DELETE` | `/api/users/{userId}` | Exclui um usuário | `204 No Content` ou `404 Not Found` |

As respostas de usuário usam `UserResponseDto` e não expõem senha nem hash. No `PUT`, campos omitidos permanecem inalterados; uma nova senha, quando enviada, é transformada em novo hash.

## Como executar

### Pré-requisitos

- JDK 21
- MySQL em execução
- Git

O repositório inclui o Maven Wrapper. Como os arquivos não têm permissão de execução no ambiente atual, use `bash ./mvnw` nos comandos abaixo.

### 1. Clone o repositório

```bash
git clone https://github.com/rodrygords/ApiUsers.git
cd ApiUsers
```

### 2. Crie o banco de dados

No MySQL, execute:

```sql
CREATE DATABASE apiusers_db;
```

### 3. Configure as credenciais

Forneça as credenciais por variáveis de ambiente ao iniciar a aplicação:

```bash
SPRING_DATASOURCE_USERNAME=seu_usuario \
SPRING_DATASOURCE_PASSWORD=sua_senha \
bash ./mvnw spring-boot:run
```

No Windows (PowerShell):

```powershell
$env:SPRING_DATASOURCE_USERNAME="seu_usuario"
$env:SPRING_DATASOURCE_PASSWORD="sua_senha"
.\mvnw.cmd spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

### 4. Execute os testes

```bash
bash ./mvnw test
```

Os testes atuais cobrem regras do service com Mockito e endpoints com MockMvc, sem depender de uma instância externa do MySQL.

Para compilar sem executar os testes:

```bash
bash ./mvnw package -DskipTests
```

## Estrutura do projeto

```text
src/main/java/poo/ASemestral/ApiUsers/
├── config/       # Configuração do PasswordEncoder
├── controller/   # Endpoints e DTOs
├── entity/       # Entidade JPA
├── exception/    # Erros e tratamento global
├── repository/   # Acesso aos dados
└── service/      # Regras de negócio
```

## Limites atuais e próximos passos

O projeto não implementa autenticação, login, autorização por papéis, JWT ou refresh token. Esses mecanismos são próximos passos e não devem ser inferidos a partir do hash BCrypt: hash de senha protege o valor persistido, mas não autentica requisições.
