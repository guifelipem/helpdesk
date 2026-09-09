import {
    AlertTriangle,
    ArrowRight,
    CheckCircle2,
    CircleDot,
    Clock3,
    Inbox,
    LayoutDashboard,
    MessageCircleMore,
    UserRoundCheck,
    UsersRound,
    CalendarDays,
    Timer,
    TrendingUp,
} from "lucide-react";
import { Link, Navigate } from "react-router-dom";
import { useState } from "react";

import { Skeleton } from "@/components/ui/skeleton";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { TicketPriorityBadge } from "@/features/tickets/components/ticket-priority-badge";
import { TicketStatusBadge } from "@/features/tickets/components/ticket-status-badge";
import { useAdminTicketDashboard, useAdminTicketPerformance } from "@/features/tickets/hooks/use-tickets";
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
    if (minutes < 60) return `${minutes}min`;
    const hours = Math.round((minutes / 60) * 10) / 10;
    return `${hours.toLocaleString("pt-BR")}h`;
}

function attentionReason(ticket: Ticket) {
    if (!ticket.assignedTo) return "Sem responsável";
    if (ticket.priority === "HIGH") return "Prioridade alta";
    if (Date.now() - new Date(ticket.updatedAt).getTime() >= staleThreshold) return "Sem atualização há mais de 3 dias";
    return "Requer acompanhamento";
}

