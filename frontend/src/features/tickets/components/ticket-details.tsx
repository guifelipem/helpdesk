import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";

import { formatDate } from "@/shared/utils/format-date";

import type { Ticket } from "../types/ticket.types";
import { TicketPriorityBadge } from "./ticket-priority-badge";
import { TicketStatusBadge } from "./ticket-status-badge";
import { CommentSection } from "@/features/comments/components/comment-section";
import { TicketHistorySection } from "@/features/history/components/ticket-history-section";
import { TicketActions } from "./ticket-actions";
import { AlignLeft, ArrowLeft, CalendarDays, CircleUserRound, UserCheck } from "lucide-react";

type TicketDetailsProps = { ticket: Ticket; };

type InfoItemProps = {
    label: string;
    value: string;
    icon?: ReactNode;
};

function InfoItem({ label, value, icon }: InfoItemProps) {
    return (
        <div className="rounded-xl border border-[#413b6b]/8 bg-[#f8f8ff] p-4">
            <div className="mb-1.5 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-[#5c65c0]">{icon}{label}</div>
            <p className="font-semibold text-[#301c41]">{value}</p>
        </div>
    )
}

export function TicketDetails({ ticket }: TicketDetailsProps) {
    return (
        <div className="space-y-6">
            <Button asChild variant="outline">
                <Link to="/tickets"><ArrowLeft /> Voltar para chamados</Link>
            </Button>

            <Card className="relative overflow-hidden border-0 bg-gradient-to-br from-[#301c41] via-[#413b6b] to-[#5c65c0] text-white ring-0">
                <div className="absolute -right-16 -top-20 size-64 rounded-full border-[34px] border-white/5" />
                <CardHeader className="relative">
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                        <div>
                            <p className="text-xs font-bold uppercase tracking-[0.18em] text-[#aebfff]">
                                Chamado #{ticket.id}
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

                <CardContent className="relative space-y-6">
                    <div className="grid gap-4 sm:grid-cols-2">
                        <InfoItem label="Criado por" value={ticket.createdBy.name} icon={<CircleUserRound className="size-3.5" />} />

                        <InfoItem
                            label="Responsável"
                            value={ticket.assignedTo?.name ?? "Não atribuído"}
                            icon={<UserCheck className="size-3.5" />}
                        />

                        <InfoItem label="Criado em" value={formatDate(ticket.createdAt)} icon={<CalendarDays className="size-3.5" />} />

                        <InfoItem label="Atualizado em" value={formatDate(ticket.updatedAt)} icon={<CalendarDays className="size-3.5" />} />
                    </div>

                    <TicketActions ticket={ticket} />
                </CardContent>
            </Card>

            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-lg font-bold"><span className="flex size-8 items-center justify-center rounded-lg bg-[#ececff] text-[#5c65c0]"><AlignLeft className="size-4" /></span>Descrição</CardTitle>
                </CardHeader>

                <CardContent>
                    <p className="whitespace-pre-line rounded-xl bg-[#f8f8ff] p-4 text-sm leading-7 text-[#413b6b]">
                        {ticket.description}
                    </p>
                </CardContent>
            </Card>

            <CommentSection ticketId={ticket.id} ticketStatus={ticket.status}/>

            <TicketHistorySection ticketId={ticket.id} />
        </div>
    )
}
