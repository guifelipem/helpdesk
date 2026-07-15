import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";

import { useAssignTickets, useUpdateTicketStatus, useCloseTicket } from "../hooks/use-tickets";
import type { Ticket, TicketStatus } from "../types/ticket.types";

type TicketActionsProps = { ticket: Ticket; };

export function TicketActions({ ticket }: TicketActionsProps) {
    const user = useAuthStore((state) => state.user);

    const assignTicket = useAssignTickets();
    const updateStatus = useUpdateTicketStatus();
    const closeTicket = useCloseTicket();

    const mutationError = assignTicket.error ?? updateStatus.error ?? closeTicket.error;

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

    const isPending = assignTicket.isPending || updateStatus.isPending || closeTicket.isPending;

    if (isClient) {
        if (ticket.status !== "RESOLVED" || !isTicketOwner) {
            return null;
        }

        return (
            <div className="space-y-2">
                <Button onClick={handleClose} disabled={closeTicket.isPending}>
                    {closeTicket.isPending ? "Fechando..." : "Confirmar resolução"}
                </Button>

                {closeTicket.isError && (
                    <p className="text-sm text-destructive">
                        {getApiErrorMessage(
                            closeTicket.error,
                            "Não foi possível fechar o chamado. Tente novamente."
                        )}
                    </p>
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
                            Aguardar cliente
                        </Button>

                        <Button
                            onClick={() => handleStatusChange("RESOLVED")}
                            disabled={isPending}
                        >
                            Marcar como Resolvido
                        </Button>
                    </>
                )}

                {ticket.status === "WAITING_CLIENT" && (
                    <>
                        <Button
                            variant="outline"
                            onClick={() => handleStatusChange("IN_PROGRESS")}
                            disabled={isPending}
                        >
                            Retomar atendimento
                        </Button>

                        <Button
                            onClick={() => handleStatusChange("RESOLVED")}
                            disabled={isPending}
                        >
                            Marcar como Resolvido
                        </Button>
                    </>
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