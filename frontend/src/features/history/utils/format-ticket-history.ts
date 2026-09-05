import type { TicketHistoryAction, TicketHistoryResponse } from "../types/ticket-history-types";
import { CirclePlus, RefreshCw, Undo2, UserCheck, type LucideIcon } from "lucide-react";

const actionLabels: Record<TicketHistoryAction, string> = {
    TICKET_CREATED: "Chamado criado",
    TICKET_ASSIGNED: "Chamado atribuído",
    STATUS_CHANGED: "Status alterado",
    RESOLUTION_REJECTED: "Resolução rejeitada",
};

const statusLabels: Record<string, string> = {
    OPEN: "Aberto",
    IN_PROGRESS: "Em andamento",
    WAITING_CLIENT: "Aguardando cliente",
    RESOLVED: "Resolvido",
    CLOSED: "Fechado",
};

const actionIcons: Record<TicketHistoryAction, LucideIcon> = {
    TICKET_CREATED: CirclePlus,
    TICKET_ASSIGNED: UserCheck,
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
