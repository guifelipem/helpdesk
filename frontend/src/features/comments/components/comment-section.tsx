import { useState } from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { useComments, useCreateComment } from "../hooks/use-comments";
import { formatDate } from "@/shared/utils/format-date";

type CommentSectionProps = {
    ticketId: number;
};

export function CommentSection({ ticketId }: CommentSectionProps) {
    const [message, setMessage] = useState("");

    const { data: comments = [], isLoading } = useComments(ticketId);
    const createCommentMutation = useCreateComment(ticketId);

    function handleCreateComment() {
        if (!message.trim()) return;

        createCommentMutation.mutate(
            {
                message,
                isInternal: false,
            },
            {
                onSuccess: () => setMessage(""),
            }
        );
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle>Comentários</CardTitle>
            </CardHeader>

            <CardContent className="space-y-4">
                <div className="space-y-2">
                    <Textarea 
                        placeholder="Escreva um comentário..."
                        value={message}
                        onChange={(event) => setMessage(event.target.value)}
                    />

                    <Button
                        onClick={handleCreateComment}
                        disabled={createCommentMutation.isPending}
                    >
                        {createCommentMutation.isPending ? "Enviando..." : "Enviar comentário"}
                    </Button>
                </div>

                {isLoading ? (
                    <p>Carregando comentários...</p>
                ) : comments.length === 0 ? (
                    <p className="text-sm text-muted-foreground">
                        Nenhum comentário ainda.
                    </p>
                ) : (
                    <div className="space-y-3">
                        {comments.map((comment) => (
                            <div
                                key={comment.id}
                                className="rounded-lg border p-3"
                            >
                                <div className="flex items-center justify-between">
                                    <strong>{comment.author.name}</strong>

                                    <span className="text-xs text-muted-foreground">
                                        {formatDate(comment.createdAt)}
                                    </span>
                                </div>

                                <p className="mt-2 text-sm">
                                    {comment.message}
                                </p>
                            </div>
                        ))}
                    </div>
                )}
            </CardContent>
        </Card>
    );
}