import type { TicketStatus, TicketPriority } from "./ticket.types";

export type FindAllTicketsParams = {
    status?: TicketStatus;
    priority?: TicketPriority;
    agentId?: number;
    active?: boolean;
    unassigned?: boolean;
    search?: string;
    page?: number;
    size?: number;
    sort?: string;
};

export type FindMyTicketsParams = Omit<FindAllTicketsParams, "status"> & {
    status?: TicketStatus | TicketStatus[];
};
