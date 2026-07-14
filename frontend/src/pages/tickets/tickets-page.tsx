import { useAuthStore } from "@/features/auth/store/auth.store";
import { useMyTickets, useTickets } from "@/features/tickets/hooks/use-tickets";
import { TicketCard } from "@/features/tickets/components/ticket-card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Link } from "react-router-dom";
import { useState } from "react";
import type { TicketPriority, TicketStatus } from "@/features/tickets/types/ticket.types";
import { TicketListSkeleton } from "@/features/tickets/components/ticket-card-skeleton";

export function TicketsPage() {
    const user = useAuthStore((state) => state.user);

    const isClient = user?.role === "CLIENT";

    const [search, setSearch] = useState("");
    const [status, setStatus] = useState<TicketStatus | "">("");
    const [priority, setPriority] = useState<TicketPriority | "">("");
    const [page, setPage] = useState(0);

    const allTicketsQuery = useTickets(
        {
            search: search || undefined,
            status: status || undefined,
            priority: priority || undefined,
            page,
            size: 6,
        },
        {
            enabled: !isClient,
        },
    );

    const myTicketsQuery = useMyTickets({ enabled: isClient, });

    const isPending = isClient ? myTicketsQuery.isPending : allTicketsQuery.isPending;

    const isError = isClient ? myTicketsQuery.isError : allTicketsQuery.isError;

    const tickets = isClient ? myTicketsQuery.data : allTicketsQuery.data?.content;

    const pageData = allTicketsQuery.data;

    const hasActiveFilters = search !== "" || status !== "" || priority !== "";

    function handleClearFilters() {
        setSearch("");
        setStatus("");
        setPriority("");
        setPage(0);
    }

    if (isError) {
        return <p>Erro ao carregar os tickets.</p>;
    }

    if (!isPending && isClient && (!tickets || tickets.length === 0)) {
        return (
            <div>
                <h1>Tickets</h1>
                <p>Você ainda não possui tickets.</p>

                <Button asChild>
                    <Link to="/tickets/new">Novo Ticket</Link>
                </Button>
            </div>
        );
    }

    return (
        <div>
            <div className="flex items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight">Tickets</h1>
                    <p className="text-muted-foreground">
                        {isClient
                            ? "Acompanhe os chamados que você abriu."
                            : "Gerencie os chamados do sistema."
                        }
                    </p>
                </div>

                {isClient && (
                    <Button asChild>
                        <Link to="/tickets/new">Novo Ticket</Link>
                    </Button>
                )}
            </div>

            {!isClient && (
                <div className="my-6 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                    <Input
                        type="search"
                        placeholder="Buscar por título..."
                        value={search}
                        onChange={(event) => {
                            setSearch(event.target.value);
                            setPage(0);
                        }}
                    />

                    <select
                        value={status}
                        onChange={(event) => {
                            setStatus(event.target.value as TicketStatus | "");
                            setPage(0);
                        }}
                        className="h-9 rounded-md border border-input bg-transparent px-3 text-sm"
                    >
                        <option value="">Todos os Status</option>
                        <option value="OPEN">Aberto</option>
                        <option value="IN_PROGRESS">Em andamento</option>
                        <option value="WAITING_CLIENT">Aguardando cliente</option>
                        <option value="RESOLVED">Resolvido</option>
                        <option value="CLOSED">Fechado</option>
                    </select>

                    <select
                        value={priority}
                        onChange={(event) => {
                            setPriority(event.target.value as TicketPriority | "");
                            setPage(0);
                        }}
                        className="h-9 rounded-md border border-input bg-transparent px-3 text-sm"
                    >
                        <option value="">Todas as prioridades</option>
                        <option value="LOW">Baixa</option>
                        <option value="MEDIUM">Média</option>
                        <option value="HIGH">Alta</option>
                    </select>

                    <Button
                        type="button"
                        variant="outline"
                        onClick={handleClearFilters}
                        disabled={!hasActiveFilters}
                    >
                        Limpar filtros
                    </Button>
                </div>
            )}

            {!isClient && pageData && (
                <p className="mb-4 text-sm text-muted-foreground">
                    {pageData.totalElements} chamado
                    {pageData.totalElements === 1 ? "" : "s"} encontrado
                    {pageData.totalElements === 1 ? "" : "s"}.
                </p>
            )}

            {isPending ? (
                <TicketListSkeleton />
            ) : !tickets || tickets.length === 0 ? (
                <p>
                    Nenhum chamado corresponde aos filtros selecionados.
                </p>
            ) : (
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                    {tickets?.map((ticket) => (
                        <TicketCard key={ticket.id} ticket={ticket} />
                    ))}
                </div>
            )}

            {!isClient && pageData && pageData.totalPages > 1 && (
                <div className="mt-6 flex items-center justify-between">
                    <Button
                        variant="outline"
                        disabled={page === 0 || allTicketsQuery.isFetching}
                        onClick={() => setPage((currentPage) => currentPage - 1)}
                    >
                        Anterior
                    </Button>

                    <span className="text-sm text-muted-foreground">
                        Página {pageData.page + 1} de {pageData.totalPages}
                    </span>

                    <Button
                        variant="outline"
                        disabled={page >= pageData.totalPages - 1 || allTicketsQuery.isFetching}
                        onClick={() => setPage((currentPage) => currentPage + 1)}
                    >
                        Próxima
                    </Button>
                </div>
            )}
        </div>
    );
}