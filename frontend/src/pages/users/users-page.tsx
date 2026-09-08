import { useState } from "react";
import { Navigate } from "react-router-dom";
import { Filter, Search, ShieldCheck, UserCog, UserRoundCheck, UserRoundX } from "lucide-react";
import axios from "axios";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useAuthStore } from "@/features/auth/store/auth.store";
import type { UserRole } from "@/features/auth/types/user.types";
import { useBlockUser, useUnblockUser, useUpdateUserRole, useUsers } from "@/features/users/hooks/use-users";
import { AgentBlockDialog } from "@/features/users/components/agent-block-dialog";
import type { ActiveTicketsConflictResponse, AdminUser, BlockUserParams } from "@/features/users/types/admin-user.types";
import { ErrorState } from "@/shared/components/error-state";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";

const roleLabels: Record<UserRole, string> = {
    CLIENT: "Cliente",
    AGENT: "Agente",
    ADMIN: "Administrador",
};

export function UsersPage() {
    const currentUser = useAuthStore((state) => state.user);
    const [search, setSearch] = useState("");
    const [role, setRole] = useState<UserRole | "">("");
    const [page, setPage] = useState(0);
    const [feedback, setFeedback] = useState<string | null>(null);
    const [blockConflict, setBlockConflict] = useState<{ user: AdminUser; activeTicketCount: number } | null>(null);
    const usersQuery = useUsers({
        search: search.trim() || undefined,
        role: role || undefined,
        page,
        size: 10,
        sort: "name,asc",
    }, currentUser?.role === "ADMIN");
    const updateRole = useUpdateUserRole();
    const block = useBlockUser();
    const unblock = useUnblockUser();

    if (currentUser?.role !== "ADMIN") {
        return <Navigate to="/tickets" replace />;
    }

    const actionError = updateRole.error ?? block.error ?? unblock.error;
    const isMutating = updateRole.isPending || block.isPending || unblock.isPending;

    function changeRole(user: AdminUser) {
        const nextRole = user.role === "CLIENT" ? "AGENT" : "CLIENT";
        const verb = nextRole === "AGENT" ? "promover para agente" : "rebaixar para cliente";
        if (!window.confirm(`Deseja ${verb} ${user.name}?`)) return;

        setFeedback(null);
        updateRole.reset();
        block.reset();
        unblock.reset();
        updateRole.mutate(
            { userId: user.id, role: nextRole },
            { onSuccess: () => setFeedback(`Perfil de ${user.name} alterado com sucesso.`) },
        );
    }

    function toggleBlocked(user: AdminUser) {
        const label = user.active ? "bloquear" : "desbloquear";
        if (!window.confirm(`Deseja ${label} ${user.name}?`)) return;

        setFeedback(null);
        updateRole.reset();
        block.reset();
        unblock.reset();
        if (!user.active) {
            unblock.mutate(user.id, { onSuccess: () => setFeedback(`${user.name} foi desbloqueado.`) });
            return;
        }

        block.mutate({ userId: user.id }, {
            onSuccess: () => setFeedback(`${user.name} foi bloqueado.`),
            onError: (error) => {
                if (axios.isAxiosError<ActiveTicketsConflictResponse>(error)
                    && error.response?.status === 409
                    && typeof error.response.data.activeTicketCount === "number") {
                    const activeTicketCount = error.response.data.activeTicketCount;
                    block.reset();
                    setBlockConflict({ user, activeTicketCount });
                }
            },
        });
    }

    function confirmAgentBlock(params: BlockUserParams) {
        if (!blockConflict) return;
        block.mutate(params, {
            onSuccess: () => {
                setFeedback(`${blockConflict.user.name} teve os chamados redistribuídos e foi bloqueado.`);
                setBlockConflict(null);
            },
        });
    }

    return (
        <div className="space-y-7">
            {blockConflict && <AgentBlockDialog
                user={blockConflict.user}
                activeTicketCount={blockConflict.activeTicketCount}
                isPending={block.isPending}
                error={block.error}
                onClose={() => { block.reset(); setBlockConflict(null); }}
                onConfirm={confirmAgentBlock}
            />}
            <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#4657a9] via-[#6366c7] to-[#256d85] px-6 py-8 text-white shadow-[0_25px_60px_-30px_#0d121c] sm:px-8">
                <div className="absolute -right-12 -top-20 size-64 rounded-full border-[32px] border-white/5" />
                <div className="relative">
                    <div className="mb-3 flex items-center gap-2 text-xs font-bold uppercase tracking-[0.18em] text-white/75"><ShieldCheck className="size-4" /> Administração</div>
                    <h1 className="text-3xl font-bold tracking-tight">Gestão de usuários</h1>
                    <p className="mt-2 max-w-2xl text-sm text-white/70">Promova agentes, gerencie acessos e acompanhe todos os usuários cadastrados.</p>
                </div>
            </section>

            <section className="rounded-2xl border border-border bg-card/80 p-4 shadow-sm">
                <div className="mb-3 flex items-center gap-2 text-sm font-semibold"><Filter className="size-4 text-primary-strong" /> Filtros</div>
                <div className="grid gap-3 md:grid-cols-[1fr_220px_auto]">
                    <div className="relative">
                        <Search className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                        <Input value={search} onChange={(event) => { setSearch(event.target.value); setPage(0); }} className="pl-10" placeholder="Buscar por nome ou e-mail" />
                    </div>
                    <select value={role} onChange={(event) => { setRole(event.target.value as UserRole | ""); setPage(0); }} className="h-11 rounded-xl border border-input bg-card px-3 text-sm">
                        <option value="">Todas as roles</option>
                        <option value="CLIENT">Clientes</option>
                        <option value="AGENT">Agentes</option>
                        <option value="ADMIN">Administradores</option>
                    </select>
                    <Button variant="outline" disabled={!search && !role} onClick={() => { setSearch(""); setRole(""); setPage(0); }}>Limpar</Button>
                </div>
            </section>

            {feedback && <p role="status" className="rounded-xl border border-emerald-300 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800 dark:border-emerald-800 dark:bg-emerald-950/45 dark:text-emerald-200">{feedback}</p>}
            {actionError && <p role="alert" className="rounded-xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">{getApiErrorMessage(actionError, "Não foi possível atualizar o usuário.")}</p>}

            {usersQuery.isError ? (
                <ErrorState title="Não foi possível carregar os usuários" description={getApiErrorMessage(usersQuery.error, "Tente novamente.")} onRetry={() => usersQuery.refetch()} isRetrying={usersQuery.isFetching} />
            ) : usersQuery.isPending ? (
                <div className="space-y-3">{Array.from({ length: 5 }).map((_, index) => <div key={index} className="h-24 animate-pulse rounded-2xl bg-muted" />)}</div>
            ) : (
                <>
                    <div className="flex items-center justify-between text-sm text-muted-foreground">
                        <span>{usersQuery.data.totalElements} usuário{usersQuery.data.totalElements === 1 ? "" : "s"}</span>
                        <span>Página {usersQuery.data.number + 1} de {Math.max(1, usersQuery.data.totalPages)}</span>
                    </div>
                    <div className="overflow-hidden rounded-2xl border border-border bg-card shadow-sm">
                        <div className="hidden grid-cols-[minmax(180px,1.2fr)_minmax(220px,1.5fr)_130px_110px_260px] gap-4 border-b border-border bg-muted/55 px-5 py-3 text-xs font-bold uppercase tracking-wider text-muted-foreground lg:grid">
                            <span>Usuário</span><span>E-mail</span><span>Role</span><span>Status</span><span className="text-right">Ações</span>
                        </div>
                        {usersQuery.data.content.length === 0 ? <p className="p-10 text-center text-sm text-muted-foreground">Nenhum usuário encontrado.</p> : usersQuery.data.content.map((user) => (
                            <div key={user.id} className="grid gap-4 border-b border-border px-5 py-4 last:border-0 lg:grid-cols-[minmax(180px,1.2fr)_minmax(220px,1.5fr)_130px_110px_260px] lg:items-center">
                                <div className="flex items-center gap-3"><span className="flex size-9 items-center justify-center rounded-full bg-secondary font-bold text-secondary-foreground">{user.name.charAt(0).toUpperCase()}</span><div><p className="font-semibold">{user.name}</p><p className="text-xs text-muted-foreground">ID {user.id}</p></div></div>
                                <p className="break-all text-sm text-muted-foreground">{user.email}</p>
                                <Badge variant="outline" className="w-fit">{roleLabels[user.role]}</Badge>
                                <Badge variant={user.active ? "secondary" : "destructive"} className="w-fit">{user.active ? "Ativo" : "Bloqueado"}</Badge>
                                <div className="flex flex-wrap gap-2 lg:justify-end">
                                    {user.role !== "ADMIN" && <Button size="sm" variant="outline" disabled={isMutating} onClick={() => changeRole(user)}><UserCog /> {user.role === "CLIENT" ? "Tornar agente" : "Tornar cliente"}</Button>}
                                    {user.role !== "ADMIN" && <Button size="sm" variant={user.active ? "destructive" : "secondary"} disabled={isMutating} onClick={() => toggleBlocked(user)}>{user.active ? <UserRoundX /> : <UserRoundCheck />}{user.active ? "Bloquear" : "Desbloquear"}</Button>}
                                </div>
                            </div>
                        ))}
                    </div>
                    {usersQuery.data.totalPages > 1 && <div className="flex items-center justify-between"><Button variant="outline" disabled={page === 0 || usersQuery.isFetching} onClick={() => setPage((value) => value - 1)}>Anterior</Button><Button variant="outline" disabled={page >= usersQuery.data.totalPages - 1 || usersQuery.isFetching} onClick={() => setPage((value) => value + 1)}>Próxima</Button></div>}
                </>
            )}
        </div>
    );
}
