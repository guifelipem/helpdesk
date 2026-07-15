import { useState } from "react";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { formatDate } from "@/shared/utils/format-date";
import { useComments, useCreateComment } from "../hooks/use-comments";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { Label } from "@/components/ui/label";
import type { TicketStatus } from "@/features/tickets/types/ticket.types";

type CommentSectionProps = {
    ticketId: number;
    ticketStatus: TicketStatus;
};

export function CommentSection({ ticketId, ticketStatus }: CommentSectionProps) {
    const [message, setMessage] = useState("");

    const user = useAuthStore((state) => state.user);

    const [isInternal, setIsInternal] = useState(false);

    const canCreateInternalComment = user?.role === "AGENT" || user?.role === "ADMIN";

    const isTicketClosed = ticketStatus === "CLOSED";

    const { data: comments = [], isLoading } = useComments(ticketId);
    const createCommentMutation = useCreateComment(ticketId);

    function handleCreateComment() {
        if (!message.trim()) return;

        createCommentMutation.mutate(
            {
                message,
                isInternal,
            },
            {
                onSuccess: () => {
                    setMessage("");
                    setIsInternal(false);
                },
            }
        );
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle>Comentários</CardTitle>
            </CardHeader>

            <CardContent className="space-y-4">

                {isTicketClosed ? (
                    <p className="rounded-md border bg-muted/40 p-3 text-sm text-muted-foreground">
                        Este chamado foi encerrado e não aceita novos comentários.
                    </p>
                ) : (
                    <div className="space-y-2">
                        <Textarea
                            placeholder="Escreva um comentário..."
                            value={message}
                            onChange={(event) => setMessage(event.target.value)}
                        />

                        {canCreateInternalComment && (
                            <div className="flex items-center gap-2">
                                <input
                                    id="internal-comment"
                                    type="checkbox"
                                    checked={isInternal}
                                    onChange={(event) => setIsInternal(event.target.checked)}
                                    className="size-4"
                                />

                                <Label htmlFor="internal-comment">
                                    Comentário interno
                                </Label>
                            </div>
                        )}

                        <Button
                            onClick={handleCreateComment}
                            disabled={createCommentMutation.isPending}
                        >
                            {createCommentMutation.isPending ? "Enviando..." : "Enviar comentário"}
                        </Button>
                    </div>
                )}

                {isLoading ? (
                    <p>Carregando comentários...</p>
                ) : comments.length === 0 ? (
                    <p className="text-sm text-muted-foreground">
                        Nenhum comentário ainda.
                    </p>
                ) : (
                    <div className="space-y-3">
                        {comments.map((comment) => {
                            const isSupport = comment.author.role === "AGENT" || comment.author.role === "ADMIN";

                            return (
                                <div
                                    key={comment.id}
                                    className={`rounded-lg border p-3 ${comment.isInternal
                                        ? "border-amber-300 bg-amber-50"
                                        : isSupport
                                            ? "bg-muted/40"
                                            : "bg-background"
                                        }`}
                                >
                                    <div className="flex items-start justify-between gap-3">
                                        <div className="flex flex-wrap items-center gap-2">
                                            <strong className="text-sm">
                                                {comment.author.name}
                                            </strong>

                                            <Badge variant={isSupport ? "default" : "secondary"}>
                                                {isSupport ? "Equipe" : "Cliente"}
                                            </Badge>

                                            {comment.isInternal && (
                                                <Badge
                                                    variant="outline"
                                                    className="border-amber-300 bg-amber-100 text-amber-800"
                                                >
                                                    Interno
                                                </Badge>
                                            )}
                                        </div>

                                        <span className="shrink-0 text-xs text-muted-foreground">
                                            {formatDate(comment.createdAt)}
                                        </span>
                                    </div>

                                    <p className="mt-2 text-sm">
                                        {comment.message}
                                    </p>
                                </div>
                            )
                        })}
                    </div>
                )}
            </CardContent>
        </Card>
    );
}