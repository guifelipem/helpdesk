\echo 'Recriando os dados de demonstracao...'

BEGIN;

-- Todo usuario da seed usa a senha local admin@123.
-- O sufixo exclusivo permite limpar os dados sem atingir contas comuns.
INSERT INTO users (name, email, password_hash, role, created_at, active)
SELECT
    agent_names[n],
    'agente.' || lpad(n::text, 2, '0') || '.demo@helpdesk.local',
    '$2a$12$zUvSd9SszKoZnuoHfPxrA.LDYSE/Fc2d9xFVIg0Fbl4J3TX2SSWpm',
    'AGENT',
    CURRENT_TIMESTAMP - (INTERVAL '180 days' - n * INTERVAL '3 days'),
    TRUE
FROM (SELECT ARRAY['Ana Martins', 'Bruno Tavares', 'Carla Nogueira', 'Diego Almeida', 'Fernanda Rocha'] agent_names) names
CROSS JOIN generate_series(1, 5) n
ON CONFLICT (email) DO UPDATE SET
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role,
    active = EXCLUDED.active;

INSERT INTO users (name, email, password_hash, role, created_at, active)
SELECT
    client_names[n],
    'cliente.' || lpad(n::text, 2, '0') || '.demo@helpdesk.local',
    '$2a$12$zUvSd9SszKoZnuoHfPxrA.LDYSE/Fc2d9xFVIg0Fbl4J3TX2SSWpm',
    'CLIENT',
    CURRENT_TIMESTAMP - (INTERVAL '150 days' - n * INTERVAL '2 days'),
    n NOT IN (19, 20)
FROM (
    SELECT ARRAY[
        'Lucas Ribeiro', 'Mariana Costa', 'Rafael Souza', 'Juliana Mendes', 'Gustavo Lima',
        'Camila Ferreira', 'Felipe Santos', 'Beatriz Oliveira', 'André Carvalho', 'Larissa Gomes',
        'Rodrigo Barbosa', 'Patrícia Castro', 'Marcelo Teixeira', 'Renata Dias', 'Eduardo Moreira',
        'Aline Cardoso', 'Thiago Correia', 'Isabela Freitas', 'Vinícius Moura', 'Natália Pires'
    ] client_names
) names
CROSS JOIN generate_series(1, 20) n
ON CONFLICT (email) DO UPDATE SET
    name = EXCLUDED.name,
    password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role,
    active = EXCLUDED.active;

-- A reexecucao substitui somente os chamados pertencentes aos clientes demo.
DELETE FROM ticket_history
WHERE ticket_id IN (
    SELECT t.id FROM tickets t
    JOIN users u ON u.id = t.created_by_id
    WHERE u.email LIKE 'cliente.%.demo@helpdesk.local'
);

DELETE FROM comments
WHERE ticket_id IN (
    SELECT t.id FROM tickets t
    JOIN users u ON u.id = t.created_by_id
    WHERE u.email LIKE 'cliente.%.demo@helpdesk.local'
);

DELETE FROM tickets
WHERE created_by_id IN (
    SELECT id FROM users WHERE email LIKE 'cliente.%.demo@helpdesk.local'
);

CREATE TEMP TABLE demo_ticket_source AS
SELECT
    n,
    titles[((n - 1) % cardinality(titles)) + 1]
        || ' - ' || contexts[((n - 1) % cardinality(contexts)) + 1] AS title,
    descriptions[((n - 1) % cardinality(descriptions)) + 1] AS description,
    CASE
        WHEN n % 10 IN (0, 1) THEN 'OPEN'
        WHEN n % 10 IN (2, 3) THEN 'IN_PROGRESS'
        WHEN n % 10 = 4 THEN 'WAITING_CLIENT'
        WHEN n % 10 = 5 THEN 'WAITING_AGENT'
        WHEN n % 10 IN (6, 7) THEN 'RESOLVED'
        ELSE 'CLOSED'
    END AS status,
    CASE WHEN n % 5 = 0 THEN 'HIGH' WHEN n % 5 IN (1, 2) THEN 'LOW' ELSE 'MEDIUM' END AS priority,
    CURRENT_TIMESTAMP - ((101 - n) * INTERVAL '20 hours') AS created_at,
    'cliente.' || lpad((((n - 1) % 20) + 1)::text, 2, '0') || '.demo@helpdesk.local' AS client_email,
    'agente.' || lpad((((n - 1) % 5) + 1)::text, 2, '0') || '.demo@helpdesk.local' AS agent_email
