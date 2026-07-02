# API de Usuários

API REST para cadastro e gerenciamento básico de usuários, desenvolvida como projeto acadêmico para praticar a construção de uma aplicação em camadas com Spring Boot e persistência em banco de dados MySQL.

> Projeto acadêmico desenvolvido para o estudo de APIs REST com Spring Boot. Como evolução futura, pode receber criptografia de senhas com BCrypt, validações de entrada mais completas e autenticação.

## Tecnologias

- Java 21
- Spring Boot 4.0.0
- Spring Web
- Spring Data JPA
- Hibernate
- MySQL
- Maven (com Maven Wrapper)
- JUnit 5 e Spring Boot Test

## Funcionalidades

- Cadastro de usuários com nome de usuário, e-mail e senha
- Consulta de um usuário pelo UUID
- Listagem de todos os usuários
- Atualização dos dados de um usuário na camada de serviço
- Exclusão de usuários pelo UUID
- Persistência dos dados em MySQL com criação/atualização do esquema pelo Hibernate

## Endpoints

| Método | Rota | Descrição | Resposta esperada |
| --- | --- | --- | --- |
| `POST` | `/api/users` | Cadastra um usuário | `201 Created` |
| `GET` | `/api/users` | Lista os usuários | `200 OK` |
| `GET` | `/api/users/{userId}` | Busca um usuário pelo UUID | `200 OK` ou `404 Not Found` |
| `DELETE` | `/api/users/{userId}` | Exclui um usuário | `204 No Content` |

O projeto possui uma operação de atualização, mas o mapeamento atual do `PUT` não inclui `{userId}` na rota apesar de esperar esse parâmetro. Por isso, esse endpoint precisa de uma correção no código antes de ser utilizado. A limitação é documentada aqui sem alterar a regra de negócio.

## Como instalar e executar

### Pré-requisitos

- JDK 21
- MySQL em execução
- Git

Não é necessário instalar o Maven: o repositório inclui o Maven Wrapper.

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

As tabelas são gerenciadas pelo Hibernate porque a propriedade `spring.jpa.hibernate.ddl-auto` está definida como `update`.

### 3. Informe as credenciais do MySQL

Para não salvar credenciais no código, forneça-as por variáveis de ambiente ao iniciar a aplicação:

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

## Exemplo de uso

Cadastre um usuário:

```bash
curl -i -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"usuario","email":"usuario@example.com","password":"senha"}'
```

Liste os usuários:

```bash
curl http://localhost:8080/api/users
```

## Estrutura do projeto

```text
ApiUsers/
├── src/
│   ├── main/
│   │   ├── java/poo/ASemestral/ApiUsers/
│   │   │   ├── controller/   # Endpoints REST e DTOs
│   │   │   ├── entity/       # Entidade JPA de usuário
│   │   │   ├── repository/   # Acesso aos dados com Spring Data JPA
│   │   │   ├── service/      # Regras e operações da aplicação
│   │   │   └── ApiUsersApplication.java
│   │   └── resources/
│   │       ├── application-example.properties  # Modelo de configuração versionado
│   │       └── application.properties          # Configuração local ignorada pelo Git
│   └── test/                    # Testes automatizados
├── .mvn/                        # Configuração do Maven Wrapper
├── mvnw
├── mvnw.cmd
└── pom.xml                      # Dependências e build Maven
```

## Objetivo acadêmico e aprendizados

O projeto exercita conceitos de Programação Orientada a Objetos e desenvolvimento back-end, incluindo:

- separação de responsabilidades em camadas;
- criação de endpoints REST e uso de códigos HTTP;
- transferência de dados com DTOs;
- mapeamento objeto-relacional com JPA/Hibernate;
- operações CRUD com Spring Data JPA;
- integração de uma aplicação Java com MySQL;
- uso de UUID como identificador de entidades.

## Melhorias futuras

- Aplicar BCrypt ao armazenamento de senhas e evitar a exposição de dados sensíveis nas respostas.
- Adicionar autenticação, autorização e validações de entrada mais completas.
- Ajustar o mapeamento da rota de atualização.
- Ampliar a cobertura de testes para os endpoints e as regras da aplicação.
