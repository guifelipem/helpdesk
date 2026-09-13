import { api } from "@/shared/api/client";

import type { User } from "@/features/auth/types/user.types";
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from "../types/auth.types";

export async function login(request: LoginRequest) {
    const response = await api.post<LoginResponse>("/auth/login", request);

    return response.data;
}

export async function getMe() {
    const response = await api.get<User>("/auth/me")

    return response.data;
}

export async function register(request: RegisterRequest) {
    const response = await api.post<RegisterResponse>("/auth/register", request);

    return response.data;
}
