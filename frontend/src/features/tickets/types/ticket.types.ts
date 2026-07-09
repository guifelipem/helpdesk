import type { UserSummary } from "@/shared/types/user-summary";

export type TicketStatus = 
    | "OPEN"
    | "IN_PROGRESS"
    | "WAITING_CLIENT"
    | "RESOLVED"
    | "CLOSED";

export type TicketPriority =
    | "LOW"
    | "MEDIUM"
    | "HIGH";

export type Ticket = {
    id: number;
    title: string;
    description: string;
    status: TicketStatus;
    priority: TicketPriority;
    createdBy: UserSummary;
    assignedTo: UserSummary | null;
    createdAt: string;
    updatedAt: string;
};