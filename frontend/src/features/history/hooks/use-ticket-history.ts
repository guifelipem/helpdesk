import { useQuery } from "@tanstack/react-query";
import { ticketHistoryQueryKeys } from "../constants/ticket-history-query-keys";
import { findTicketHistory } from "../api/ticket-history-api";

export function useTicketHistory(ticketId: number) {
        return useQuery({
                queryKey: ticketHistoryQueryKeys.history(ticketId),
                queryFn: () => findTicketHistory(ticketId),
                enabled: !!ticketId,
        });
}