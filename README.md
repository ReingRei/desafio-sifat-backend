# Desafio Técnico Full Stack - SIFAT Sistemas: Back-End

## Objetivo do Projeto

Este repositório contém a solução Back-End para o Desafio Técnico da SIFAT Sistemas. O objetivo é demonstrar proficiência na construção de APIs RESTful, na arquitetura de microsserviços, e na comunicação assíncrona utilizando Apache Kafka e Spring Boot.

## Arquitetura de Microsserviços e Fluxo Assíncrono

O Back-End é composto por dois microsserviços que interagem de forma independente e assíncrona:

1.  **`product-service`**:
    * Responsável pelo CRUD completo de produtos, paginação e filtros.
    * Atua como **Produtor Kafka**, enviando eventos para o tópico `product.events.v1` a cada operação de CRUD.
    * **Implementação de Consistência:** Utiliza lógica de retentativa e lança `RuntimeException` em caso de falha persistente no envio do evento Kafka, forçando o **Rollback** da transação do banco de dados (Two-Phase Commit Pattern).

2.  **`inventory-service`**:
    * Atua como **Consumidor Kafka**, escutando o tópico `product.events.v1` para criar ou desativar o registro de estoque.
    * Possui endpoints REST para consulta e ajuste de inventário.
    * **Bônus (Opcional):** Publica evento `inventory.updated` ao haver alteração de estoque.

---

## Padrões de Design e Qualidade de Código

### Decisões e Padrões de Arquitetura

## Padrões de Design e Qualidade de Código

### 1. Princípios Estruturais (SOLID, KISS, DRY)

| Princípio | Aplicação no Projeto |
| :--- | :--- |
| **SOLID (S - SRP)** | Aplicado na separação de responsabilidades (Controller/Service/Repository/Mapper/ExceptionHandler). |
| **DRY (Don't Repeat Yourself)** | Centralização da lógica de conversão (Mapper), manipulação de exceções (GlobalExceptionHandler) e configuração de prefixos (`context-path`). |
| **KISS (Keep It Simple)** | Uso de padrões Spring (DI, Repository) e escolha da estratégia de Update Atômico para evitar complexidade desnecessária com *locking*. |

### 2. Padrões de Design (GoF e JEE)

| Padrão | Aplicação no Projeto |
| :--- | :--- |
| **Dependency Injection** | Uso de injeção via construtor em todas as camadas para baixo acoplamento. |
| **Data Transfer Object (DTO)** | Contrato da API e separação das camadas de Web/Serviço/Persistência. |
| **Repository** | Abstração da camada de acesso a dados (Spring Data JPA). |
| **Mapper/Assembler** | Classe dedicada à conversão Entidade ⇌ DTO (ex: preço e categorias). |
| **Specification** | Padrão de design para construção de queries dinâmicas e reutilizáveis (filtros do `product-service`). |

### 3. Padrões de Microsserviços

| Padrão | Aplicação no Projeto |
| :--- | :--- |
| **Database Per Service** | Cada serviço possui seu próprio schema de banco de dados. |
| **Asynchronous Messaging** | Comunicação via Kafka para processamento de eventos de forma não-bloqueante. |
| **Two-Phase Commit** | Lógica de rollback no `product-service` para garantir que o evento Kafka e a transação DB sejam atômicos. |


### Tecnologias Principais

| Categoria | Tecnologia | Uso no Projeto |
| :--- | :--- | :--- |
| **Linguagem** | Java 17 | Código principal. |
| **Framework** | Spring Boot 3.x | Desenvolvimento dos microsserviços. |
| **Persistência** | MySQL 8+ & Flyway | Banco de dados relacional e migrações. |
| **Comunicação** | Apache Kafka | Mensageria assíncrona. |
| **Documentação** | Swagger (Springdoc) | Documentação interativa dos endpoints. |
| **Infra** | Docker & Compose | Orquestração do ambiente completo. |

---

## Instruções de Execução

O ambiente completo é orquestrado via Docker Compose.

### Pré-requisitos

* Docker e Docker Compose instalados e em execução.

### 1. Setup e Inicialização

O comando a seguir constrói as imagens, inicializa a infraestrutura e executa as migrações Flyway.

```bash
# Limpa volumes antigos (se necessário) e sobe o ambiente completo
docker compose down -v 
docker compose up --build
```
**Nota sobre Segurança (CORS):** A anotação @CrossOrigin(origins = "*") foi aplicada nos Controllers para permitir a comunicação com o Frontend rodando em localhost. Em produção, este valor deve ser restrito apenas aos domínios confiáveis do cliente.

### 2. Acesso à Aplicação

Os serviços estarão disponíveis nas seguintes portas:

| Serviço | Porta | Swagger UI |
| :--- | :--- | :--- |
| **Product Service** | `8080` | `http://localhost:8080/api/v1/swagger-ui.html` |
| **Inventory Service** | `8081` | `http://localhost:8081/api/v1/swagger-ui.html` |

---

## 📋 Endpoints e Fluxo de Teste

Todos os endpoints estão protegidos por tratamento de erros centralizado (400, 404, 503) e validação de entrada.

### Product Service (`:8080/api/v1`)

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/products` | Lista produtos com paginação e filtros (nome, categoria, faixa de preço). |
| `POST` | `/products` | Cadastra um novo produto (Dispara evento **CREATED**). |
| `PUT` | `/products/{id}` | Atualiza um produto existente (Dispara evento **UPDATED**). |
| `DELETE` | `/products/{id}` | Realiza exclusão lógica (Dispara evento **DELETED**). |
| `GET` | `/categories` | Lista as categorias existentes. |

### Inventory Service (`:8081/api/v1`)

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `GET` | `/inventory/{productId}` | Consulta o saldo de estoque atual de um produto. |
| `PATCH` | `/inventory/{productId}/adjust` | Ajusta atomicamente a quantidade em estoque e registra a movimentação. |

---

## ✅ Cobertura de Testes Automatizados

O projeto possui alta cobertura de testes, conforme requisito do desafio.

* **Testes Unitários (JUnit/Mockito):** Cobrem 100% das regras de negócio e lógica de conversão (Mappers e Services) para ambos os serviços.
* **Testes de Integração Web (@WebMvcTest):** Verificam a camada de Controller, garantindo que os status HTTP (200, 201, 204, 400, 404) e o GlobalExceptionHandler funcionam corretamente.
* **Testes de Kafka:** Validam a emissão de eventos e, principalmente, a lógica de **rollback** do `product-service` em caso de falha de conexão persistente com o Kafka.