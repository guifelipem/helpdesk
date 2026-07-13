import { useParams } from "react-router-dom";

import { useTicket } from "@/features/tickets/hooks/use-tickets";
import { TicketDetails } from "@/features/tickets/components/ticket-details";

export function TicketDetailsPage() {
    const { id } = useParams();

    const ticketId = Number(id);

    const {
        data: ticket,
        isLoading,
        isError,
    } = useTicket(ticketId);

    if (isLoading) {
        return <p>Carregando...</p>
    }

    if (isError || !ticket) {
        return <p>Ticket não encontrado.</p>
    }

    return (
        <TicketDetails ticket={ticket} />
    );
}