import { api } from "@/shared/api/client";
import type { Comment } from "../types/comment-types";
import type { CreateCommentRequest } from "../types/create-comment-request";

export async function findComments(ticketId: number) {
    const { data } = await api.get<Comment[]>(
        `/tickets/${ticketId}/comments`
    );

    return data;
}

export async function createComment(
    ticketId: number,
    request: CreateCommentRequest
) {
    const { data } = await api.post<Comment>(
        `/tickets/${ticketId}/comments`,
        request
    );

    return data;
}