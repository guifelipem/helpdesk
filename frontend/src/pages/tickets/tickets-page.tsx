import { useTickets } from "@/features/tickets/hooks/useTickets";

export function TicketsPage() {
    const { data, isPending, isError } = useTickets();

    if (isPending) {
        return <p>Carregando...</p>;
    }

    if (isError) {
        return <p>Erro ao carregar tickets.</p>;
    }

    return (
        <div>
            <h1>Tickets</h1>

            {data?.content.map((ticket) => (
                <div key={ticket.id}>
                    <h3>{ticket.title}</h3>
                    <p>{ticket.status}</p>
                    <p>{ticket.priority}</p>
                </div>
            ))}
        </div>
    );
}