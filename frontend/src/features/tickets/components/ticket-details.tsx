import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

import { formatDate } from "@/shared/utils/format-date";

import type { Ticket } from "../types/ticket.types";
import { TicketPriorityBadge } from "./ticket-priority-badge";
import { TicketStatusBadge } from "./ticket-status-badge";
import { CommentSection } from "@/features/comments/components/comment-section";
import { TicketHistorySection } from "@/features/history/components/ticket-history-section";
import { TicketActions } from "./ticket-actions";

type TicketDetailsProps = { ticket: Ticket; };

type InfoItemProps = {
    label: string;
    value: string;
};

function InfoItem({ label, value }: InfoItemProps) {
    return (
        <div>
            <p className="text-sm text-muted-foreground">{label}</p>
            <p className="font-medium">{value}</p>
        </div>
    )
}

export function TicketDetails({ ticket }: TicketDetailsProps) {
    return (
        <div className="space-y-6">
            <Card>
                <CardHeader>
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                        <div>
                            <p className="text-sm text-muted-foreground">
                                Ticket #{ticket.id}
                            </p>

                            <CardTitle className="mt-1 text-2xl">
                                {ticket.title}
                            </CardTitle>
                        </div>

                        <div className="flex gap-2">
                            <TicketStatusBadge status={ticket.status} />
                            <TicketPriorityBadge priority={ticket.priority} />
                        </div>
                    </div>
                </CardHeader>

                <CardContent className="space-y-6">
                    <div className="grid gap-4 sm:grid-cols-2">
                        <InfoItem label="Criado por" value={ticket.createdBy.name} />

                        <InfoItem
                            label="Responsável"
                            value={ticket.assignedTo?.name ?? "Não atribuído"}
                        />

                        <InfoItem label="Criado em" value={formatDate(ticket.createdAt)} />

                        <InfoItem label="Atualizado em" value={formatDate(ticket.updatedAt)} />
                    </div>

                    <TicketActions ticket={ticket} />
                </CardContent>
            </Card>

            <Card>
                <CardHeader>
                    <CardTitle>Descrição</CardTitle>
                </CardHeader>

                <CardContent>
                    <p className="whitespace-pre-line text-sm leading-6">
                        {ticket.description}
                    </p>
                </CardContent>
            </Card>

            <CommentSection ticketId={ticket.id} />

            <TicketHistorySection ticketId={ticket.id} />
        </div>
    )
}