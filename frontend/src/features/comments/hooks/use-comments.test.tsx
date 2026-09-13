import type { ReactNode } from "react";
import { act, renderHook } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { createComment } from "../api/comment.api";
import { commentQueryKeys } from "../constants/comment-query-keys";
import { ticketQueryKeys } from "@/features/tickets/constants/ticket-query-keys";
import { useCreateComment } from "./use-comments";

vi.mock("../api/comment.api", () => ({
    createComment: vi.fn(),
    findComments: vi.fn(),
}));

describe("useCreateComment", () => {
    beforeEach(() => vi.clearAllMocks());

    it("atualiza comentários e detalhes do chamado após comentar", async () => {
        const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
        const invalidateQueries = vi.spyOn(queryClient, "invalidateQueries");
        vi.mocked(createComment).mockResolvedValue({
            id: 1,
            message: "Resposta",
            isInternal: false,
            author: { id: 7, name: "Agente", role: "AGENT" },
            createdAt: "2026-09-13T12:00:00",
        });
        const wrapper = ({ children }: { children: ReactNode }) => (
            <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
        );
        const { result } = renderHook(() => useCreateComment(42), { wrapper });

        await act(() => result.current.mutateAsync({ message: "Resposta", isInternal: false }));

        expect(invalidateQueries).toHaveBeenCalledWith({ queryKey: commentQueryKeys.list(42) });
        expect(invalidateQueries).toHaveBeenCalledWith({ queryKey: ticketQueryKeys.detail(42) });
    });
});
