import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { createComment, findComments } from "../api/comment.api";
import { commentQueryKeys } from "../constants/comment-query-keys";
import type { CreateCommentRequest } from "../types/create-comment-request";

export function useComments(ticketId: number) {
    return useQuery({
        queryKey: commentQueryKeys.list(ticketId),
        queryFn: () => findComments(ticketId),
        enabled: !!ticketId,
    });
}

export function useCreateComment(ticketId: number) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: CreateCommentRequest) =>
            createComment(ticketId, request),

        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: commentQueryKeys.list(ticketId),
            });
        },
    });
}