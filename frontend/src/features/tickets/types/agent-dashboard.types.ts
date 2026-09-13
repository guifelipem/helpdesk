import type { Ticket } from "./ticket.types";

export type AgentTicketDashboard = {
    active: number;
    waitingClient: number;
    waitingAgent: number;
    resolvedAwaitingConfirmation: number;
    available: number;
    priorityNow: Ticket[];
};

export type AgentTicketPerformance = {
    from: string;
    to: string;
    resolved: number;
    averageResolutionMinutes: number | null;
};
