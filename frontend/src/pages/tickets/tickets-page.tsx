import { useAuthStore } from "@/features/auth/store/auth.store";
import { useMyTickets, useTickets } from "@/features/tickets/hooks/use-tickets";
import { TicketCard } from "@/features/tickets/components/ticket-card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Link } from "react-router-dom";
import { useState } from "react";
import type { TicketPriority, TicketStatus } from "@/features/tickets/types/ticket.types";
import { TicketListSkeleton } from "@/features/tickets/components/ticket-card-skeleton";
import { ErrorState } from "@/shared/components/error-state";
import { Filter, Plus, Search, Sparkles, Ticket as TicketIcon } from "lucide-react";

export function TicketsPage() {
    const user = useAuthStore((state) => state.user);

    const isClient = user?.role === "CLIENT";

    const [search, setSearch] = useState("");
    const [status, setStatus] = useState<TicketStatus | "">("");
    const [priority, setPriority] = useState<TicketPriority | "">("");
    const [page, setPage] = useState(0);
    const [isRetrying, setIsRetrying] = useState(false);

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

    const refetch = isClient ? myTicketsQuery.refetch : allTicketsQuery.refetch;

    const pageData = allTicketsQuery.data;

    const hasActiveFilters = search !== "" || status !== "" || priority !== "";

    function handleClearFilters() {
        setSearch("");
        setStatus("");
        setPriority("");
        setPage(0);
    }

    async function handleRetry() {
        setIsRetrying(true);

        await refetch();

        setIsRetrying(false);
    }

    if (isError || isRetrying) {
        return (
            <ErrorState
                title="Não foi possível carregar os chamados"
                description="Tivemos um problema ao buscar os chamados. Verifique se o servidor está disponível e tente novamente."
                onRetry={handleRetry}
                isRetrying={isRetrying}
            />
        );
    }

    if (!isPending && isClient && (!tickets || tickets.length === 0)) {
        return (
            <div className="rounded-3xl border border-dashed border-[#5c65c0]/25 bg-white/70 px-6 py-16 text-center shadow-sm">
                <div className="mx-auto mb-4 flex size-14 items-center justify-center rounded-2xl bg-[#ececff] text-[#5c65c0]"><TicketIcon /></div>
                <h1 className="text-2xl font-bold tracking-tight">Nenhum chamado por aqui</h1>
                <p className="mb-6 mt-2 text-muted-foreground">Você ainda não possui chamados.</p>

                <Button asChild>
                    <Link to="/tickets/new"><Plus /> Novo chamado</Link>
                </Button>
            </div>
        );
    }

    return (
        <div className="space-y-7">
            <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#301c41] via-[#413b6b] to-[#5c65c0] px-6 py-8 text-white shadow-[0_25px_60px_-30px_#301c41] sm:px-8">
                <div className="absolute -right-12 -top-20 size-64 rounded-full border-[32px] border-white/5" />
                <div className="relative flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <div className="mb-3 flex items-center gap-2 text-xs font-bold uppercase tracking-[0.18em] text-[#aebfff]"><Sparkles className="size-3.5" /> Central de atendimento</div>
                    <h1 className="text-3xl font-bold tracking-tight">Seus chamados</h1>
                    <p className="mt-2 max-w-xl text-sm text-white/65">
                        {isClient
                            ? "Acompanhe os chamados que você abriu."
                            : "Gerencie os chamados do sistema."
                        }
                    </p>
                </div>

                {isClient && (
                    <Button asChild className="bg-[#6f95ff] text-white shadow-[#1c0b2b]/40 hover:bg-[#83a4ff]">
                        <Link to="/tickets/new"><Plus /> Novo chamado</Link>
                    </Button>
                )}
                </div>
            </div>

            {!isClient && (
                <div className="rounded-2xl border border-white bg-white/80 p-4 shadow-[0_15px_40px_-30px_#301c41] backdrop-blur-sm">
                  <div className="mb-3 flex items-center gap-2 text-sm font-semibold text-[#413b6b]"><Filter className="size-4" /> Filtros</div>
                  <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
                    <div className="relative">
                    <Search className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                    <Input className="pl-10"
                        type="search"
                        placeholder="Buscar por título..."
                        value={search}
                        onChange={(event) => {
                            setSearch(event.target.value);
                            setPage(0);
                        }}
                    /></div>

                    <select
                        value={status}
                        onChange={(event) => {
                            setStatus(event.target.value as TicketStatus | "");
                            setPage(0);
                        }}
                        className="h-11 rounded-xl border border-input bg-white/80 px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/20"
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
                        className="h-11 rounded-xl border border-input bg-white/80 px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/20"
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
                </div>
            )}

            {!isClient && pageData && (
                <p className="text-sm font-medium text-muted-foreground">
                    {pageData.totalElements} chamado
                    {pageData.totalElements === 1 ? "" : "s"} encontrado
                    {pageData.totalElements === 1 ? "" : "s"}.
                </p>
            )}

            {isPending ? (
                <TicketListSkeleton />
            ) : !tickets || tickets.length === 0 ? (
                <div className="rounded-2xl border border-dashed border-[#5c65c0]/25 bg-white/70 py-12 text-center text-muted-foreground">Nenhum chamado corresponde aos filtros selecionados.</div>
            ) : (
                <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
                    {tickets?.map((ticket) => (
                        <TicketCard key={ticket.id} ticket={ticket} />
                    ))}
                </div>
            )}

            {!isClient && pageData && pageData.totalPages > 1 && (
                <div className="flex items-center justify-between rounded-2xl border border-white bg-white/70 p-3 shadow-sm">
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
