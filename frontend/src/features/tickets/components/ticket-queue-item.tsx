import { ArrowUpRight, CalendarDays, Clock3, UserRound } from "lucide-react";
import { Link } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { formatDate, formatRelativeDate } from "@/shared/utils/format-date";
import { TicketPriorityBadge } from "./ticket-priority-badge";
import { TicketStatusBadge } from "./ticket-status-badge";
import type { TicketQueue } from "../types/ticket-queue.types";
import type { Ticket } from "../types/ticket.types";

type TicketQueueItemProps = {
    ticket: Ticket;
    queue: TicketQueue;
    onAssign?: (ticketId: number) => void;
    isAssigning?: boolean;
    isAssignDisabled?: boolean;
    assignError?: string;
};

const queueMessage: Record<TicketQueue, string> = {
    AVAILABLE: "Sem responsável",
    MY_TICKETS: "Responsável: você",
    WAITING_CLIENT: "Aguardando uma resposta do cliente",
    WAITING_AGENT: "Aguardando sua resposta",
    RESOLVED: "Aguardando confirmação do cliente",
};

export function TicketQueueItem({
    ticket,
    queue,
    onAssign,
    isAssigning = false,
    isAssignDisabled = false,
    assignError,
}: TicketQueueItemProps) {
    const isAvailable = queue === "AVAILABLE";
    const needsAttention = queue === "WAITING_AGENT";
    const relevantDate = isAvailable ? ticket.createdAt : ticket.updatedAt;

    return (
        <article
            className={cn(
                "rounded-2xl border bg-card/90 p-4 shadow-[0_16px_42px_-34px_#4794b8] transition hover:border-primary/40 hover:shadow-[0_20px_46px_-30px_#4794b870] sm:p-5",
                needsAttention && "border-primary/55 bg-primary/5 shadow-[0_18px_48px_-30px_#4794b8]",
            )}
        >
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                <div className="min-w-0 flex-1">
                    <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between lg:justify-start">
                        <div className="min-w-0">
                            <p className="text-xs font-bold tracking-[0.12em] text-muted-foreground">CHAMADO #{ticket.id}</p>
                            <h2 className="mt-1 text-base font-bold leading-6 text-foreground sm:truncate sm:text-lg">{ticket.title}</h2>
                        </div>
                        <div className="flex shrink-0 flex-wrap gap-1.5 lg:ml-4">
                            <TicketPriorityBadge priority={ticket.priority} />
                            <TicketStatusBadge status={ticket.status} />
                        </div>
                    </div>

                    <div className="mt-3 flex flex-col gap-2 text-xs text-muted-foreground sm:flex-row sm:flex-wrap sm:items-center sm:gap-x-5">
                        <span className="flex items-center gap-1.5">
                            <UserRound className="size-3.5" /> Cliente: {ticket.createdBy.name}
                        </span>
                        <span className={cn("font-semibold", needsAttention ? "text-primary-strong" : "text-foreground/75")}>
                            {queueMessage[queue]}
                        </span>
                        <span className="flex items-center gap-1.5" title={formatDate(relevantDate)}>
                            {isAvailable ? <CalendarDays className="size-3.5" /> : <Clock3 className="size-3.5" />}
                            {isAvailable ? "Aberto" : "Atualizado"} {formatRelativeDate(relevantDate)}
                        </span>
                    </div>

                    {isAvailable && (
                        <p className="mt-2 text-xs text-muted-foreground">Última atualização: {formatDate(ticket.updatedAt)}</p>
                    )}

                    {assignError && <p role="alert" className="mt-3 text-sm font-medium text-destructive">{assignError}</p>}
                </div>

                <div className="flex shrink-0 flex-wrap gap-2 border-t border-border/70 pt-4 lg:border-l lg:border-t-0 lg:pl-5 lg:pt-0">
                    <Button asChild variant="outline">
                        <Link to={`/tickets/${ticket.id}`}>Ver chamado <ArrowUpRight /></Link>
                    </Button>
                    {isAvailable && onAssign && (
                        <Button
                            type="button"
                            onClick={() => onAssign(ticket.id)}
                            disabled={isAssignDisabled}
                        >
                            {isAssigning ? "Assumindo..." : "Assumir"}
                        </Button>
                    )}
                </div>
            </div>
        </article>
    );
}