FROM generate_series(1, 100) n
CROSS JOIN (
    SELECT
        ARRAY[
            'VPN desconecta durante o expediente', 'Acesso bloqueado ao portal financeiro',
            'Impressora não aparece na rede', 'E-mail corporativo sem sincronização',
            'Erro ao emitir segunda via da fatura', 'Pasta compartilhada sem permissão',
            'Sistema apresenta lentidão no login', 'Monitor externo sem sinal',
            'Falha na autenticação em dois fatores', 'Relatório mensal não é exportado',
            'Aplicativo fecha ao anexar documento', 'Cadastro de fornecedor não é concluído',
            'Áudio não funciona em reuniões', 'Senha expirada sem opção de troca',
            'Pedido aparece com valor incorreto', 'Estação reinicia após atualização',
            'Arquivo removido precisa ser restaurado', 'Integração não atualiza novos registros',
            'Licença do editor não foi reconhecida', 'Notificações chegam com atraso'
        ] titles,
        ARRAY['Matriz', 'Filial Centro', 'Home office', 'Unidade Norte', 'Unidade Sul'] contexts,
        ARRAY[
            'O problema começou hoje pela manhã e continua mesmo após reiniciar o equipamento. Preciso do acesso para concluir minhas atividades.',
            'A funcionalidade operava normalmente até ontem. Testei novamente em outro navegador, mas recebi a mesma mensagem de erro.',
            'O comportamento ocorre de forma intermitente e está afetando mais de uma tentativa. Seguem no chamado os detalhes necessários para análise.',
            'Não houve alteração de senha ou configuração por minha parte. Peço apoio para validar as permissões e orientar os próximos passos.',
            'Já executei as verificações básicas indicadas na central de ajuda, sem sucesso. O incidente está bloqueando uma entrega desta semana.'
        ] descriptions
) content;

INSERT INTO tickets (title, description, status, priority, created_at, updated_at, created_by_id, assigned_to_id)
SELECT
    s.title,
    s.description,
    s.status,
    s.priority,
    s.created_at,
    s.created_at + CASE s.status
        WHEN 'OPEN' THEN INTERVAL '0 hours'
        WHEN 'IN_PROGRESS' THEN CASE WHEN s.n % 10 = 3 THEN INTERVAL '42 hours' ELSE INTERVAL '10 hours' END
        WHEN 'WAITING_CLIENT' THEN INTERVAL '24 hours'
        WHEN 'WAITING_AGENT' THEN INTERVAL '36 hours'
        WHEN 'RESOLVED' THEN INTERVAL '30 hours'
        WHEN 'CLOSED' THEN INTERVAL '48 hours'
    END,
    client_user.id,
    CASE WHEN s.status = 'OPEN' THEN NULL ELSE agent_user.id END
FROM demo_ticket_source s
JOIN users client_user ON client_user.email = s.client_email
JOIN users agent_user ON agent_user.email = s.agent_email
ORDER BY s.n;

CREATE TEMP TABLE demo_tickets AS
SELECT t.id AS ticket_id, row_number() OVER (ORDER BY t.id)::integer AS n
FROM tickets t
JOIN users u ON u.id = t.created_by_id
WHERE u.email LIKE 'cliente.%.demo@helpdesk.local';

-- Criação registrada para todos os chamados.
INSERT INTO ticket_history (ticket_id, action, old_value, new_value, performed_by, created_at)
SELECT t.id, 'TICKET_CREATED', NULL, 'OPEN', t.created_by_id, t.created_at
FROM tickets t
JOIN demo_tickets d ON d.ticket_id = t.id;

-- Chamados fora da fila possuem a atribuição e a transição inicial correspondentes.
INSERT INTO ticket_history (ticket_id, action, old_value, new_value, performed_by, created_at)
SELECT t.id, 'STATUS_CHANGED', 'OPEN', 'IN_PROGRESS', t.assigned_to_id, t.created_at + INTERVAL '6 hours'
FROM tickets t JOIN demo_tickets d ON d.ticket_id = t.id
WHERE t.assigned_to_id IS NOT NULL;

INSERT INTO ticket_history (ticket_id, action, old_value, new_value, performed_by, created_at)
SELECT t.id, 'TICKET_ASSIGNED', NULL, agent.name, t.assigned_to_id, t.created_at + INTERVAL '6 hours 1 minute'
FROM tickets t
JOIN demo_tickets d ON d.ticket_id = t.id
JOIN users agent ON agent.id = t.assigned_to_id
WHERE t.assigned_to_id IS NOT NULL;

-- Conversas públicas e anotações internas coerentes com o atendimento.
INSERT INTO comments (ticket_id, user_id, message, is_internal, created_at)
SELECT t.id, t.assigned_to_id,
       'Olá! Recebi seu chamado e iniciei a análise. Vou verificar os registros e retorno em seguida.',
       FALSE, t.created_at + INTERVAL '8 hours'
