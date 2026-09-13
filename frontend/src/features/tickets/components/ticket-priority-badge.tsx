import { Badge } from "@/components/ui/badge";

import type { TicketPriority } from "../types/ticket.types";

type Props = { priority: TicketPriority; };

const priorityMap: Record<
    TicketPriority,
    { label: string; className: string }
> = {
    LOW: {
        label: "Baixa",
        className: "border border-emerald-300 bg-emerald-50 text-emerald-900 dark:border-emerald-700 dark:bg-emerald-950/65 dark:text-emerald-200",
    },
    MEDIUM: {
        label: "Média",
        className: "border border-amber-300 bg-amber-50 text-amber-900 dark:border-amber-700 dark:bg-amber-950/65 dark:text-amber-200",
    },
    HIGH: {
        label: "Alta",
        className: "border border-rose-300 bg-rose-50 text-rose-900 dark:border-rose-700 dark:bg-rose-950/65 dark:text-rose-200",
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
