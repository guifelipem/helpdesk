import { useEffect, useState } from "react";
import { ArrowRightLeft, RotateCcw, TriangleAlert, X } from "lucide-react";

import { Button } from "@/components/ui/button";
import { useActiveAgents } from "@/features/users/hooks/use-users";
import type { AdminUser, AgentBlockAction, BlockUserParams } from "@/features/users/types/admin-user.types";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";

type AgentBlockDialogProps = {
    user: AdminUser;
    activeTicketCount: number;
    isPending: boolean;
    error: unknown;
    onClose: () => void;
    onConfirm: (params: BlockUserParams) => void;
};

export function AgentBlockDialog({
    user,
    activeTicketCount,
    isPending,
    error,
    onClose,
    onConfirm,
}: AgentBlockDialogProps) {
    const [action, setAction] = useState<AgentBlockAction>("TRANSFER");
    const [targetAgentId, setTargetAgentId] = useState("");
    const agentsQuery = useActiveAgents();
    const availableAgents = (agentsQuery.data ?? []).filter((agent) => agent.id !== user.id);
    const canConfirm = action === "RETURN_TO_QUEUE" || targetAgentId !== "";

    useEffect(() => {
        function closeOnEscape(event: KeyboardEvent) {
            if (event.key === "Escape" && !isPending) onClose();
        }
        document.addEventListener("keydown", closeOnEscape);
        return () => document.removeEventListener("keydown", closeOnEscape);
    }, [isPending, onClose]);

    function confirm() {
        if (!canConfirm) return;
        onConfirm({
            userId: user.id,
            action,
            targetAgentId: action === "TRANSFER" ? Number(targetAgentId) : undefined,
        });
    }

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/65 p-4 backdrop-blur-sm" onMouseDown={(event) => {
            if (event.target === event.currentTarget && !isPending) onClose();
        }}>
            <section role="dialog" aria-modal="true" aria-labelledby="agent-block-title" className="w-full max-w-xl rounded-3xl border border-border bg-card p-6 shadow-2xl sm:p-7">
                <div className="flex items-start justify-between gap-4">
                    <div className="flex gap-3">
                        <span className="flex size-11 shrink-0 items-center justify-center rounded-2xl bg-amber-100 text-amber-700 dark:bg-amber-950/60 dark:text-amber-300"><TriangleAlert className="size-5" /></span>
                        <div>
                            <h2 id="agent-block-title" className="text-xl font-bold">Redistribuir chamados antes do bloqueio</h2>
                            <p className="mt-2 text-sm leading-6 text-muted-foreground">
                                <strong className="text-foreground">{user.name}</strong> possui {activeTicketCount} chamado{activeTicketCount === 1 ? "" : "s"} não fechado{activeTicketCount === 1 ? "" : "s"}. Escolha o destino antes de bloquear o acesso.
                            </p>
                        </div>
                    </div>
                    <Button type="button" size="icon" variant="ghost" aria-label="Fechar" disabled={isPending} onClick={onClose}><X /></Button>
                </div>

                <div className="mt-6 grid gap-3 sm:grid-cols-2">
                    <button type="button" onClick={() => setAction("TRANSFER")} disabled={isPending} className={`rounded-2xl border p-4 text-left transition ${action === "TRANSFER" ? "border-primary bg-secondary/70 ring-2 ring-primary/20" : "border-border hover:bg-muted/60"}`}>
                        <ArrowRightLeft className="mb-3 size-5 text-primary-strong" />
                        <span className="block font-semibold">Transferir chamados</span>
                        <span className="mt-1 block text-xs leading-5 text-muted-foreground">Mantém o status atual, inclusive dos chamados resolvidos.</span>
                    </button>
                    <button type="button" onClick={() => setAction("RETURN_TO_QUEUE")} disabled={isPending} className={`rounded-2xl border p-4 text-left transition ${action === "RETURN_TO_QUEUE" ? "border-primary bg-secondary/70 ring-2 ring-primary/20" : "border-border hover:bg-muted/60"}`}>
                        <RotateCcw className="mb-3 size-5 text-primary-strong" />
                        <span className="block font-semibold">Devolver à fila</span>
                        <span className="mt-1 block text-xs leading-5 text-muted-foreground">Remove o responsável e disponibiliza os chamados novamente.</span>
                    </button>
                </div>

                {action === "TRANSFER" && (
                    <div className="mt-5">
                        <label htmlFor="block-target-agent" className="mb-2 block text-sm font-semibold">Agente de destino</label>
                        <select id="block-target-agent" autoFocus value={targetAgentId} onChange={(event) => setTargetAgentId(event.target.value)} disabled={agentsQuery.isPending || isPending} className="h-11 w-full rounded-xl border border-input bg-card px-3 text-sm outline-none focus:border-ring focus:ring-3 focus:ring-ring/20">
                            <option value="">{agentsQuery.isPending ? "Carregando agentes ativos..." : "Selecione um agente ativo"}</option>
                            {availableAgents.map((agent) => <option key={agent.id} value={agent.id}>{agent.name} — {agent.email}</option>)}
                        </select>
                        {!agentsQuery.isPending && availableAgents.length === 0 && <p className="mt-2 text-sm text-amber-700 dark:text-amber-300">Não há outro agente ativo disponível. Use “Devolver à fila”.</p>}
                        {agentsQuery.isError && <p role="alert" className="mt-2 text-sm text-destructive">Não foi possível carregar os agentes ativos.</p>}
                    </div>
                )}

                {Boolean(error) && <p role="alert" className="mt-4 rounded-xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">{getApiErrorMessage(error, "Não foi possível concluir o bloqueio.")}</p>}

                <div className="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
                    <Button type="button" variant="outline" disabled={isPending} onClick={onClose}>Cancelar</Button>
                    <Button type="button" variant="destructive" disabled={!canConfirm || isPending || (action === "TRANSFER" && agentsQuery.isPending)} onClick={confirm}>
                        {isPending ? "Redistribuindo e bloqueando..." : "Confirmar bloqueio"}
                    </Button>
                </div>
            </section>
        </div>
    );
}
