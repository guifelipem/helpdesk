import type { TicketStatus, TicketPriority } from "./ticket.types";

export type FindAllTicketsParams = {
    status?: TicketStatus;
    priority?: TicketPriority;
    search?: string;
    page?: number;
    size?: number;
    sort?: number;
};