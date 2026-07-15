import type { TicketStatus } from "./ticket.types";

export type UpdateTicketStatusRequest = {
    status: TicketStatus;
};