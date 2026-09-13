import { api } from "@/shared/api/client";

import type { AdminUser, BlockUserParams, FindUsersParams, UsersPageResponse } from "../types/admin-user.types";

export async function findUsers(params?: FindUsersParams) {
    const response = await api.get<UsersPageResponse>("/users", { params });
    return response.data;
}

export async function findAllActiveAgents() {
    const firstPage = await findUsers({ role: "AGENT", page: 0, size: 100, sort: "name,asc" });
    const remainingPages = Array.from(
        { length: Math.max(0, firstPage.totalPages - 1) },
        (_, index) => findUsers({ role: "AGENT", page: index + 1, size: 100, sort: "name,asc" }),
    );
    const pages = await Promise.all(remainingPages);

    return [firstPage, ...pages]
        .flatMap((page) => page.content)
        .filter((user) => user.active);
}

export async function updateUserRole({ userId, role }: { userId: number; role: "CLIENT" | "AGENT" }) {
    const response = await api.patch<AdminUser>(`/users/${userId}/role`, { role });
    return response.data;
}

export async function blockUser({ userId, action, targetAgentId }: BlockUserParams) {
    const body = action ? { action, targetAgentId } : undefined;
    const response = await api.patch<AdminUser>(`/users/${userId}/block`, body);
    return response.data;
}

export async function unblockUser(userId: number) {
    const response = await api.patch<AdminUser>(`/users/${userId}/unblock`);
    return response.data;
}
