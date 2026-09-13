import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { useAuthStore } from "@/features/auth/store/auth.store";

import { changePassword, getAccount, updateAccount } from "../api/account.api";

const accountQueryKey = ["account"] as const;

export function useAccount() {
    return useQuery({ queryKey: accountQueryKey, queryFn: getAccount });
}

export function useUpdateAccount() {
    const queryClient = useQueryClient();
    const setToken = useAuthStore((state) => state.setToken);
    const setUser = useAuthStore((state) => state.setUser);

    return useMutation({
        mutationFn: updateAccount,
        onSuccess: ({ token, ...user }) => {
            setToken(token);
            setUser(user);
            queryClient.setQueryData(accountQueryKey, user);
            queryClient.setQueryData(["auth", "me"], user);
        },
    });
}

export function useChangePassword() {
    return useMutation({ mutationFn: changePassword });
}
