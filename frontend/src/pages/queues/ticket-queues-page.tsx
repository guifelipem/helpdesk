import { Headphones, Inbox, ListChecks, MessageCircleMore, TimerReset, BadgeCheck } from "lucide-react";
import { useState } from "react";
import { Navigate, useSearchParams } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { TicketQueueItem } from "@/features/tickets/components/ticket-queue-item";
import { TicketQueueSkeleton } from "@/features/tickets/components/ticket-queue-skeleton";
import { useAssignTicket, useTicketQueue, useTicketQueueSummary } from "@/features/tickets/hooks/use-tickets";
import type { TicketQueue, TicketQueueSummary } from "@/features/tickets/types/ticket-queue.types";
import { cn } from "@/lib/utils";
import { ErrorState } from "@/shared/components/error-state";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";

const PAGE_SIZE = 8;

const queueTabs: Array<{
    value: TicketQueue;
    label: string;
    countKey: keyof TicketQueueSummary;
    emptyMessage: string;
    icon: typeof Inbox;
}> = [
    {
        value: "AVAILABLE",
        label: "Disponíveis",
        countKey: "available",
        emptyMessage: "Nenhum chamado disponível no momento.",
        icon: Inbox,
    },
    {
        value: "MY_TICKETS",
        label: "Meus chamados",
        countKey: "myTickets",
        emptyMessage: "Você ainda não possui chamados atribuídos.",
        icon: ListChecks,
    },
    {
        value: "WAITING_CLIENT",
        label: "Aguardando cliente",
        countKey: "waitingClient",
        emptyMessage: "Você não possui chamados aguardando cliente.",
        icon: TimerReset,
    },
    {
        value: "WAITING_AGENT",
        label: "Aguardando minha resposta",
        countKey: "waitingAgent",
        emptyMessage: "Nenhum chamado está aguardando sua resposta.",
        icon: MessageCircleMore,
    },
    {
        value: "RESOLVED",
        label: "Resolvidos",
        countKey: "resolved",
        emptyMessage: "Nenhum chamado resolvido está aguardando confirmação.",
        icon: BadgeCheck,
    },
];

const initialPages: Record<TicketQueue, number> = {
    AVAILABLE: 0,
    MY_TICKETS: 0,
    WAITING_CLIENT: 0,
    WAITING_AGENT: 0,
    RESOLVED: 0,
};

