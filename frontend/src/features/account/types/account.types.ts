import type { User } from "@/features/auth/types/user.types";

export type UpdateAccountRequest = {
    name: string;
    email: string;
};

export type UpdateAccountResponse = User & {
    token: string;
};

export type ChangePasswordRequest = {
    currentPassword: string;
    newPassword: string;
};
