import type { TicketHistoryAction, TicketHistoryResponse } from "../types/ticket-history-types";
import { ArrowRightLeft, CirclePlus, RefreshCw, RotateCcw, Undo2, UserCheck, type LucideIcon } from "lucide-react";

const actionLabels: Record<TicketHistoryAction, string> = {
    TICKET_CREATED: "Chamado criado",
    TICKET_ASSIGNED: "Chamado atribuído",
    TICKET_TRANSFERRED: "Chamado transferido",
    TICKET_RETURNED_TO_QUEUE: "Chamado devolvido à fila",
    STATUS_CHANGED: "Status alterado",
    RESOLUTION_REJECTED: "Resolução rejeitada",
};

const statusLabels: Record<string, string> = {
    OPEN: "Aberto",
    IN_PROGRESS: "Em andamento",
    WAITING_CLIENT: "Aguardando cliente",
    WAITING_AGENT: "Aguardando suporte",
    RESOLVED: "Resolvido",
    CLOSED: "Fechado",
};

const actionIcons: Record<TicketHistoryAction, LucideIcon> = {
    TICKET_CREATED: CirclePlus,
    TICKET_ASSIGNED: UserCheck,
    TICKET_TRANSFERRED: ArrowRightLeft,
    TICKET_RETURNED_TO_QUEUE: RotateCcw,
    STATUS_CHANGED: RefreshCw,
    RESOLUTION_REJECTED: Undo2,
};

function translateValue(value: string | null) {
    if (!value) {
        return null;
    }

    return statusLabels[value] ?? value;
}

export function getHistoryTitle(action: TicketHistoryAction) {
    return actionLabels[action];
}

export function getHistoryDescription(item: TicketHistoryResponse) {
    const oldValue = translateValue(item.oldValue);
    const newValue = translateValue(item.newValue);

    switch (item.action) {
        case "TICKET_CREATED":
            return newValue
                ? `O chamado foi criado com o status ${newValue}.`
                : "O chamado foi criado.";

        case "TICKET_ASSIGNED":
            return newValue
                ? `O chamado foi atribuído a ${newValue}.`
                : "O responsável pelo chamado foi removido.";

        case "TICKET_TRANSFERRED":
            return oldValue && newValue
                ? `O chamado foi transferido de ${oldValue} para ${newValue}.`
                : "O chamado foi transferido para outro agente.";

        case "TICKET_RETURNED_TO_QUEUE":
            return oldValue
                ? `O agente ${oldValue} foi removido e o chamado voltou para a fila.`
                : "O chamado voltou para a fila de disponíveis.";

        case "STATUS_CHANGED":
            if (oldValue && newValue) {
                return `O status mudou de ${oldValue} para ${newValue}.`;
            }

            return newValue
                ? `O status foi alterado para ${newValue}.`
                : "O status do chamado foi alterado.";

        case "RESOLUTION_REJECTED":
            return item.details
                ? `A resolução foi rejeitada: ${item.details}`
                : "A resolução do chamado foi rejeitada.";
    }
}

export function getHistoryIcon(action: TicketHistoryAction) {
    return actionIcons[action];
}
