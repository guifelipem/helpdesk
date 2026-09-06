import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { blockUser, findAllActiveAgents, findUsers, unblockUser, updateUserRole } from "../api/user.api";
import { userQueryKeys } from "../constants/user-query-keys";
import type { FindUsersParams } from "../types/admin-user.types";

export function useUsers(params?: FindUsersParams, enabled = true) {
    return useQuery({
        queryKey: userQueryKeys.list(params),
        queryFn: () => findUsers(params),
        placeholderData: keepPreviousData,
        enabled,
    });
}

export function useActiveAgents(enabled = true) {
    return useQuery({
        queryKey: userQueryKeys.activeAgents(),
        queryFn: findAllActiveAgents,
        enabled,
    });
}

function useInvalidateUsers() {
    const queryClient = useQueryClient();
    return () => queryClient.invalidateQueries({ queryKey: userQueryKeys.all });
}

export function useUpdateUserRole() {
    const invalidateUsers = useInvalidateUsers();
    return useMutation({ mutationFn: updateUserRole, onSuccess: invalidateUsers });
}

export function useBlockUser() {
    const invalidateUsers = useInvalidateUsers();
    return useMutation({ mutationFn: blockUser, onSuccess: invalidateUsers });
}

export function useUnblockUser() {
    const invalidateUsers = useInvalidateUsers();
    return useMutation({ mutationFn: unblockUser, onSuccess: invalidateUsers });
}
