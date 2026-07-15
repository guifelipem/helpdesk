import { Link } from "react-router-dom";

import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import type { Ticket } from "../types/ticket.types";
import { formatDate } from "@/shared/utils/format-date";
import { TicketPriorityBadge } from "./ticket-priority-badge";
import { TicketStatusBadge } from "./ticket-status-badge";

type TicketCardProps = {
    ticket: Ticket;
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

                <div className="flex gap-2">
                    <TicketStatusBadge status={ticket.status} />

                    <TicketPriorityBadge priority={ticket.priority} />
                </div>

                <p className="text-xs text-muted-foreground">
                    Criado em{" "}
                    {formatDate(ticket.createdAt)}
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