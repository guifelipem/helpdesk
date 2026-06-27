export type TicketStatus = 
    | "OPEN"
    | "IN_PROGRESS"
    | "WAITING_CLIENT"
    | "RESOLVED"
    | "CLOSED";

export type TicketPriority =
    | "LOW"
    | "MEDIUM"
    | "HIGH"
    | "CRITICAL";

export type Ticket = {
    id: number;
    title: string;
    description: string;
    status: TicketStatus;
    priority: TicketPriority;
    createdAt: string;
    updatedAt: string;
};