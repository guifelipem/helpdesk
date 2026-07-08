import { Link } from "react-router-dom";

import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import type { Ticket } from "../types/ticket.types";

type TicketCardProps = {
    ticket: Ticket;
};

const statusLabels = {
    OPEN: "Aberto",
    IN_PROGRESS: "Em andamento",
    WAITING_CLIENT: "Aguardando cliente",
    RESOLVED: "Resolvido",
    CLOSED: "Fechado",
};

const priorityLabels = {
    LOW: "Baixa",
    MEDIUM: "Média",
    HIGH: "Alta",
    CRITICAL: "Crítica",
};

function truncateText(text: string, maxLength = 120) {
    if (text.length <= maxLength) {
        return text;
    }

    return `${text.slice(0, maxLength)}...`;
}

export function TicketCard({ ticket }: TicketCardProps) {
    return (
        <Card>
            <CardHeader>
                <CardTitle>{ticket.title}</CardTitle>
            </CardHeader>

            <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground">
                    {truncateText(ticket.description)}
                </p>

                <div className="flex flex-wrap gap-2">
                    <span className="rounded-full bg-muted px-3 py-1 text-xs font-medium">
                        {statusLabels[ticket.status]}
                    </span>

                    <span className="rounded-full bg-muted px-3 py-1 text-xs font-medium">
                        {priorityLabels[ticket.priority]}
                    </span>
                </div>

                <p className="text-xs text-muted-foreground">
                    Criado em{" "}
                    {new Date(ticket.createdAt).toLocaleDateString("pt-br")}
                </p>
            </CardContent>

            <CardFooter className="justify-end">
                <Button asChild variant="outline" size="sm">
                    <Link to={`/tickets/${ticket.id}`}>Ver detalhes</Link>
                </Button>
            </CardFooter>
        </Card>
    )
}