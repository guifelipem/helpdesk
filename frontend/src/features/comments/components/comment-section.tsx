import { useState } from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { useComments, useCreateComment } from "../hooks/use-comments";
import { formatDate } from "@/shared/utils/format-date";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { Label } from "@/components/ui/label";

type CommentSectionProps = {
    ticketId: number;
};

export function CommentSection({ ticketId }: CommentSectionProps) {
    const [message, setMessage] = useState("");

    const user = useAuthStore((state) => state.user);

    const [isInternal, setIsInternal] = useState(false);

    const canCreateInternalComment = user?.role === "AGENT" || user?.role === "ADMIN";

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
                                className={`rounded-lg border p-3 ${comment.isInternal ? "border-amber-300 bg-amber-50" : ""
                                    }`}
                            >
                                <div className="flex items-center gap-2">
                                    <strong>{comment.author.name}</strong>

                                    {comment.isInternal && (
                                        <span className="rounded-full bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-800">
                                            Interno
                                        </span>
                                    )}
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