import { Badge } from "@/components/ui/badge";

import type { TicketStatus } from "../types/ticket.types";

type Props = { status: TicketStatus; };

const statusMap: Record<
    TicketStatus, { label: string; className: string }
> = {
    OPEN: {
        label: "Aberto",
        className: "border border-sky-300 bg-sky-100 text-sky-900 shadow-sm dark:border-sky-700 dark:bg-sky-950/65 dark:text-sky-200",
    },
    IN_PROGRESS: {
        label: "Em andamento",
        className: "border border-sky-300 bg-sky-100 text-sky-900 dark:border-sky-700 dark:bg-sky-950/65 dark:text-sky-200",
    },
    WAITING_CLIENT: {
        label: "Aguardando cliente",
        className: "border border-amber-300 bg-amber-100 text-amber-900 dark:border-amber-700 dark:bg-amber-950/65 dark:text-amber-200",
    },
    WAITING_AGENT: {
        label: "Aguardando suporte",
        className: "border border-violet-300 bg-violet-100 text-violet-900 dark:border-violet-700 dark:bg-violet-950/65 dark:text-violet-200",
    },
    RESOLVED: {
        label: "Resolvido",
        className: "border border-emerald-300 bg-emerald-100 text-emerald-900 dark:border-emerald-700 dark:bg-emerald-950/65 dark:text-emerald-200",
    },
    CLOSED: {
        label: "Fechado",
        className: "border border-slate-300 bg-slate-100 text-slate-800 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-200",
    },
};

export function TicketStatusBadge({ status }: Props) {
    const config = statusMap[status];

    return (
        <Badge className={config.className}>
            {config.label}
        </Badge>
    );
}
