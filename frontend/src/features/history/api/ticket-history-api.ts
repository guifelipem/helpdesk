import { api } from "@/shared/api/client";

import type { TicketHistoryResponse } from "../types/ticket-history-types";

export async function findTicketHistory(
	ticketId: number
): Promise<TicketHistoryResponse[]> {
	const response = await api.get(
		`/tickets/${ticketId}/history`
	);

	return response.data;
}