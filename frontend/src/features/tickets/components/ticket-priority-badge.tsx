import { Badge } from "@/components/ui/badge";

import type { TicketPriority } from "../types/ticket.types";

type Props = { priority: TicketPriority; };

const priorityMap: Record<
    TicketPriority,
    { label: string; className: string }
> = {
    LOW: {
        label: "Baixa",
        className: "border border-emerald-200 bg-emerald-50 text-emerald-700",
    },
    MEDIUM: {
        label: "Média",
        className: "border border-amber-200 bg-amber-50 text-amber-700",
    },
    HIGH: {
        label: "Alta",
        className: "border border-rose-200 bg-rose-50 text-rose-700",
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
