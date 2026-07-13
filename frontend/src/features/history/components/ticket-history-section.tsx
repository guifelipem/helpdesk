import { useTicketHistory } from "../hooks/use-ticket-history";
import { formatDate } from "@/shared/utils/format-date";

type TicketHistorySectionProps = {
    ticketId: number;
};

export function TicketHistorySection({ ticketId }: TicketHistorySectionProps) {
    const { data: history = [] } = useTicketHistory(ticketId);

    return (
        <div className="space-y-2">
            <h2 className="font-semibold">
                Histórico
            </h2>

            {history.map(item => (
                <div
                    key={item.id}
                    className="rounded border p-3"
                >
                    <p className="font-medium">
                        {item.action}
                    </p>

                    <p className="text-sm text-muted-foreground">
                        {item.performedBy.name}
                    </p>

                    <p className="text-sm">
                        {item.oldValue} → {item.newValue}
                    </p>

                    <p className="text-xs text-muted-foreground">
                        {formatDate(item.createdAt)}
                    </p>
                </div>
            ))}
        </div>
    );
}