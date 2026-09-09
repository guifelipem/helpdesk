import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import type { ReactNode } from "react";
import { useLocation, useNavigate } from "react-router-dom";

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
        <div className="rounded-xl border border-border bg-card/65 p-4 dark:bg-black/15">
            <div className="mb-1.5 flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-primary-strong">{icon}{label}</div>
            <p className="font-semibold text-foreground dark:text-white">{value}</p>
        </div>
    )
}

export function TicketDetails({ ticket }: TicketDetailsProps) {
    const location = useLocation();
    const navigate = useNavigate();

    function handleBack() {
        if (location.key === "default") {
            navigate("/tickets", { replace: true });
            return;
        }

        navigate(-1);
    }

    return (
        <div className="space-y-6">
            <Button type="button" variant="outline" onClick={handleBack}>
                <ArrowLeft /> Voltar
            </Button>

            <Card className="relative overflow-hidden border-0 bg-gradient-to-br from-[#4657a9] via-[#6366c7] to-[#256d85] text-white ring-0">
                <div className="absolute -right-16 -top-20 size-64 rounded-full border-[34px] border-white/5" />
                <CardHeader className="relative">
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                        <div>
                            <p className="text-xs font-bold uppercase tracking-[0.18em] text-white/75">
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
                    <CardTitle className="flex items-center gap-2 text-lg font-bold"><span className="flex size-8 items-center justify-center rounded-lg bg-primary/15 text-primary-strong"><AlignLeft className="size-4" /></span>Descrição</CardTitle>
                </CardHeader>

                <CardContent>
                    <p className="whitespace-pre-line rounded-xl bg-muted/60 p-4 text-sm leading-7 text-foreground">
                        {ticket.description}
                    </p>
                </CardContent>
            </Card>

            <CommentSection ticketId={ticket.id} ticketStatus={ticket.status}/>

            <TicketHistorySection ticketId={ticket.id} />
        </div>
    )
}
