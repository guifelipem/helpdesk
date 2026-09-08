import {
    AlertTriangle,
    ArrowRight,
    BadgeCheck,
    CalendarCheck2,
    Clock3,
    Headphones,
    Inbox,
    ListChecks,
    MessageCircleMore,
    Timer,
} from "lucide-react";
import { Link, Navigate } from "react-router-dom";
import { useState } from "react";

import { Skeleton } from "@/components/ui/skeleton";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { TicketPriorityBadge } from "@/features/tickets/components/ticket-priority-badge";
import { TicketStatusBadge } from "@/features/tickets/components/ticket-status-badge";
import { useAgentTicketDashboard, useAgentTicketPerformance } from "@/features/tickets/hooks/use-tickets";
import type { Ticket } from "@/features/tickets/types/ticket.types";
import { ErrorState } from "@/shared/components/error-state";
import { formatRelativeDate } from "@/shared/utils/format-date";

const staleThreshold = 72 * 60 * 60 * 1000;
type Period = "TODAY" | "LAST_7" | "LAST_30" | "LAST_90" | "THIS_MONTH" | "PREVIOUS_MONTH" | "CUSTOM";

function toLocalDateTime(date: Date) {
    return new Date(date.getTime() - date.getTimezoneOffset() * 60_000).toISOString().slice(0, 19);
}

function toDateInput(date: Date) {
    return toLocalDateTime(date).slice(0, 10);
}

function periodRange(period: Period, customFrom: string, customTo: string) {
    if (period === "CUSTOM" && (!customFrom || !customTo)) return { from: "", to: "" };
    const now = new Date();
    let from = new Date(now);
    let to = new Date(now);

    if (period === "TODAY") {
        from.setHours(0, 0, 0, 0);
        to = new Date(from);
        to.setDate(to.getDate() + 1);
    } else if (period === "LAST_7" || period === "LAST_30" || period === "LAST_90") {
        from.setDate(from.getDate() - Number(period.split("_")[1]));
    } else if (period === "THIS_MONTH") {
        from = new Date(now.getFullYear(), now.getMonth(), 1);
        to = new Date(now.getFullYear(), now.getMonth() + 1, 1);
    } else if (period === "PREVIOUS_MONTH") {
        from = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        to = new Date(now.getFullYear(), now.getMonth(), 1);
    } else {
        from = new Date(`${customFrom}T00:00:00`);
        to = new Date(`${customTo}T00:00:00`);
        to.setDate(to.getDate() + 1);
    }
    return { from: toLocalDateTime(from), to: toLocalDateTime(to) };
}

function formatResolutionTime(minutes: number | null) {
    if (minutes === null) return "—";
    if (minutes < 60) return `${minutes} min`;
    return `${(Math.round((minutes / 60) * 10) / 10).toLocaleString("pt-BR")} h`;
}

function priorityReason(ticket: Ticket) {
    if (!ticket.assignedTo) return "Disponível há mais tempo";
    if (ticket.status === "WAITING_AGENT") return "Aguardando sua resposta";
    if (ticket.priority === "HIGH") return "Prioridade alta";
    if (Date.now() - new Date(ticket.updatedAt).getTime() >= staleThreshold) return "Sem atualização há mais de 3 dias";
    return "Requer atenção";
}

