import { Link } from "react-router-dom";

import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import type { Ticket } from "../types/ticket.types";
import { formatDate } from "@/shared/utils/format-date";
import { TicketPriorityBadge } from "./ticket-priority-badge";
import { TicketStatusBadge } from "./ticket-status-badge";
import { ArrowUpRight, CalendarDays, Ticket as TicketIcon } from "lucide-react";
import { useAuthStore } from "@/features/auth/store/auth.store";

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
    const user = useAuthStore((state) => state.user);
    const isClientWaiting = user?.role === "CLIENT" && ticket.status === "WAITING_CLIENT";

    return (
        <Card className="transition-all duration-300 hover:-translate-y-1 hover:border-primary/40 hover:shadow-[0_22px_55px_-28px_#4794b880]">
            <CardHeader>
                <div className="mb-2 flex items-center justify-between">
                    <div className="flex size-9 items-center justify-center rounded-xl bg-primary/15 text-primary-strong"><TicketIcon className="size-4" /></div>
                    <span className="text-xs font-bold tracking-wider text-muted-foreground">#{ticket.id}</span>
                </div>
                <CardTitle className="text-lg font-bold tracking-tight text-foreground">{ticket.title}</CardTitle>
            </CardHeader>

            <CardContent className="space-y-4">
                <p className="min-h-10 text-sm leading-6 text-muted-foreground">
                    {truncateText(ticket.description)}
                </p>

                <div className="flex gap-2">
                    <TicketStatusBadge status={ticket.status} />

                    <TicketPriorityBadge priority={ticket.priority} />
                </div>

                {ticket.status === "WAITING_AGENT" && (
                    <p className="rounded-lg border border-sky-300 bg-sky-50 px-3 py-2 text-xs font-semibold text-sky-900 dark:border-sky-700 dark:bg-sky-950/55 dark:text-sky-200">
                        O cliente já respondeu. Este chamado precisa ser retomado pelo suporte.
                    </p>
                )}

                {isClientWaiting && (
                    <p className="rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-xs font-semibold text-amber-900 dark:border-amber-700 dark:bg-amber-950/55 dark:text-amber-200">
                        O Suporte já respondeu. Este chamado precisa ser respondido por você.
                    </p>
                )}

                <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
                    <CalendarDays className="size-3.5" /> Criado em{" "}
                    {formatDate(ticket.createdAt)}
                </p>
            </CardContent>

            <CardFooter className="justify-end border-border bg-muted/45">
                <Button asChild variant="ghost" size="sm" className="text-primary-strong">
                    <Link to={`/tickets/${ticket.id}`}>Ver detalhes <ArrowUpRight /></Link>
                </Button>
            </CardFooter>
        </Card>
    )
}
