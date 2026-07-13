import { useAuthStore } from "@/features/auth/store/auth.store";
import { useMyTickets, useTickets } from "@/features/tickets/hooks/use-tickets";
import { TicketCard } from "@/features/tickets/components/ticket-card";
import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";

export function TicketsPage() {
    const user = useAuthStore((state) => state.user);

    const isClient = user?.role === "CLIENT";

    const allTicketsQuery = useTickets(undefined, {
        enabled: !isClient,
    });

    const myTicketsQuery = useMyTickets({ enabled: isClient, });

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
                {isClient && (
                    <Button asChild>
                        <Link to="/tickets/new">Novo Ticket</Link>
                    </Button>
                )}
            </div>
        );
    }

    return (
        <div>
            <div className="flex items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight">Tickets</h1>
                    <p className="text-muted-foreground">
                        {isClient
                            ? "Acompanhe os chamados que você abriu."
                            : "Gerencie os chamados do sistema."
                        }
                    </p>
                </div>

                {isClient && (
                    <Button asChild>
                        <Link to="/tickets/new">Novo Ticket</Link>
                    </Button>
                )}
            </div>
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                {tickets?.map((ticket) => (
                    <TicketCard key={ticket.id} ticket={ticket} />
                ))}
            </div>
        </div>
    );
}