export function AgentDashboardPage() {
    const user = useAuthStore((state) => state.user);
    const defaultStart = new Date();
    defaultStart.setDate(defaultStart.getDate() - 29);
    const [period, setPeriod] = useState<Period>("LAST_30");
    const [customFrom, setCustomFrom] = useState(toDateInput(defaultStart));
    const [customTo, setCustomTo] = useState(toDateInput(new Date()));
    const range = periodRange(period, customFrom, customTo);
    const dashboardQuery = useAgentTicketDashboard(user?.role === "AGENT");
    const performanceQuery = useAgentTicketPerformance(range.from, range.to, user?.role === "AGENT" && range.from < range.to);

    if (user?.role !== "AGENT") return <Navigate to={user?.role === "CLIENT" ? "/home" : "/dashboard"} replace />;
    if (dashboardQuery.isError) {
        return <ErrorState title="Não foi possível carregar seu painel" description="Tivemos um problema ao buscar seus indicadores de trabalho." onRetry={() => dashboardQuery.refetch()} isRetrying={dashboardQuery.isFetching} />;
    }
    if (dashboardQuery.isPending) return <AgentDashboardSkeleton />;

    const dashboard = dashboardQuery.data;
    const cards = [
        { label: "Meus chamados ativos", value: dashboard.active, href: "/queues?queue=MY_TICKETS", icon: ListChecks, tone: "bg-primary/15 text-primary-strong" },
        { label: "Aguardando cliente", value: dashboard.waitingClient, href: "/queues?queue=WAITING_CLIENT", icon: Clock3, tone: "bg-amber-100 text-amber-700 dark:bg-amber-950/60 dark:text-amber-200" },
        { label: "Aguardando minha resposta", value: dashboard.waitingAgent, href: "/queues?queue=WAITING_AGENT", icon: MessageCircleMore, tone: "bg-rose-100 text-rose-700 dark:bg-rose-950/60 dark:text-rose-200" },
        { label: "Resolvidos aguardando confirmação", value: dashboard.resolvedAwaitingConfirmation, href: "/queues?queue=RESOLVED", icon: BadgeCheck, tone: "bg-emerald-100 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-200" },
        { label: "Chamados disponíveis", value: dashboard.available, href: "/queues?queue=AVAILABLE", icon: Inbox, tone: "bg-sky-100 text-sky-700 dark:bg-sky-950/60 dark:text-sky-200" },
    ];

    return (
        <div className="space-y-8">
            <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#272e62] via-[#373384] to-[#4794b8] px-6 py-8 text-white shadow-[0_25px_60px_-30px_#030607] sm:px-8">
                <div className="absolute -right-10 -top-16 size-56 rounded-full border-[30px] border-white/5" />
                <div className="relative">
                    <div className="mb-3 flex items-center gap-2 text-xs font-bold uppercase tracking-[0.18em] text-white/75"><Headphones className="size-4" /> Seu espaço de trabalho</div>
                    <h1 className="text-3xl font-bold tracking-tight">Olá, {user.name.split(" ")[0]}</h1>
                    <p className="mt-2 max-w-2xl text-sm text-white/70">Veja o que pede sua atenção e siga para o próximo atendimento.</p>
                </div>
            </section>

            <section aria-labelledby="work-summary">
                <div className="mb-4 flex items-end justify-between gap-4">
                    <div><h2 id="work-summary" className="text-xl font-bold">Meu trabalho agora</h2><p className="mt-1 text-sm text-muted-foreground">Uma visão rápida das suas filas de atendimento.</p></div>
                    <Link to="/queues" className="hidden items-center gap-1 text-sm font-semibold text-primary-strong hover:underline sm:flex">Abrir filas <ArrowRight className="size-4" /></Link>
                </div>
                <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
                    {cards.map(({ label, value, href, icon: Icon, tone }) => (
                        <Link key={label} to={href} className="group rounded-2xl border border-border bg-card/90 p-5 shadow-[0_16px_40px_-30px_#4794b8] transition hover:-translate-y-0.5 hover:border-primary/45">
                            <div className={`flex size-10 items-center justify-center rounded-xl ${tone}`}><Icon className="size-5" /></div>
                            <p className="mt-4 text-3xl font-bold tracking-tight">{value}</p>
                            <p className="mt-1 min-h-10 text-sm font-medium leading-5 text-muted-foreground">{label}</p>
                            <span className="mt-3 flex items-center gap-1 text-xs font-semibold text-primary-strong">Ver chamados <ArrowRight className="size-3.5 transition group-hover:translate-x-0.5" /></span>
                        </Link>
                    ))}
                </div>
            </section>

            <div className="grid gap-6 xl:grid-cols-[minmax(0,1.35fr)_minmax(300px,.65fr)]">
                <section className="rounded-2xl border border-border bg-card/90 p-5 sm:p-6" aria-labelledby="priority-now">
                    <div className="mb-5 flex items-center justify-between gap-4">
                        <div className="flex items-center gap-3"><div className="flex size-10 items-center justify-center rounded-xl bg-rose-100 text-rose-700 dark:bg-rose-950/60 dark:text-rose-200"><AlertTriangle className="size-5" /></div><div><h2 id="priority-now" className="text-lg font-bold">Prioridade agora</h2><p className="text-sm text-muted-foreground">Ordenado pelo que mais precisa da sua ação.</p></div></div>
                    </div>
                    {dashboard.priorityNow.length === 0 ? (
                        <div className="rounded-xl bg-muted/55 px-5 py-8 text-center"><CalendarCheck2 className="mx-auto size-7 text-emerald-600" /><p className="mt-3 font-semibold">Tudo em dia por aqui</p><p className="mt-1 text-sm text-muted-foreground">Nenhum chamado precisa de atenção imediata.</p></div>
                    ) : (
                        <div className="divide-y divide-border">
                            {dashboard.priorityNow.map((ticket) => (
                                <Link key={ticket.id} to={`/tickets/${ticket.id}`} className="group flex flex-col gap-3 py-4 first:pt-0 last:pb-0 sm:flex-row sm:items-center">
                                    <div className="min-w-0 flex-1"><p className="text-xs font-bold tracking-wider text-primary-strong">{priorityReason(ticket)}</p><h3 className="mt-1 truncate font-semibold group-hover:text-primary-strong">#{ticket.id} · {ticket.title}</h3><p className="mt-1 text-xs text-muted-foreground">Atualizado {formatRelativeDate(ticket.updatedAt)} · {ticket.createdBy.name}</p></div>
                                    <div className="flex shrink-0 items-center gap-2"><TicketPriorityBadge priority={ticket.priority} /><TicketStatusBadge status={ticket.status} /><ArrowRight className="size-4 text-muted-foreground transition group-hover:translate-x-0.5" /></div>
                                </Link>
                            ))}
                        </div>
                    )}
                </section>

                <section className="rounded-2xl border border-border bg-card/90 p-5 sm:p-6" aria-labelledby="my-performance">
                    <div className="flex flex-col gap-4">
                        <div className="flex items-center gap-3"><div className="flex size-10 items-center justify-center rounded-xl bg-secondary/35 text-accent"><Timer className="size-5" /></div><div><h2 id="my-performance" className="text-lg font-bold">Meu ritmo</h2><p className="text-sm text-muted-foreground">Resultados no período selecionado.</p></div></div>
                        <div className="flex flex-col items-stretch gap-2">
                            <label htmlFor="agent-performance-period" className="sr-only">Selecionar período</label>
                            <select id="agent-performance-period" value={period} onChange={(event) => setPeriod(event.target.value as Period)} className="h-10 rounded-xl border border-input bg-card px-3 text-sm font-medium outline-none focus:border-ring focus:ring-3 focus:ring-ring/20">
                                <option value="TODAY">Hoje</option><option value="LAST_7">Últimos 7 dias</option><option value="LAST_30">Últimos 30 dias</option><option value="LAST_90">Últimos 90 dias</option><option value="THIS_MONTH">Este mês</option><option value="PREVIOUS_MONTH">Mês anterior</option><option value="CUSTOM">Personalizado</option>
                            </select>
                            {period === "CUSTOM" && <div className="flex items-center gap-2"><input aria-label="Data inicial" type="date" value={customFrom} max={customTo} onChange={(event) => setCustomFrom(event.target.value)} className="h-10 min-w-0 flex-1 rounded-xl border border-input bg-card px-2 text-sm" /><span className="text-xs text-muted-foreground">até</span><input aria-label="Data final" type="date" value={customTo} min={customFrom} onChange={(event) => setCustomTo(event.target.value)} className="h-10 min-w-0 flex-1 rounded-xl border border-input bg-card px-2 text-sm" /></div>}
                        </div>
                    </div>
                    {performanceQuery.isError ? (
                        <div className="mt-6 rounded-xl border border-dashed border-destructive/40 p-5 text-sm text-muted-foreground">Não foi possível carregar este período. <button type="button" className="font-semibold text-primary-strong hover:underline" onClick={() => performanceQuery.refetch()}>Tentar novamente</button></div>
                    ) : performanceQuery.isPending || performanceQuery.isFetching ? (
                        <div className="mt-6 space-y-3"><Skeleton className="h-28 rounded-xl" /><Skeleton className="h-24 rounded-xl" /></div>
                    ) : (
                        <div className="mt-6 space-y-3">
                            <div className="rounded-xl bg-muted/55 p-4"><p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Tempo médio de resolução</p><p className="mt-2 text-2xl font-bold">{formatResolutionTime(performanceQuery.data.averageResolutionMinutes)}</p></div>
                            <div className="rounded-xl border border-border p-4"><p className="text-2xl font-bold">{performanceQuery.data.resolved}</p><p className="mt-1 text-xs text-muted-foreground">Resolvidos no período</p></div>
                        </div>
                    )}
                </section>
            </div>
        </div>
    );
}

function AgentDashboardSkeleton() {
    return <div className="space-y-8"><Skeleton className="h-44 rounded-3xl" /><div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">{Array.from({ length: 5 }, (_, index) => <Skeleton key={index} className="h-48 rounded-2xl" />)}</div><div className="grid gap-6 xl:grid-cols-[1.35fr_.65fr]"><Skeleton className="h-96 rounded-2xl" /><Skeleton className="h-80 rounded-2xl" /></div></div>;
}