FROM tickets t JOIN demo_tickets d ON d.ticket_id = t.id
WHERE t.assigned_to_id IS NOT NULL;

INSERT INTO comments (ticket_id, user_id, message, is_internal, created_at)
SELECT t.id, t.created_by_id,
       CASE d.n % 3
           WHEN 0 THEN 'Obrigado pelo retorno. O problema ainda acontece quando repito o procedimento.'
           WHEN 1 THEN 'Certo, fico no aguardo. Se precisar, consigo enviar mais detalhes do erro.'
           ELSE 'Fiz um novo teste agora e o comportamento permanece igual.'
       END,
       FALSE, t.created_at + INTERVAL '10 hours'
FROM tickets t JOIN demo_tickets d ON d.ticket_id = t.id
WHERE t.assigned_to_id IS NOT NULL;

INSERT INTO comments (ticket_id, user_id, message, is_internal, created_at)
SELECT t.id, t.assigned_to_id,
       CASE d.n % 3
           WHEN 0 THEN 'Validar logs do serviço antes de aplicar qualquer alteração no ambiente.'
           WHEN 1 THEN 'Possível falha de permissão. Conferir o grupo do usuário no diretório.'
           ELSE 'Reproduzido em teste. Solução conhecida pode ser aplicada com baixo impacto.'
       END,
       TRUE, t.created_at + INTERVAL '11 hours'
FROM tickets t JOIN demo_tickets d ON d.ticket_id = t.id
WHERE t.assigned_to_id IS NOT NULL AND d.n % 3 = 0;

-- Etapas finais variam conforme o estado atual de cada chamado.
INSERT INTO ticket_history (ticket_id, action, old_value, new_value, performed_by, created_at)
SELECT t.id, 'STATUS_CHANGED', 'IN_PROGRESS', 'WAITING_CLIENT', t.assigned_to_id, t.created_at + INTERVAL '24 hours'
FROM tickets t JOIN demo_tickets d ON d.ticket_id = t.id
WHERE t.status IN ('WAITING_CLIENT', 'WAITING_AGENT');

INSERT INTO ticket_history (ticket_id, action, old_value, new_value, performed_by, created_at)
SELECT t.id, 'STATUS_CHANGED', 'WAITING_CLIENT', 'WAITING_AGENT', t.created_by_id, t.created_at + INTERVAL '36 hours'
FROM tickets t JOIN demo_tickets d ON d.ticket_id = t.id
WHERE t.status = 'WAITING_AGENT';

INSERT INTO ticket_history (ticket_id, action, old_value, new_value, performed_by, created_at)
SELECT t.id, 'STATUS_CHANGED', 'IN_PROGRESS', 'RESOLVED', t.assigned_to_id, t.created_at + INTERVAL '30 hours'
FROM tickets t JOIN demo_tickets d ON d.ticket_id = t.id
WHERE t.status IN ('RESOLVED', 'CLOSED');

INSERT INTO ticket_history (ticket_id, action, old_value, new_value, performed_by, created_at)
SELECT t.id, 'STATUS_CHANGED', 'RESOLVED', 'CLOSED', t.created_by_id, t.created_at + INTERVAL '48 hours'
FROM tickets t JOIN demo_tickets d ON d.ticket_id = t.id
WHERE t.status = 'CLOSED';

-- Parte dos chamados em andamento representa uma resolução recusada pelo cliente.
INSERT INTO ticket_history (ticket_id, action, old_value, new_value, performed_by, created_at)
SELECT t.id, 'STATUS_CHANGED', 'IN_PROGRESS', 'RESOLVED', t.assigned_to_id, t.created_at + INTERVAL '30 hours'
FROM tickets t JOIN demo_tickets d ON d.ticket_id = t.id
WHERE t.status = 'IN_PROGRESS' AND d.n % 10 = 3;

INSERT INTO ticket_history (ticket_id, action, old_value, new_value, details, performed_by, created_at)
SELECT t.id, 'RESOLUTION_REJECTED', 'RESOLVED', 'IN_PROGRESS',
       'O problema voltou a ocorrer após alguns minutos de uso.',
       t.created_by_id, t.created_at + INTERVAL '42 hours'
FROM tickets t JOIN demo_tickets d ON d.ticket_id = t.id
WHERE t.status = 'IN_PROGRESS' AND d.n % 10 = 3;

COMMIT;

\echo 'Seed concluida: 5 agentes, 20 clientes, 100 chamados e conversas/historicos relacionados.'
