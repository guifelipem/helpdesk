\echo 'Removendo os dados de demonstracao...'

BEGIN;

-- Interrompe com segurança se um agente demo estiver ligado a chamado de uma conta comum.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM tickets t
        JOIN users agent ON agent.id = t.assigned_to_id
        JOIN users client ON client.id = t.created_by_id
        WHERE agent.email LIKE 'agente.%.demo@helpdesk.local'
          AND client.email NOT LIKE 'cliente.%.demo@helpdesk.local'
    ) THEN
        RAISE EXCEPTION 'Ha agentes demo vinculados a chamados de clientes comuns; devolva ou transfira esses chamados antes da limpeza';
    END IF;
END $$;

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

DELETE FROM users
WHERE email LIKE 'agente.%.demo@helpdesk.local'
   OR email LIKE 'cliente.%.demo@helpdesk.local';

COMMIT;

\echo 'Dados de demonstracao removidos.'