export function AdminDashboardPage() {
    const user = useAuthStore((state) => state.user);
    const defaultStart = new Date();
    defaultStart.setDate(defaultStart.getDate() - 29);
    const [period, setPeriod] = useState<Period>("LAST_30");
    const [customFrom, setCustomFrom] = useState(toDateInput(defaultStart));
    const [customTo, setCustomTo] = useState(toDateInput(new Date()));
    const range = periodRange(period, customFrom, customTo);
    const dashboardQuery = useAdminTicketDashboard(user?.role === "ADMIN");
    const performanceQuery = useAdminTicketPerformance(range.from, range.to, user?.role === "ADMIN" && range.from < range.to);

    if (user?.role !== "ADMIN") return <Navigate to="/" replace />;

    if (dashboardQuery.isError) {
        return (
            <ErrorState
                title="Não foi possível carregar a visão da operação"
                description="Tivemos um problema ao buscar os indicadores administrativos."
                onRetry={() => dashboardQuery.refetch()}
                isRetrying={dashboardQuery.isFetching}
            />
        );
    }

    if (dashboardQuery.isPending) return <DashboardSkeleton />;

    const dashboard = dashboardQuery.data;
    const cards = [
        { label: "Abertos", value: dashboard.totalActive, href: "/tickets?active=true", icon: CircleDot, tone: "text-primary-strong bg-primary/15" },
        { label: "Sem responsável", value: dashboard.unassigned, href: "/tickets?unassigned=true&active=true", icon: Inbox, tone: "text-rose-700 bg-rose-100 dark:text-rose-200 dark:bg-rose-950/60" },
        { label: "Em andamento", value: dashboard.inProgress, href: "/tickets?status=IN_PROGRESS", icon: Clock3, tone: "text-sky-700 bg-sky-100 dark:text-sky-200 dark:bg-sky-950/60" },
        { label: "Aguardando cliente", value: dashboard.waitingClient, href: "/tickets?status=WAITING_CLIENT", icon: MessageCircleMore, tone: "text-amber-700 bg-amber-100 dark:text-amber-200 dark:bg-amber-950/60" },
        { label: "Aguardando agente", value: dashboard.waitingAgent, href: "/tickets?status=WAITING_AGENT", icon: UserRoundCheck, tone: "text-violet-700 bg-violet-100 dark:text-violet-200 dark:bg-violet-950/60" },
        { label: "Resolvidos", value: dashboard.resolved, href: "/tickets?status=RESOLVED", icon: CheckCircle2, tone: "text-emerald-700 bg-emerald-100 dark:text-emerald-200 dark:bg-emerald-950/60" },
    ];
    const maxLoad = Math.max(...dashboard.activeByAgent.map((agent) => agent.activeTickets), 1);

    return (
        <div className="space-y-8">
            <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#4657a9] via-[#6366c7] to-[#256d85] px-6 py-8 text-white shadow-[0_25px_60px_-30px_#0d121c] sm:px-8">
                <div className="absolute -right-10 -top-16 size-56 rounded-full border-[30px] border-white/5" />
                <div className="relative">
                    <div className="mb-3 flex items-center gap-2 text-xs font-bold uppercase tracking-[0.18em] text-white/75"><LayoutDashboard className="size-4" /> Visão operacional</div>
                    <h1 className="text-3xl font-bold tracking-tight">Dashboard administrativo</h1>
                    <p className="mt-2 max-w-2xl text-sm text-white/70">Acompanhe o volume, identifique gargalos e navegue para os chamados que precisam de supervisão.</p>
                </div>
            </section>

            <section aria-labelledby="operation-summary">
                <div className="mb-4 flex items-end justify-between gap-4">
                    <div><h2 id="operation-summary" className="text-xl font-bold">Operação agora</h2><p className="mt-1 text-sm text-muted-foreground">Chamados resolvidos continuam ativos até o fechamento.</p></div>
                    <Link to="/tickets" className="hidden items-center gap-1 text-sm font-semibold text-primary-strong hover:underline sm:flex">Ver todos <ArrowRight className="size-4" /></Link>
                </div>
                <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
                    {cards.map(({ label, value, href, icon: Icon, tone }) => (
                        <Link key={label} to={href} className="group rounded-2xl border border-border bg-card/90 p-5 shadow-[0_16px_40px_-30px_#256d85] transition hover:-translate-y-0.5 hover:border-primary/45 hover:shadow-[0_20px_45px_-28px_#256d85]">
                            <div className="flex items-start justify-between">
                                <div><p className="text-sm font-medium text-muted-foreground">{label}</p><p className="mt-2 text-3xl font-bold tracking-tight">{value}</p></div>
                                <div className={`flex size-10 items-center justify-center rounded-xl ${tone}`}><Icon className="size-5" /></div>
                            </div>
                            <div className="mt-5 flex items-center gap-1 text-xs font-semibold text-primary-strong">Abrir listagem <ArrowRight className="size-3.5 transition group-hover:translate-x-0.5" /></div>
                        </Link>
                    ))}
                </div>
            </section>

            <div className="grid gap-6 xl:grid-cols-[minmax(0,1.05fr)_minmax(360px,.95fr)]">
                <section className="rounded-2xl border border-border bg-card/90 p-5 sm:p-6" aria-labelledby="agent-load">
                    <div className="mb-5 flex items-center gap-3"><div className="flex size-10 items-center justify-center rounded-xl bg-secondary/35 text-accent"><UsersRound className="size-5" /></div><div><h2 id="agent-load" className="text-lg font-bold">Carga por agente</h2><p className="text-sm text-muted-foreground">Chamados ativos atualmente atribuídos.</p></div></div>
                    {dashboard.activeByAgent.length === 0 ? <p className="rounded-xl bg-muted/60 p-5 text-sm text-muted-foreground">Nenhum agente cadastrado.</p> : (
                        <div className="space-y-2">
                            {dashboard.activeByAgent.map((agent) => (
                                <Link key={agent.agentId} to={`/tickets?agentId=${agent.agentId}&active=true`} className="group flex items-center gap-4 rounded-xl p-3 transition hover:bg-muted/70">
                                    <div className="flex size-10 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-secondary to-primary text-sm font-bold text-secondary-foreground">{agent.agentName.charAt(0).toUpperCase()}</div>
                                    <div className="min-w-0 flex-1"><div className="mb-2 flex justify-between gap-3"><span className="truncate text-sm font-semibold">{agent.agentName}</span><span className="text-sm font-bold">{agent.activeTickets}</span></div><div className="h-1.5 overflow-hidden rounded-full bg-muted"><div className="h-full rounded-full bg-gradient-to-r from-accent to-primary" style={{ width: `${(agent.activeTickets / maxLoad) * 100}%` }} /></div></div>
                                    <ArrowRight className="size-4 text-muted-foreground transition group-hover:translate-x-0.5 group-hover:text-primary-strong" />
                                </Link>
                            ))}
                        </div>
                    )}
                </section>

                <section className="rounded-2xl border border-border bg-card/90 p-5 sm:p-6" aria-labelledby="attention-list">
                    <div className="mb-5 flex items-center gap-3"><div className="flex size-10 items-center justify-center rounded-xl bg-amber-100 text-amber-700 dark:bg-amber-950/60 dark:text-amber-200"><AlertTriangle className="size-5" /></div><div><h2 id="attention-list" className="text-lg font-bold">Exigem atenção</h2><p className="text-sm text-muted-foreground">Sem responsável, alta prioridade ou sem atualização.</p></div></div>
                    {dashboard.attentionTickets.length === 0 ? <p className="rounded-xl bg-muted/60 p-5 text-sm text-muted-foreground">Nenhum chamado exige atenção agora.</p> : (
                        <div className="divide-y divide-border">
                            {dashboard.attentionTickets.map((ticket) => (
                                <Link key={ticket.id} to={`/tickets/${ticket.id}`} className="group block py-4 first:pt-0 last:pb-0">
                                    <div className="flex items-start justify-between gap-3"><div className="min-w-0"><p className="truncate text-sm font-semibold group-hover:text-primary-strong">#{ticket.id} · {ticket.title}</p><p className="mt-1 text-xs font-medium text-amber-700 dark:text-amber-300">{attentionReason(ticket)}</p></div><ArrowRight className="mt-1 size-4 shrink-0 text-muted-foreground" /></div>
                                    <div className="mt-3 flex flex-wrap items-center gap-2"><TicketPriorityBadge priority={ticket.priority} /><TicketStatusBadge status={ticket.status} /><span className="text-xs text-muted-foreground">Atualizado {formatRelativeDate(ticket.updatedAt)}</span></div>
                                </Link>
                            ))}
                        </div>
                    )}
                </section>
            </div>

            <section className="rounded-2xl border border-border bg-card/90 p-5 sm:p-6" aria-labelledby="period-performance">
                <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                    <div><div className="flex items-center gap-2"><TrendingUp className="size-5 text-primary-strong" /><h2 id="period-performance" className="text-lg font-bold">Desempenho no período</h2></div><p className="mt-1 text-sm text-muted-foreground">Movimentações e tempo de resolução somente no intervalo selecionado.</p></div>
                    <div className="flex flex-col items-stretch gap-2 sm:items-end">
                        <label htmlFor="performance-period" className="sr-only">Selecionar período</label>
                        <select id="performance-period" value={period} onChange={(event) => setPeriod(event.target.value as Period)} className="h-10 rounded-xl border border-input bg-card px-3 text-sm font-medium outline-none focus:border-ring focus:ring-3 focus:ring-ring/20">
                            <option value="TODAY">Hoje</option><option value="LAST_7">Últimos 7 dias</option><option value="LAST_30">Últimos 30 dias</option><option value="LAST_90">Últimos 90 dias</option><option value="THIS_MONTH">Este mês</option><option value="PREVIOUS_MONTH">Mês anterior</option><option value="CUSTOM">Personalizado</option>
                        </select>
                        {period === "CUSTOM" && <div className="flex items-center gap-2"><input aria-label="Data inicial" type="date" value={customFrom} max={customTo} onChange={(event) => setCustomFrom(event.target.value)} className="h-10 rounded-xl border border-input bg-card px-3 text-sm" /><span className="text-xs text-muted-foreground">até</span><input aria-label="Data final" type="date" value={customTo} min={customFrom} onChange={(event) => setCustomTo(event.target.value)} className="h-10 rounded-xl border border-input bg-card px-3 text-sm" /></div>}
                    </div>
                </div>

                {performanceQuery.isError ? <div className="rounded-xl border border-dashed border-destructive/40 p-5 text-sm text-muted-foreground">Não foi possível carregar os indicadores deste período. <button type="button" className="font-semibold text-primary-strong hover:underline" onClick={() => performanceQuery.refetch()}>Tentar novamente</button></div> : performanceQuery.isPending || performanceQuery.isFetching ? (
                    <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">{Array.from({ length: 4 }).map((_, index) => <Skeleton key={index} className="h-28 rounded-xl" />)}</div>
                ) : (
                    <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                        {[
                            { label: "Criados", value: performanceQuery.data.created, icon: CalendarDays },
                            { label: "Resolvidos", value: performanceQuery.data.resolved, icon: CheckCircle2 },
                            { label: "Fechados", value: performanceQuery.data.closed, icon: CircleDot },
                            { label: "Tempo médio de resolução", value: formatResolutionTime(performanceQuery.data.averageResolutionMinutes), icon: Timer },
                        ].map(({ label, value, icon: Icon }) => <div key={label} className="rounded-xl border border-border/80 bg-muted/35 p-4"><div className="flex items-center justify-between gap-3"><p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">{label}</p><Icon className="size-4 text-primary-strong" /></div><p className="mt-4 text-2xl font-bold tracking-tight">{value}</p></div>)}
                    </div>
                )}
            </section>
        </div>
    );
}

function DashboardSkeleton() {
    return <div className="space-y-8"><Skeleton className="h-40 rounded-3xl" /><div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">{Array.from({ length: 6 }).map((_, index) => <Skeleton key={index} className="h-40 rounded-2xl" />)}</div><div className="grid gap-6 xl:grid-cols-2"><Skeleton className="h-96 rounded-2xl" /><Skeleton className="h-96 rounded-2xl" /></div></div>;
}
