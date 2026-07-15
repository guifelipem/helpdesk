import { Badge } from "@/components/ui/badge";

import type { TicketPriority } from "../types/ticket.types";

type Props = { priority: TicketPriority; };

const priorityMap: Record<
    TicketPriority,
    { label: string; className: string }
> = {
    LOW: {
        label: "Baixa",
        className: "bg-green-100 text-green-800",
    },
    MEDIUM: {
        label: "Média",
        className: "bg-yellow-100 text-yellow-800",
    },
    HIGH: {
        label: "Alta",
        className: "bg-red-100 text-red-800",
    },
};

export function TicketPriorityBadge({ priority }: Props) {
    const config = priorityMap[priority];

    return (
        <Badge className={config.className}>
            {config.label}
        </Badge>
    );
}