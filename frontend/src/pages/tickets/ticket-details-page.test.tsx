import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";

import { useTicket } from "@/features/tickets/hooks/use-tickets";
import { TicketDetailsPage } from "./ticket-details-page";

vi.mock("@/features/tickets/hooks/use-tickets", () => ({ useTicket: vi.fn() }));
vi.mock("@/features/tickets/components/ticket-details", () => ({
    TicketDetails: () => <div>Detalhes carregados</div>,
}));

function renderPage() {
    return render(
        <MemoryRouter initialEntries={["/tickets/42"]}>
            <Routes><Route path="/tickets/:id" element={<TicketDetailsPage />} /></Routes>
        </MemoryRouter>,
    );
}

function queryResult(overrides: Record<string, unknown>) {
    return {
        data: undefined,
        error: null,
        isPending: false,
        isError: false,
        isFetching: false,
        refetch: vi.fn(),
        ...overrides,
    } as unknown as ReturnType<typeof useTicket>;
}

describe("TicketDetailsPage", () => {
    it("exibe skeleton durante o carregamento", () => {
        vi.mocked(useTicket).mockReturnValue(queryResult({ isPending: true }));
        renderPage();
        expect(screen.getByLabelText("Carregando detalhes do chamado")).toBeInTheDocument();
    });

    it("diferencia chamado inexistente", () => {
        vi.mocked(useTicket).mockReturnValue(queryResult({ isError: true, error: { isAxiosError: true, response: { status: 404 } } }));
        renderPage();
        expect(screen.getByRole("heading", { name: "Chamado não encontrado" })).toBeInTheDocument();
    });

    it("diferencia acesso negado", () => {
        vi.mocked(useTicket).mockReturnValue(queryResult({ isError: true, error: { isAxiosError: true, response: { status: 403 } } }));
        renderPage();
        expect(screen.getByRole("heading", { name: "Acesso negado" })).toBeInTheDocument();
    });

    it("oferece retry em falha temporária", () => {
        vi.mocked(useTicket).mockReturnValue(queryResult({ isError: true, error: new Error("offline") }));
        renderPage();
        expect(screen.getByRole("button", { name: "Tentar novamente" })).toBeInTheDocument();
    });
});
