import { Badge } from "@/components/ui/badge";

import type { TicketStatus } from "../types/ticket.types";

type Props = { status: TicketStatus; };

const statusMap: Record<
    TicketStatus, { label: string; className: string }
> = {
    OPEN: {
        label: "Aberto",
        className: "bg-blue-100 text-blue-800",
    },
    IN_PROGRESS: {
        label: "Em andamento",
        className: "bg-yellow-100 text-yellow-800",
    },
    WAITING_CLIENT: {
        label: "Aguardando cliente",
        className: "bg-orange-100 text-orange-800",
    },
    RESOLVED: {
        label: "Resolvido",
        className: "bg-green-100 text-green-800",
    },
    CLOSED: {
        label: "Fechado",
        className: "bg-gray-100 text-gray-800",
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