import type { UserSummary } from "@/shared/types/user-summary";

export interface TicketHistoryResponse {
        id: number;
        action: string;
        oldValue: string | null;
        newValue: string | null;
        performedBy: UserSummary;
        createdAt: string;
}