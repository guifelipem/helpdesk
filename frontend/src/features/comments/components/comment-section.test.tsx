import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { useAuthStore } from "@/features/auth/store/auth.store";
import { CommentSection } from "./comment-section";
import { useComments, useCreateComment } from "../hooks/use-comments";

vi.mock("../hooks/use-comments", () => ({
    useComments: vi.fn(),
    useCreateComment: vi.fn(),
}));

describe("CommentSection", () => {
    beforeEach(() => {
        useAuthStore.setState({
            user: { id: 7, name: "Agente", email: "agente@example.com", role: "AGENT" },
            token: "token",
            isAuthenticated: true,
        });
        vi.mocked(useComments).mockReturnValue({
            data: [],
            isLoading: false,
            isError: false,
            error: null,
            refetch: vi.fn(),
            isFetching: false,
        } as unknown as ReturnType<typeof useComments>);
        vi.mocked(useCreateComment).mockReturnValue({
            mutate: vi.fn(),
            isPending: false,
            error: null,
        } as unknown as ReturnType<typeof useCreateComment>);
    });

    it("não oferece comentário antes de o agente assumir o chamado", () => {
        render(<CommentSection ticketId={42} ticketStatus="OPEN" assignedToId={null} />);

        expect(screen.getByText("Assuma este chamado antes de adicionar comentários.")).toBeInTheDocument();
        expect(screen.queryByLabelText("Adicionar comentário")).not.toBeInTheDocument();
    });

    it("oferece comentário ao agente responsável", () => {
        render(<CommentSection ticketId={42} ticketStatus="IN_PROGRESS" assignedToId={7} />);

        expect(screen.getByLabelText("Adicionar comentário")).toHaveAttribute("maxlength", "5000");
        expect(screen.getByLabelText("Comentário interno")).toBeInTheDocument();
    });
});
