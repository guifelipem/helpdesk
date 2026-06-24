import { api } from "@/shared/api/client";

import type { User } from "@/features/auth/types/user.types";
import type { LoginRequest, LoginResponse } from "../types/auth.types";

export async function login(request: LoginRequest) {
    const response = await api.post<LoginResponse>("/auth/login", request);

    return response.data;
}

export async function getMe(token?: string) {
    const response = await api.get<User>("/auth/me", {
        headers: token ? {
            Authorization: `Bearer %{token}`,
        } : undefined,
    });

    return response.data;
}