# Frontend do Helpdesk

Interface web do projeto Helpdesk, desenvolvida com React, TypeScript e Vite.

## Requisitos

- Node.js 22
- Backend disponível localmente

## Configuração

Copie `.env.example` para `.env.local` caso precise alterar a URL da API:

```env
VITE_API_URL=http://localhost:8080/api
```

Sem essa variável, a aplicação usa `http://localhost:8080/api`.

## Scripts

```bash
npm ci
npm run dev
npm run lint
npm run build
npm run preview
```

- `dev`: inicia o servidor de desenvolvimento.
- `lint`: executa as verificações estáticas do ESLint.
- `build`: valida o TypeScript e gera o bundle de produção.
- `preview`: serve localmente o bundle gerado.
