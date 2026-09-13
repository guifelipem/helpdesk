import { ArrowRight, Clock3, UserRound } from "lucide-react";
import { Link } from "react-router-dom";

import { TicketPriorityBadge } from "./ticket-priority-badge";
import { TicketStatusBadge } from "./ticket-status-badge";
import type { Ticket } from "../types/ticket.types";
import { formatDate } from "@/shared/utils/format-date";

type TicketOverviewItemProps = {
    ticket: Ticket;
    needsAttention?: boolean;
};

function getActionLabel(ticket: Ticket) {
    if (ticket.status === "WAITING_CLIENT") {
        return "Responder chamado";
    }

    if (ticket.status === "RESOLVED") {
        return "Revisar solução";
    }

    return "Ver chamado";
}

export function TicketOverviewItem({ ticket, needsAttention = false }: TicketOverviewItemProps) {
    return (
        <article className={`group rounded-2xl border bg-card/90 p-4 shadow-[0_16px_42px_-34px_#256d85] transition hover:-translate-y-0.5 hover:shadow-[0_20px_46px_-30px_#256d8570] ${needsAttention ? "border-amber-300/80 dark:border-amber-700/70" : "border-border"}`}>
            <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                    <p className="text-xs font-bold tracking-[0.12em] text-muted-foreground">CHAMADO #{ticket.id}</p>
                    <h3 className="mt-1 truncate text-base font-bold text-foreground">{ticket.title}</h3>
                </div>
                <div className="flex shrink-0 flex-wrap justify-end gap-1.5">
                    <TicketStatusBadge status={ticket.status} />
                    <TicketPriorityBadge priority={ticket.priority} />
                </div>
            </div>

            <div className="mt-4 flex flex-col gap-2 text-xs text-muted-foreground sm:flex-row sm:items-center sm:gap-5">
                <span className="flex items-center gap-1.5">
                    <Clock3 className="size-3.5" />
                    Atualizado em {formatDate(ticket.updatedAt)}
                </span>
                <span className="flex items-center gap-1.5">
                    <UserRound className="size-3.5" />
                    {ticket.assignedTo?.name ?? "Aguardando responsável"}
                </span>
            </div>

            <div className="mt-4 border-t border-border/70 pt-3 text-right">
                <Link
                    to={`/tickets/${ticket.id}`}
                    className="inline-flex items-center gap-1.5 text-sm font-semibold text-primary-strong transition group-hover:gap-2.5"
                >
                    {getActionLabel(ticket)} <ArrowRight className="size-4" />
                </Link>
            </div>
        </article>
    );
}
