import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/features/auth/store/auth.store";

import { useAssignTickets, useUpdateTicketStatus } from "../hooks/use-tickets";
import type { Ticket, TicketStatus } from "../types/ticket.types";

type TicketActionsProps = { ticket: Ticket; };

export function TicketActions({ ticket }: TicketActionsProps) {
    const user = useAuthStore((state) => state.user);

    const assignTicket = useAssignTickets();
    const updateStatus = useUpdateTicketStatus();

    const canManageTicket = user?.role === "AGENT" || user?.role === "ADMIN";

    if (!canManageTicket) {
        return null;
    }

    function handleAssign() {
        assignTicket.mutate(ticket.id);
    }

    function handleStatusChange(status: TicketStatus) {
        updateStatus.mutate({
            id: ticket.id,
            data: { status },            
        });
    }

    const isPending = assignTicket.isPending || updateStatus.isPending;

    return(
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

            {ticket.status === "RESOLVED" && (
                <Button
                    onClick={() => handleStatusChange("CLOSED")}
                    disabled={isPending}
                >
                    {updateStatus.isPending ? "Fechando..." : "Fechar chamado"}
                </Button>
            )}
        </div>
    )
}