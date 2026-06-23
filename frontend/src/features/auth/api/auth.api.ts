import { api } from "@/shared/api/client";

import type { LoginRequest, LoginResponse } from "../types/auth.types";

export async function login(request: LoginRequest) {
    const response = await api.post<LoginResponse>("/auth/login", request);

    return response.data;
}