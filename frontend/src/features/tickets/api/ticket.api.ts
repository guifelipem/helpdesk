import { api } from "@/shared/api/client";

import type { Ticket } from "../types/ticket.types";
import type { CreateTicketRequest } from "../types/create-ticket-request";
import type { UpdateTicketStatusRequest } from "../types/update-ticket-status-request";
import type { RejectResolutionRequest } from "../types/reject-resolution-request";
import type { FindAllTicketsParams, FindMyTicketsParams } from "../types/find-all-tickets-params";
import type { PageResponse } from "@/shared/types/page-response";
import type { TicketQueue, TicketQueueParams, TicketQueueSummary } from "../types/ticket-queue.types";
import type { AdminTicketDashboard, AdminTicketPerformance } from "../types/admin-dashboard.types";
import type { AgentTicketDashboard, AgentTicketPerformance } from "../types/agent-dashboard.types";

export async function createTicket(data: CreateTicketRequest) {
        const response = await api.post<Ticket>("/tickets", data);
        return response.data;
}

export async function findMyTickets(params?: FindMyTicketsParams) {
        const normalizedParams = params
                ? {
                        ...params,
                        status: Array.isArray(params.status)
                                ? params.status.join(",")
                                : params.status,
                }
                : undefined;

        const response = await api.get<PageResponse<Ticket>>("/tickets/me", { params: normalizedParams });
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

export async function rejectTicketResolution({
        id,
        data,
}: {
        id: number;
        data: RejectResolutionRequest;
}): Promise<Ticket> {
        const response = await api.post<Ticket>(`/tickets/${id}/reject-resolution`, data);
        return response.data;
}

export async function sendTicketToAgent(id: number) {
        const response = await api.post<Ticket>(`/tickets/${id}/send-to-agent`);
        return response.data;
}

export async function assignTicketToMe(id: number) {
        const response = await api.patch<Ticket>(`/tickets/${id}/assign/me`);
        return response.data;
}

export async function returnTicketToQueue(id: number) {
        const response = await api.patch<Ticket>(`/tickets/${id}/return-to-queue`);
        return response.data;
}

export async function transferTicket({ id, agentId }: { id: number; agentId: number }) {
        const response = await api.patch<Ticket>(`/tickets/${id}/transfer`, { agentId });
        return response.data;
}

export async function findAllTickets(params?: FindAllTicketsParams) {
        const response = await api.get<PageResponse<Ticket>>("/tickets", {
                params,
        });
        return response.data;
}

export async function findTicketQueue(queue: TicketQueue, params?: TicketQueueParams) {
        const response = await api.get<PageResponse<Ticket>>(`/tickets/queues/${queue}`, { params });
        return response.data;
}

export async function findTicketQueueSummary() {
        const response = await api.get<TicketQueueSummary>("/tickets/queues/summary");
        return response.data;
}

export async function findAgentTicketDashboard() {
        const response = await api.get<AgentTicketDashboard>("/tickets/agent/dashboard");
        return response.data;
}

export async function findAgentTicketPerformance(from: string, to: string) {
        const response = await api.get<AgentTicketPerformance>("/tickets/agent/dashboard/performance", {
                params: { from, to },
        });
        return response.data;
}

export async function findAdminTicketDashboard() {
        const response = await api.get<AdminTicketDashboard>("/tickets/admin/dashboard");
        return response.data;
}

export async function findAdminTicketPerformance(from: string, to: string) {
        const response = await api.get<AdminTicketPerformance>("/tickets/admin/dashboard/performance", {
                params: { from, to },
        });
        return response.data;
}

