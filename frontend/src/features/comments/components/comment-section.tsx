import { useState } from "react";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { formatDate } from "@/shared/utils/format-date";
import { ErrorState } from "@/shared/components/error-state";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";
import { useComments, useCreateComment } from "../hooks/use-comments";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { Label } from "@/components/ui/label";
import type { TicketStatus } from "@/features/tickets/types/ticket.types";
import { MessageSquareText, Send } from "lucide-react";

type CommentSectionProps = {
    ticketId: number;
    ticketStatus: TicketStatus;
};

export function CommentSection({ ticketId, ticketStatus }: CommentSectionProps) {
    const [message, setMessage] = useState("");

    const user = useAuthStore((state) => state.user);

    const [isInternal, setIsInternal] = useState(false);

    const canCreateInternalComment = user?.role === "AGENT" || user?.role === "ADMIN";

    const isTicketReadOnly = ticketStatus === "RESOLVED" || ticketStatus === "CLOSED";

    const { data: comments = [], isLoading, isError, error, refetch, isFetching, } = useComments(ticketId);
    const createCommentMutation = useCreateComment(ticketId);

    const commentsErrorMessage = getApiErrorMessage(
        error,
        "Não foi possível carregar os comentários. Tente novamente"
    );

    const createCommentErrorMessage = getApiErrorMessage(
        createCommentMutation.error,
        "Não foi possível enviar o comentário. Tente novamente."
    );

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
                <CardTitle className="flex items-center gap-2 text-lg font-bold"><span className="flex size-8 items-center justify-center rounded-lg bg-[#ececff] text-[#5c65c0]"><MessageSquareText className="size-4" /></span>Comentários</CardTitle>
            </CardHeader>

            <CardContent className="space-y-6">
                {isLoading ? (
                    <p className="text-sm text-muted-foreground">
                        Carregando comentários...
                    </p>
                ) : isError ? (
                    <ErrorState
                        title="Não foi possível carregar os comentários"
                        description={commentsErrorMessage}
                        onRetry={() => refetch()}
                        isRetrying={isFetching}
                    />
                ) : comments.length === 0 ? (
                    <p className="text-sm text-muted-foreground">
                        Nenhum comentário ainda.
                    </p>
                ) : (
                    <div className="space-y-3">
                        <div className="flex items-center justify-between">
                            <p className="text-sm font-semibold text-[#301c41]">Conversa</p>
                            <span className="rounded-full bg-[#ececff] px-2.5 py-1 text-xs font-semibold text-[#5c65c0]">
                                {comments.length} {comments.length === 1 ? "mensagem" : "mensagens"}
                            </span>
                        </div>
                        {comments.map((comment) => {
                            const isSupport = comment.author.role === "AGENT" || comment.author.role === "ADMIN";

                            return (
                                <div
                                    key={comment.id}
                                    className={`rounded-xl border p-4 shadow-[0_8px_24px_-22px_#301c41] ${comment.isInternal
                                        ? "border-amber-300 bg-amber-50"
                                        : isSupport
                                            ? "border-[#6f95ff]/20 border-l-[3px] border-l-[#6f95ff] bg-[#f4f6ff]"
                                            : "bg-white"
                                        }`}
                                >
                                    <div className="flex items-start justify-between gap-3">
                                        <div className="flex flex-wrap items-center gap-2">
                                            <div className={`flex size-8 items-center justify-center rounded-full text-xs font-bold ${isSupport ? "bg-[#5c65c0] text-white" : "bg-[#ececff] text-[#413b6b]"}`}>
                                                {comment.author.name.charAt(0).toUpperCase()}
                                            </div>

                                            <strong className="text-sm">{comment.author.name}</strong>

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

                                    <p className="mt-3 pl-10 text-sm leading-6 text-[#413b6b]">
                                        {comment.message}
                                    </p>
                                </div>
                            )
                        })}
                    </div>
                )}

                {isTicketReadOnly ? (
                    <p className="rounded-xl border bg-muted/40 p-3 text-sm text-muted-foreground">
                        {ticketStatus === "RESOLVED"
                            ? "Este chamado aguarda a confirmação da resolução e não aceita novos comentários. Confirme ou rejeite a resolução acima."
                            : "Este chamado foi encerrado e não aceita novos comentários."}
                    </p>
                ) : (
                    <div className="space-y-3 border-t border-[#413b6b]/10 pt-5">
                        <Label htmlFor="comment-message" className="text-[#301c41]">Adicionar comentário</Label>

                        <Textarea
                            id="comment-message"
                            placeholder="Escreva uma resposta..."
                            value={message}
                            onChange={(event) => setMessage(event.target.value)}
                            className="min-h-20 max-h-40 resize-y bg-[#f8f8ff]"
                            aria-describedby={createCommentMutation.error ? "comment-error" : undefined}
                        />

                        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                            {canCreateInternalComment ? (
                                <div className="flex items-center gap-2">
                                    <input
                                        id="internal-comment"
                                        type="checkbox"
                                        checked={isInternal}
                                        onChange={(event) => setIsInternal(event.target.checked)}
                                        className="size-4 accent-[#5c65c0]"
                                    />

                                    <Label htmlFor="internal-comment">Comentário interno</Label>
                                </div>
                            ) : <span />}

                            <Button
                                onClick={handleCreateComment}
                                disabled={createCommentMutation.isPending || !message.trim()}
                            >
                                <Send /> {createCommentMutation.isPending ? "Enviando..." : "Enviar comentário"}
                            </Button>
                        </div>

                        {createCommentMutation.error && (
                            <p id="comment-error" role="alert" className="text-sm text-destructive">{createCommentErrorMessage}</p>
                        )}
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
