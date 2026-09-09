import { CircleAlert, ListChecks, Plus, Sparkles } from "lucide-react";
import { Link, Navigate } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { TicketOverviewItem } from "@/features/tickets/components/ticket-overview-item";
import { useMyTickets } from "@/features/tickets/hooks/use-tickets";
import type { TicketStatus } from "@/features/tickets/types/ticket.types";
import { ErrorState } from "@/shared/components/error-state";

const ATTENTION_STATUSES: TicketStatus[] = ["WAITING_CLIENT", "RESOLVED"];
const ACTIVE_STATUSES: TicketStatus[] = ["OPEN", "IN_PROGRESS", "WAITING_AGENT"];
const SECTION_SIZE = 4;

type EmptySectionProps = {
    title: string;
    description: string;
};

function EmptySection({ title, description }: EmptySectionProps) {
    return (
        <div className="rounded-2xl border border-dashed border-border bg-card/60 px-5 py-9 text-center">
            <p className="font-semibold text-foreground">{title}</p>
            <p className="mt-1 text-sm text-muted-foreground">{description}</p>
        </div>
    );
}

function SectionSkeleton() {
    return (
        <div className="space-y-3" aria-label="Carregando chamados">
            {Array.from({ length: 2 }).map((_, index) => (
                <div key={index} className="h-36 animate-pulse rounded-2xl border border-border bg-muted/70" />
            ))}
        </div>
    );
}

export function ClientHomePage() {
    const user = useAuthStore((state) => state.user);

    const attentionQuery = useMyTickets(
        { status: ATTENTION_STATUSES, page: 0, size: SECTION_SIZE },
        { enabled: user?.role === "CLIENT" },
    );
    const activeQuery = useMyTickets(
        { status: ACTIVE_STATUSES, page: 0, size: SECTION_SIZE },
        { enabled: user?.role === "CLIENT" },
    );

    if (user?.role !== "CLIENT") {
        return <Navigate to="/tickets" replace />;
    }

    const firstName = user.name.trim().split(/\s+/)[0];
    const hasError = attentionQuery.isError || activeQuery.isError;

    async function retryQueries() {
        await Promise.all([attentionQuery.refetch(), activeQuery.refetch()]);
    }

    if (hasError) {
        return (
            <ErrorState
                title="Não foi possível carregar seu início"
                description="Tivemos um problema ao buscar seus chamados mais recentes. Tente novamente."
                onRetry={retryQueries}
                isRetrying={attentionQuery.isFetching || activeQuery.isFetching}
            />
        );
    }

    const attentionTickets = attentionQuery.data?.content ?? [];
    const activeTickets = activeQuery.data?.content ?? [];

    return (
        <div className="space-y-8">
            <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#4657a9] via-[#6366c7] to-[#256d85] px-6 py-8 text-white shadow-[0_25px_60px_-30px_#0d121c] sm:px-8 sm:py-10">
                <div className="absolute -right-12 -top-20 size-64 rounded-full border-[32px] border-white/5" />
                <div className="relative flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                        <div className="mb-3 flex items-center gap-2 text-xs font-bold uppercase tracking-[0.18em] text-white/75">
                            <Sparkles className="size-3.5" /> Sua central de suporte
                        </div>
                        <h1 className="text-3xl font-bold tracking-tight">Olá, {firstName}</h1>
                        <p className="mt-2 max-w-xl text-sm leading-6 text-white/70">
                            Veja o que precisa da sua atenção e acompanhe seus chamados mais recentes.
                        </p>
                    </div>

                    <Button asChild size="lg" className="bg-primary text-primary-foreground shadow-black/30 hover:bg-primary-strong">
                        <Link to="/tickets/new"><Plus /> Abrir novo chamado</Link>
                    </Button>
                </div>
            </section>

            <section aria-labelledby="attention-title" className="space-y-4">
                <div className="flex flex-wrap items-end justify-between gap-3">
                    <div>
                        <div className="flex items-center gap-2">
                            <span className="flex size-9 items-center justify-center rounded-xl bg-amber-100 text-amber-800 dark:bg-amber-950/70 dark:text-amber-200">
                                <CircleAlert className="size-4" />
                            </span>
                            <h2 id="attention-title" className="text-xl font-bold tracking-tight">Precisa da sua atenção</h2>
                            {!!attentionQuery.data?.totalElements && (
                                <span className="rounded-full bg-amber-100 px-2.5 py-1 text-xs font-bold text-amber-900 dark:bg-amber-950/70 dark:text-amber-200">
                                    {attentionQuery.data.totalElements}
                                </span>
                            )}
                        </div>
                        <p className="mt-2 text-sm text-muted-foreground">Responda ao suporte ou revise uma solução enviada.</p>
                    </div>
                </div>

                {attentionQuery.isPending ? (
                    <SectionSkeleton />
                ) : attentionTickets.length === 0 ? (
                    <EmptySection title="Tudo em dia" description="Nenhum chamado precisa de uma ação sua agora." />
                ) : (
                    <div className="space-y-3">
                        {attentionTickets.map((ticket) => (
                            <TicketOverviewItem key={ticket.id} ticket={ticket} needsAttention />
                        ))}
                    </div>
                )}
            </section>

            <section aria-labelledby="active-title" className="space-y-4">
                <div className="flex flex-wrap items-end justify-between gap-3">
                    <div>
                        <div className="flex items-center gap-2">
                            <span className="flex size-9 items-center justify-center rounded-xl bg-primary/15 text-primary-strong">
                                <ListChecks className="size-4" />
                            </span>
                            <h2 id="active-title" className="text-xl font-bold tracking-tight">Em andamento</h2>
                        </div>
                        <p className="mt-2 text-sm text-muted-foreground">Uma visão rápida dos seus chamados ativos mais recentes.</p>
                    </div>
                    <Button asChild variant="outline" size="sm">
                        <Link to="/tickets">Ver todos</Link>
                    </Button>
                </div>

                {activeQuery.isPending ? (
                    <SectionSkeleton />
                ) : activeTickets.length === 0 ? (
                    <EmptySection title="Nenhum chamado em andamento" description="Quando houver movimentações, elas aparecerão aqui." />
                ) : (
                    <div className="space-y-3">
                        {activeTickets.map((ticket) => (
                            <TicketOverviewItem key={ticket.id} ticket={ticket} />
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}
