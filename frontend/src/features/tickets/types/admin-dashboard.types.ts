import type { Ticket } from "./ticket.types";

export type AgentActiveTickets = {
    agentId: number;
    agentName: string;
    activeTickets: number;
};

export type AdminTicketDashboard = {
    totalActive: number;
    unassigned: number;
    inProgress: number;
    waitingClient: number;
    waitingAgent: number;
    resolved: number;
    activeByAgent: AgentActiveTickets[];
    attentionTickets: Ticket[];
};

export type AdminTicketPerformance = {
    from: string;
    to: string;
    created: number;
    resolved: number;
    closed: number;
    averageResolutionMinutes: number | null;
};
