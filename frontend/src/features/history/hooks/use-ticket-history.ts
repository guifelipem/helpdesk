import { useQuery } from "@tanstack/react-query";
import { ticketQueryKeys } from "../constants/ticket-history-query-keys";
import { findTicketHistory } from "../api/ticket-history-api";

export function useTicketHistory(ticketId: number) {
        return useQuery({
                queryKey: ticketQueryKeys.history(ticketId),
                queryFn: () => findTicketHistory(ticketId),
                enabled: !!ticketId,
        });
}