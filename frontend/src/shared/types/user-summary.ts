import type { UserRole } from "@/features/auth/types/user.types";

export type UserSummary = {
    id: number;
    name: string;
    role: UserRole;
};