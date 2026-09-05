import { useState, type FormEvent } from "react";

import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";

import { useAssignTicket, useUpdateTicketStatus, useCloseTicket, useRejectTicketResolution, useSendTicketToAgent } from "../hooks/use-tickets";
import type { Ticket, TicketStatus } from "../types/ticket.types";
import { Undo2 } from "lucide-react";

type TicketActionsProps = { ticket: Ticket; };

export function TicketActions({ ticket }: TicketActionsProps) {
    const [isRejectingResolution, setIsRejectingResolution] = useState(false);
    const [rejectionReason, setRejectionReason] = useState("");

    const user = useAuthStore((state) => state.user);

    const assignTicket = useAssignTicket();
    const updateStatus = useUpdateTicketStatus();
    const closeTicket = useCloseTicket();
    const rejectResolution = useRejectTicketResolution();
    const sendToAgent = useSendTicketToAgent();

    const mutationError = assignTicket.error ?? updateStatus.error ?? closeTicket.error ?? rejectResolution.error ?? sendToAgent.error;

    const errorMessage = getApiErrorMessage(
        mutationError,
        "Não foi possível realizar a ação. Tente novamente."
    );

    const isClient = user?.role === "CLIENT";
    const canManageTicket = user?.role === "AGENT" || user?.role === "ADMIN";

    const isTicketOwner = user?.id === ticket.createdBy.id;

    function handleAssign() {
        assignTicket.mutate(ticket.id);
    }

    function handleStatusChange(status: TicketStatus) {
        updateStatus.mutate({
            id: ticket.id,
            data: { status },
        });
    }

    function handleClose() {
        closeTicket.mutate(ticket.id);
    }

    function handleSendToAgent() {
        const confirmed = window.confirm(
            "Você confirma que já enviou as informações solicitadas? O chamado será devolvido para a fila do agente responsável."
        );

        if (confirmed) {
            sendToAgent.mutate(ticket.id);
        }
    }

    function handleRejectResolution(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        const reason = rejectionReason.trim();

        if (!reason) return;

        rejectResolution.mutate(
            {
                id: ticket.id,
                data: { reason },
            },
            {
                onSuccess: () => {
                    setRejectionReason("");
                    setIsRejectingResolution(false);
                },
            }
        );
    }

    function handleCancelRejection() {
        setRejectionReason("");
        setIsRejectingResolution(false);
        rejectResolution.reset();
    }

    const isPending = assignTicket.isPending || updateStatus.isPending || closeTicket.isPending || rejectResolution.isPending || sendToAgent.isPending;

    if (isClient) {
        if (!isTicketOwner) {
            return null;
        }

        if (ticket.status === "WAITING_CLIENT") {
            return (
                <div className="space-y-2">
                    <Button onClick={handleSendToAgent} disabled={sendToAgent.isPending}>
                        {sendToAgent.isPending ? "Enviando..." : "Enviar para análise do suporte"}
                    </Button>

                    {sendToAgent.isError && (
                        <p role="alert" className="text-sm text-red-200">
                            {getApiErrorMessage(
                                sendToAgent.error,
                                "Não foi possível enviar o chamado para análise do suporte. Tente novamente."
                            )}
                        </p>
                    )}
                </div>
            );
        }

        if (ticket.status !== "RESOLVED") {
            return null;
        }

        return (
            <div className="space-y-4">
                <div className="flex flex-wrap gap-2">
                    <Button onClick={handleClose} disabled={isPending}>
                        {closeTicket.isPending ? "Fechando..." : "Confirmar resolução"}
                    </Button>

                    <Button
                        variant="destructive"
                        onClick={() => setIsRejectingResolution(true)}
                        disabled={isPending || isRejectingResolution}
                    >
                        <Undo2 /> Rejeitar resolução
                    </Button>
                </div>

                {closeTicket.isError && (
                    <p className="text-sm text-destructive">
                        {getApiErrorMessage(
                            closeTicket.error,
                            "Não foi possível fechar o chamado. Tente novamente."
                        )}
                    </p>
                )}

                {isRejectingResolution && (
                    <form
                        onSubmit={handleRejectResolution}
                        className="max-w-2xl space-y-3 rounded-xl border border-red-200 bg-white/95 p-4 text-[#301c41]"
                    >
                        <div className="space-y-1">
                            <Label htmlFor="rejection-reason">Por que a resolução não resolveu o problema?</Label>
                            <p className="text-xs text-muted-foreground">
                                A justificativa será registrada no histórico e o chamado voltará para atendimento.
                            </p>
                        </div>

                        <Textarea
                            id="rejection-reason"
                            autoFocus
                            required
                            placeholder="Ex: O problema ainda acontece após seguir as orientações."
                            value={rejectionReason}
                            onChange={(event) => setRejectionReason(event.target.value)}
                            disabled={rejectResolution.isPending}
                            className="min-h-24 resize-y"
                            aria-describedby={rejectResolution.isError ? "rejection-error" : undefined}
                        />

                        <div className="flex flex-wrap gap-2">
                            <Button
                                type="submit"
                                variant="destructive"
                                disabled={rejectResolution.isPending || !rejectionReason.trim()}
                            >
                                <Undo2 />
                                {rejectResolution.isPending ? "Rejeitando..." : "Confirmar rejeição"}
                            </Button>

                            <Button
                                type="button"
                                variant="outline"
                                onClick={handleCancelRejection}
                                disabled={rejectResolution.isPending}
                            >
                                Cancelar
                            </Button>
                        </div>

                        {rejectResolution.isError && (
                            <p id="rejection-error" role="alert" className="text-sm text-destructive">
                                {getApiErrorMessage(
                                    rejectResolution.error,
                                    "Não foi possível rejeitar a resolução. Tente novamente."
                                )}
                            </p>
                        )}
                    </form>
                )}
            </div>
        );
    }

    if (!canManageTicket) {
        return null;
    }

    return (
        <div className="space-y-2">
            <div className="flex flex-wrap gap-2">
                {ticket.status === "OPEN" && !ticket.assignedTo && (
                    <Button onClick={handleAssign} disabled={isPending}>
                        {assignTicket.isPending ? "Assumindo..." : "Assumir chamado"}
                    </Button>
                )}

                {ticket.status === "IN_PROGRESS" && (
                    <>
                        <Button
                            variant="outline"
                            onClick={() => handleStatusChange("WAITING_CLIENT")}
                            disabled={isPending}
                        >
                            Aguardar resposta do cliente
                        </Button>

                        <Button
                            onClick={() => handleStatusChange("RESOLVED")}
                            disabled={isPending}
                        >
                            Resolver chamado
                        </Button>
                    </>
                )}

                {ticket.status === "WAITING_CLIENT" && (
                    <Button
                        variant="outline"
                        onClick={() => handleStatusChange("IN_PROGRESS")}
                        disabled={isPending}
                    >
                        Retomar atendimento
                    </Button>
                )}

                {ticket.status === "WAITING_AGENT" && (
                    <Button
                        onClick={() => handleStatusChange("IN_PROGRESS")}
                        disabled={isPending}
                    >
                        Retomar atendimento
                    </Button>
                )}
            </div>

            {mutationError && (
                <p className="text-sm text-destructive">
                    {errorMessage}
                </p>
            )}
        </div>
    )
}
