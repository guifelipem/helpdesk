# Help Desk System

Sistema web de gerenciamento de chamados desenvolvido como projeto de portfólio para simular o fluxo de atendimento de uma central de suporte.

O projeto tem como objetivo aplicar boas práticas de desenvolvimento full stack utilizando Java com Spring Boot no backend e React com TypeScript no frontend, seguindo conceitos de arquitetura em camadas, APIs REST, autenticação com JWT, controle de acesso por perfis e desenvolvimento de interfaces modernas.

## Status do projeto

Este projeto está em desenvolvimento contínuo e faz parte do meu portfólio.

O objetivo é evoluí-lo gradualmente para um sistema completo de Help Desk, implementando funcionalidades encontradas em aplicações utilizadas no mercado, sempre priorizando qualidade de código, organização da arquitetura e boas práticas de desenvolvimento.

A branch `main` representa a versão estável mais recente do projeto, enquanto o desenvolvimento contínuo acontece na branch `develop`.

---

# Tecnologias

## Backend

- Java 21
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Maven
- Swagger / OpenAPI

## Frontend

- React
- TypeScript
- Vite
- React Query
- Zustand
- React Hook Form
- Zod
- Tailwind CSS
- shadcn/ui
- Axios

---

# Funcionalidades

## Autenticação

- Cadastro de usuários
- Login com JWT
- Controle de acesso baseado em perfis (CLIENT, AGENT e ADMIN)
- Rotas protegidas
- Persistência da sessão

## Gerenciamento de chamados

- Criação de chamados
- Listagem de chamados do cliente
- Listagem paginada para agentes
- Busca por título e descrição
- Filtros por status e prioridade
- Visualização detalhada do chamado
- Atribuição do chamado ao agente responsável
- Alteração de status seguindo regras de negócio
- Fechamento do chamado pelo cliente

## Comentários

- Comentários em chamados
- Comentários internos visíveis apenas para agentes e administradores
- Identificação do autor
- Exibição da data e hora dos comentários
- Bloqueio de comentários em chamados encerrados

## Histórico

- Registro automático das alterações do chamado
- Histórico cronológico de eventos
- Atualização automática após alterações

## Experiência do usuário

- Skeletons durante carregamento
- Tratamento de erros da API
- Empty states
- Atualização automática dos dados
- Interface responsiva

---

# Perfis de acesso

## CLIENT

- Criar chamados
- Visualizar apenas os próprios chamados
- Comentar
- Acompanhar o histórico
- Fechar chamados resolvidos

## AGENT

- Visualizar fila de chamados
- Assumir atendimento
- Alterar status
- Registrar comentários internos

## ADMIN

- Acesso às funcionalidades administrativas atualmente implementadas

---

# Estrutura do projeto

```text
helpdesk/
├── backend/
└── frontend/
```

---

# Como executar

## Backend

```bash
cd backend

./mvnw spring-boot:run
```

## Frontend

```bash
cd frontend

npm install
npm run dev
```

---

# Próximas funcionalidades

- Dashboard
- Administração completa de usuários
- Deploy da aplicação
- Ampliação da cobertura de testes
- Melhorias na interface
- Novas funcionalidades administrativas

---

# Imagens

## Listagem de chamados

<img width="1917" height="980" alt="image" src="https://github.com/user-attachments/assets/dcd02fef-5020-48af-9e62-01226a4145d1" />

## Detalhes do chamado

<img width="1897" height="980" alt="image" src="https://github.com/user-attachments/assets/2defb61a-723b-4e4d-a3b8-94d821a3ce47" />

## Histórico do chamado

<img width="1892" height="977" alt="image" src="https://github.com/user-attachments/assets/3918a146-1f96-43a6-9cd3-87951326b79b" />

---

# Aprendizados

Durante o desenvolvimento deste projeto foram aplicados conceitos como:

- Arquitetura em camadas
- APIs REST
- Spring Security
- Autenticação e autorização com JWT
- Controle de acesso baseado em perfis
- Tratamento centralizado de exceções
- Versionamento de banco de dados com Flyway
- React Query para gerenciamento de estado assíncrono
- Gerenciamento de estado com Zustand
- Validação de formulários com React Hook Form e Zod
- Organização de projetos Full Stack
- Boas práticas de Clean Code
- Git e fluxo de desenvolvimento com branches

---

# Licença

Este projeto foi desenvolvido para fins de estudo e portfólio.
