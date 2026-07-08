import { useAuthStore } from "@/features/auth/store/auth.store";
import { useMyTickets, useTickets } from "@/features/tickets/hooks/useTickets";
import { TicketCard } from "@/features/tickets/components/ticket-card";

export function TicketsPage() {
    const user = useAuthStore((state) => state.user);

    const isClient = user?.role === "CLIENT";

    const allTicketsQuery = useTickets(undefined, {
        enabled: !isClient,
    });

    const myTicketsQuery = useMyTickets({enabled: isClient,});

    const isPending = isClient ? myTicketsQuery.isPending : allTicketsQuery.isPending;

    const isError = isClient ? myTicketsQuery.isError : allTicketsQuery.isError;

    const tickets = isClient ? myTicketsQuery.data : allTicketsQuery.data?.content;

    if (isPending) {
        return <p>Carregando...</p>;
    }

    if (isError) {
        return <p>Erro ao carregar os tickets.</p>;
    }

    if (!tickets || tickets.length === 0) {
        return (
            <div>
                <h1>Tickets</h1>
                <p>
                    {isClient ? "Você ainda não possui tickets." : "Nenhum ticket encontrado."}
                </p>
            </div>
        );
    }

    return (
        <div>
            <div className="grip gap-4">
                {tickets?.map((ticket) => (
                    <TicketCard key={ticket.id} ticket={ticket} />
                ))}
            </div>
        </div>
    );
}