export function TicketQueuesPage() {
    const user = useAuthStore((state) => state.user);
    const isAgent = user?.role === "AGENT";
    const [searchParams, setSearchParams] = useSearchParams();
    const requestedQueue = searchParams.get("queue") as TicketQueue | null;
    const initialQueue = queueTabs.some((queue) => queue.value === requestedQueue) ? requestedQueue! : "AVAILABLE";
    const [activeQueue, setActiveQueue] = useState<TicketQueue>(initialQueue);
    const [pages, setPages] = useState<Record<TicketQueue, number>>(initialPages);

    const summaryQuery = useTicketQueueSummary({ enabled: isAgent });
    const queueQuery = useTicketQueue(
        activeQueue,
        { page: pages[activeQueue], size: PAGE_SIZE },
        { enabled: isAgent },
    );
    const assignTicket = useAssignTicket();

    if (!isAgent) {
        return <Navigate to={user?.role === "CLIENT" ? "/home" : "/tickets"} replace />;
    }

    const activeConfig = queueTabs.find((queue) => queue.value === activeQueue) ?? queueTabs[0];
    const tickets = queueQuery.data?.content ?? [];
    const hasError = summaryQuery.isError || queueQuery.isError;

    function changePage(nextPage: number) {
        setPages((current) => ({ ...current, [activeQueue]: nextPage }));
    }

    function changeQueue(queue: TicketQueue) {
        setActiveQueue(queue);
        setSearchParams(queue === "AVAILABLE" ? {} : { queue });
        assignTicket.reset();
    }

    function retryQueries() {
        return Promise.all([summaryQuery.refetch(), queueQuery.refetch()]);
    }

    if (hasError) {
        return (
            <ErrorState
                title="Não foi possível carregar as filas"
                description="Tivemos um problema ao buscar as filas de atendimento. Tente novamente."
                onRetry={retryQueries}
                isRetrying={summaryQuery.isFetching || queueQuery.isFetching}
            />
        );
    }

    const assignError = assignTicket.isError
        ? getApiErrorMessage(
            assignTicket.error,
            "Não foi possível assumir o chamado. Ele pode ter sido assumido por outro agente.",
        )
        : undefined;

    return (
        <div className="space-y-7">
            <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#272e62] via-[#373384] to-[#4794b8] px-6 py-8 text-white shadow-[0_25px_60px_-30px_#030607] sm:px-8 sm:py-10">
                <div className="absolute -right-12 -top-20 size-64 rounded-full border-[32px] border-white/5" />
                <div className="relative">
                    <div className="mb-3 flex items-center gap-2 text-xs font-bold uppercase tracking-[0.18em] text-white/75">
                        <Headphones className="size-3.5" /> Central de atendimento
                    </div>
                    <h1 className="text-3xl font-bold tracking-tight">Filas de atendimento</h1>
                    <p className="mt-2 max-w-xl text-sm leading-6 text-white/70">
                        Organize seus chamados por etapa de atendimento.
                    </p>
                </div>
            </section>

            <section aria-label="Filas de chamados">
                <div className="overflow-x-auto pb-2">
                    <div className="flex min-w-max gap-2" role="tablist" aria-label="Selecione uma fila">
                        {queueTabs.map((queue) => {
                            const Icon = queue.icon;
                            const isActive = activeQueue === queue.value;
                            const count = summaryQuery.data?.[queue.countKey];

                            return (
                                <button
                                    key={queue.value}
                                    type="button"
                                    role="tab"
                                    aria-selected={isActive}
                                    aria-controls="active-ticket-queue"
                                    onClick={() => changeQueue(queue.value)}
                                    className={cn(
                                        "flex h-11 items-center gap-2 rounded-xl border px-3.5 text-sm font-semibold transition focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-ring/30",
                                        isActive
                                            ? "border-primary/50 bg-primary/15 text-primary-strong shadow-sm"
                                            : "border-border bg-card/75 text-muted-foreground hover:border-primary/35 hover:text-foreground",
                                        queue.value === "WAITING_AGENT" && !isActive && "border-primary/30",
                                    )}
                                >
                                    <Icon className="size-4" />
                                    {queue.label}
                                    {summaryQuery.isPending ? (
                                        <Skeleton className="h-5 w-7 rounded-full" />
                                    ) : (
                                        <span className={cn(
                                            "min-w-6 rounded-full px-1.5 py-0.5 text-center text-xs font-bold",
                                            isActive ? "bg-primary text-primary-foreground" : "bg-muted text-foreground",
                                        )}>
                                            {count ?? 0}
                                        </span>
                                    )}
                                </button>
                            );
                        })}
                    </div>
                </div>
            </section>

            <section id="active-ticket-queue" role="tabpanel" className="space-y-4">
                <div className="flex flex-wrap items-end justify-between gap-3">
                    <div>
                        <h2 className="text-xl font-bold tracking-tight">{activeConfig.label}</h2>
                        {queueQuery.data && (
                            <p className="mt-1 text-sm text-muted-foreground">
                                {queueQuery.data.totalElements} chamado{queueQuery.data.totalElements === 1 ? "" : "s"} nesta fila.
                            </p>
                        )}
                    </div>
                    {queueQuery.isFetching && !queueQuery.isPending && (
                        <span className="text-xs font-medium text-muted-foreground">Atualizando fila...</span>
                    )}
                </div>

                {queueQuery.isPending ? (
                    <TicketQueueSkeleton />
                ) : tickets.length === 0 ? (
                    <div className="rounded-2xl border border-dashed border-primary/35 bg-card/70 px-5 py-14 text-center">
                        <activeConfig.icon className="mx-auto size-7 text-primary-strong" />
                        <p className="mt-3 font-semibold text-foreground">{activeConfig.emptyMessage}</p>
                    </div>
                ) : (
                    <div className="space-y-3">
                        {tickets.map((ticket) => (
                            <TicketQueueItem
                                key={ticket.id}
                                ticket={ticket}
                                queue={activeQueue}
                                onAssign={activeQueue === "AVAILABLE" ? (ticketId) => assignTicket.mutate(ticketId) : undefined}
                                isAssigning={assignTicket.isPending && assignTicket.variables === ticket.id}
                                isAssignDisabled={assignTicket.isPending}
                                assignError={assignTicket.variables === ticket.id ? assignError : undefined}
                            />
                        ))}
                    </div>
                )}

                {queueQuery.data && queueQuery.data.totalPages > 1 && (
                    <div className="flex items-center justify-between rounded-2xl border border-border bg-card/70 p-3 shadow-sm">
                        <Button
                            type="button"
                            variant="outline"
                            disabled={pages[activeQueue] === 0 || queueQuery.isFetching}
                            onClick={() => changePage(pages[activeQueue] - 1)}
                        >
                            Anterior
                        </Button>
                        <span className="text-sm text-muted-foreground">
                            Página {queueQuery.data.page + 1} de {queueQuery.data.totalPages}
                        </span>
                        <Button
                            type="button"
                            variant="outline"
                            disabled={pages[activeQueue] >= queueQuery.data.totalPages - 1 || queueQuery.isFetching}
                            onClick={() => changePage(pages[activeQueue] + 1)}
                        >
                            Próxima
                        </Button>
                    </div>
                )}
            </section>
        </div>
    );
}
