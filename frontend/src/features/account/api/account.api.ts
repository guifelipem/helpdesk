import type { User } from "@/features/auth/types/user.types";
import { api } from "@/shared/api/client";

import type { ChangePasswordRequest, UpdateAccountRequest, UpdateAccountResponse } from "../types/account.types";

export async function getAccount() {
    const response = await api.get<User>("/account");
    return response.data;
}

export async function updateAccount(request: UpdateAccountRequest) {
    const response = await api.patch<UpdateAccountResponse>("/account", request);
    return response.data;
}

export async function changePassword(request: ChangePasswordRequest) {
    await api.patch("/account/password", request);
}
