import { Link } from "react-router-dom";

import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import type { Ticket } from "../types/ticket.types";
import { formatDate } from "@/shared/utils/format-date";
import { TicketPriorityBadge } from "./ticket-priority-badge";
import { TicketStatusBadge } from "./ticket-status-badge";
import { ArrowUpRight, CalendarDays, Ticket as TicketIcon } from "lucide-react";

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
        <Card className="transition-all duration-300 hover:-translate-y-1 hover:border-[#6f95ff]/25 hover:shadow-[0_22px_55px_-28px_#413b6b80]">
            <CardHeader>
                <div className="mb-2 flex items-center justify-between">
                    <div className="flex size-9 items-center justify-center rounded-xl bg-[#ececff] text-[#5c65c0]"><TicketIcon className="size-4" /></div>
                    <span className="text-xs font-bold tracking-wider text-[#413b6b]/45">#{ticket.id}</span>
                </div>
                <CardTitle className="text-lg font-bold tracking-tight text-[#301c41]">{ticket.title}</CardTitle>
            </CardHeader>

            <CardContent className="space-y-4">
                <p className="min-h-10 text-sm leading-6 text-muted-foreground">
                    {truncateText(ticket.description)}
                </p>

                <div className="flex gap-2">
                    <TicketStatusBadge status={ticket.status} />

                    <TicketPriorityBadge priority={ticket.priority} />
                </div>

                <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
                    <CalendarDays className="size-3.5" /> Criado em{" "}
                    {formatDate(ticket.createdAt)}
                </p>
            </CardContent>

            <CardFooter className="justify-end border-[#413b6b]/8 bg-[#f8f8ff]">
                <Button asChild variant="ghost" size="sm" className="text-[#5c65c0]">
                    <Link to={`/tickets/${ticket.id}`}>Ver detalhes <ArrowUpRight /></Link>
                </Button>
            </CardFooter>
        </Card>
    )
}
