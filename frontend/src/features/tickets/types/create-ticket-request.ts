import type { TicketPriority } from "./ticket.types";

export type CreateTicketRequest = {
    title: string;
    description: string;
    priority: TicketPriority;
};