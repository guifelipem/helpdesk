import { useState } from "react";
import { ArrowRightLeft, Eye, RotateCcw } from "lucide-react";

import { Button } from "@/components/ui/button";
import { useActiveAgents } from "@/features/users/hooks/use-users";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";

import { useReturnTicketToQueue, useTransferTicket } from "../hooks/use-tickets";
import type { Ticket } from "../types/ticket.types";

export function AdminTicketActions({ ticket }: { ticket: Ticket }) {
    const [agentId, setAgentId] = useState("");
    const canReassign = ticket.assignedTo !== null
        && ["OPEN", "IN_PROGRESS", "WAITING_CLIENT", "WAITING_AGENT"].includes(ticket.status);
    const agentsQuery = useActiveAgents(canReassign);
    const transfer = useTransferTicket();
    const returnToQueue = useReturnTicketToQueue();
    const availableAgents = (agentsQuery.data ?? []).filter((agent) => agent.id !== ticket.assignedTo?.id);
    const mutationError = transfer.error ?? returnToQueue.error;

    function handleTransfer() {
        const selectedAgent = availableAgents.find((agent) => agent.id === Number(agentId));
        if (!selectedAgent || !window.confirm(`Transferir este chamado para ${selectedAgent.name}?`)) return;
        transfer.mutate(
            { id: ticket.id, agentId: selectedAgent.id },
            { onSuccess: () => setAgentId("") },
        );
    }

    function handleReturnToQueue() {
        if (!window.confirm("Remover o agente responsável e devolver este chamado para a fila?")) return;
        returnToQueue.mutate(ticket.id);
    }

    return (
        <div className="space-y-4 rounded-2xl border border-white/20 bg-black/15 p-4 backdrop-blur-sm">
            <div className="flex items-start gap-3">
                <span className="flex size-9 shrink-0 items-center justify-center rounded-xl bg-white/15"><Eye className="size-4" /></span>
                <div>
                    <p className="font-semibold">Modo de supervisão</p>
                    <p className="mt-1 text-xs leading-5 text-white/70">Você pode consultar todos os dados e gerenciar a atribuição, sem atuar no atendimento.</p>
                </div>
            </div>

            {canReassign ? (
                <div className="grid gap-3 border-t border-white/15 pt-4 md:grid-cols-[1fr_auto_auto]">
                    <select
                        value={agentId}
                        onChange={(event) => setAgentId(event.target.value)}
                        disabled={agentsQuery.isLoading || transfer.isPending || returnToQueue.isPending}
                        className="h-10 rounded-xl border border-white/25 bg-white/10 px-3 text-sm text-white outline-none [&>option]:text-black"
                    >
                        <option value="">{agentsQuery.isLoading ? "Carregando agentes..." : "Selecione o agente de destino"}</option>
                        {availableAgents.map((agent) => <option key={agent.id} value={agent.id}>{agent.name} — {agent.email}</option>)}
                    </select>
                    <Button type="button" variant="secondary" disabled={!agentId || transfer.isPending || returnToQueue.isPending} onClick={handleTransfer}>
                        <ArrowRightLeft /> {transfer.isPending ? "Transferindo..." : "Transferir"}
                    </Button>
                    <Button type="button" className="border-white/25 bg-white/10 text-white shadow-none hover:bg-white/20" variant="outline" disabled={transfer.isPending || returnToQueue.isPending} onClick={handleReturnToQueue}>
                        <RotateCcw /> {returnToQueue.isPending ? "Devolvendo..." : "Devolver à fila"}
                    </Button>
                </div>
            ) : ticket.assignedTo ? (
                <p className="border-t border-white/15 pt-4 text-xs text-white/70">Este chamado está em um estado final e não pode ser transferido nem devolvido à fila.</p>
            ) : (
                <p className="border-t border-white/15 pt-4 text-xs text-white/70">Este chamado já está disponível na fila e ainda não possui responsável.</p>
            )}

            {agentsQuery.isError && <p className="text-sm text-red-200">Não foi possível carregar os agentes disponíveis.</p>}
            {mutationError && <p role="alert" className="text-sm text-red-200">{getApiErrorMessage(mutationError, "Não foi possível gerenciar a atribuição.")}</p>}
        </div>
    );
}
