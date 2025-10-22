# Desafio Técnico Full Stack - SIFAT Sistemas: Back-End

## 🎯 Objetivo do Projeto

Este repositório contém a solução Back-End para o Desafio Técnico da SIFAT Sistemas. O objetivo é demonstrar proficiência na construção de APIs RESTful, na arquitetura de microsserviços, e na comunicação assíncrona utilizando Apache Kafka e Spring Boot.

## 🏛️ Arquitetura de Microsserviços

O Back-End é composto por dois microsserviços que interagem de forma independente e assíncrona:

1.  **`product-service`**:
    * Responsável pelo CRUD completo de produtos, paginação e filtros.
    * Atua como **Produtor Kafka**, enviando eventos para o tópico `'product.events.v1'` a cada operação de CRUD.

2.  **`inventory-service`**:
    * Responsável por consumir eventos do Kafka e manter o controle de estoque.
    * Possui *endpoints* REST para consulta e ajuste de inventário.

## 🛠️ Tecnologias Principais

| Categoria | Tecnologia | Versão | Uso no Projeto |
| :--- | :--- | :--- | :--- |
| **Linguagem** | Java | 17 | Código principal. |
| **Framework** | Spring Boot | 3.x | Desenvolvimento dos microsserviços. |
| **Gerenciador** | Maven | - | Controle de dependências e *build*. |
| **Banco de Dados** | MySQL | 8+ | Persistência de Produtos e Estoque. |
| **Migrações** | Flyway | - | Versionamento e controle do esquema do DB. |
| **Mensageria** | Apache Kafka | - | Comunicação assíncrona entre os serviços. |
| **Infra** | Docker & Compose | - | Orquestração do ambiente completo. |

## 📂 Estrutura do Repositório
desafio-sifat-backend/
├── inventory-service/
│   └── src/main/java/br/com/sifat/desafio/inventoryservice
│   └── src/main/resources
│   └── pom.xml
│   └── Dockerfile
├── product-service/
│   └── src/main/java/br/com/sifat/desafio/productservice
│   └── src/main/resources
│   └── pom.xml
│   └── Dockerfile
├── .gitignore
├── docker-compose.yml
└── README.md

---

### **Instruções de Execução**

* As instruções completas para rodar o ambiente localmente e para subir todo o ambiente (via **Docker Compose**) serão detalhadas ao final do desenvolvimento.