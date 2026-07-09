import type { UserSummary } from "@/shared/types/user-summary";

export type Comment = {
    id: number;
    message: string;
    isInternal: boolean;
    author: UserSummary;
    createdAt: string;
}