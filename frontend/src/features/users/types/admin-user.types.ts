import type { UserRole } from "@/features/auth/types/user.types";

export type AdminUser = {
    id: number;
    name: string;
    email: string;
    role: UserRole;
    active: boolean;
};

export type UsersPageResponse = {
    content: AdminUser[];
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
};

export type FindUsersParams = {
    role?: UserRole;
    search?: string;
    page?: number;
    size?: number;
    sort?: string;
};

export type AgentBlockAction = "TRANSFER" | "RETURN_TO_QUEUE";

export type BlockUserParams = {
    userId: number;
    action?: AgentBlockAction;
    targetAgentId?: number;
};

export type ActiveTicketsConflictResponse = {
    timestamp: string;
    status: number;
    message: string;
    activeTicketCount: number;
};
