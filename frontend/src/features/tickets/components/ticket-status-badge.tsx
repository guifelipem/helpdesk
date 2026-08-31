import { Badge } from "@/components/ui/badge";

import type { TicketStatus } from "../types/ticket.types";

type Props = { status: TicketStatus; };

const statusMap: Record<
    TicketStatus, { label: string; className: string }
> = {
    OPEN: {
        label: "Aberto",
        className: "border border-[#b9caff] bg-[#e4ebff] text-[#293f88] shadow-sm",
    },
    IN_PROGRESS: {
        label: "Em andamento",
        className: "border border-violet-200 bg-violet-100 text-violet-700",
    },
    WAITING_CLIENT: {
        label: "Aguardando cliente",
        className: "border border-amber-200 bg-amber-100 text-amber-800",
    },
    RESOLVED: {
        label: "Resolvido",
        className: "border border-emerald-200 bg-emerald-100 text-emerald-700",
    },
    CLOSED: {
        label: "Fechado",
        className: "border border-slate-200 bg-slate-100 text-slate-600",
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
