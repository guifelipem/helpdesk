import type { UserSummary } from "@/shared/types/user-summary";

export type TicketHistoryAction = 
        | "TICKET_CREATED" 
        | "TICKET_ASSIGNED" 
        | "TICKET_TRANSFERRED"
        | "TICKET_RETURNED_TO_QUEUE"
        | "STATUS_CHANGED"
        | "RESOLUTION_REJECTED";

export interface TicketHistoryResponse {
        id: number;
        action: TicketHistoryAction;
        oldValue: string | null;
        newValue: string | null;
        details: string | null;
        performedBy: UserSummary;
        createdAt: string;
}
