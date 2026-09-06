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
