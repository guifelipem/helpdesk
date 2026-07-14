import { api } from "@/shared/api/client";

import type { Ticket } from "../types/ticket.types";
import type { CreateTicketRequest } from "../types/create-ticket-request";
import type { UpdateTicketStatusRequest } from "../types/update-ticket-status-request";
import type { FindAllTicketsParams } from "../types/find-all-tickets-params";
import type { PageResponse } from "@/shared/types/page-response";

export async function createTicket(data: CreateTicketRequest) {
        const response = await api.post<Ticket>("/tickets", data);
        return response.data;
}

export async function findMyTickets() {
        const response = await api.get<Ticket[]>("/tickets/me");
        return response.data;
}

export async function findTicketById(id: number) {
        const response = await api.get<Ticket>(`/tickets/${id}`);
        return response.data;
}

export async function updateTicketStatus({
        id,
        data,
}: {
        id: number;
        data: UpdateTicketStatusRequest;
}): Promise<Ticket> {
        const response = await api.patch<Ticket>(`/tickets/${id}/status`, data);
        return response.data;
}

export async function closeTicket(id: number) {
        const response = await api.patch<Ticket>(`/tickets/${id}/close`);
        return response.data;
}

export async function assignTicketToMe(id: number) {
        const response = await api.patch<Ticket>(`/tickets/${id}/assign/me`);
        return response.data;
}

export async function findAllTickets(params?: FindAllTicketsParams) {
        const response = await api.get<PageResponse<Ticket>>("/tickets", {
                params,
        });
        return response.data;
}

