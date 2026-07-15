import { useTicketHistory } from "../hooks/use-ticket-history";
import { formatDate } from "@/shared/utils/format-date";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { History } from "lucide-react";
import { getHistoryDescription, getHistoryTitle, getHistoryIcon } from "../utils/format-ticket-history";
import { Button } from "@/components/ui/button";

type TicketHistorySectionProps = {
    ticketId: number;
};

export function TicketHistorySection({ ticketId }: TicketHistorySectionProps) {
    const {
        data: history = [],
        isLoading,
        isError,
        refetch,
    } = useTicketHistory(ticketId);

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <History className="size-5" />
                    Histórico
                </CardTitle>
            </CardHeader>

            <CardContent>
                {isLoading && (
                    <div className="space-y-5">
                        {Array.from({ length: 3 }).map((_, index) => (
                            <div key={index} className="flex gap-4">
                                <div className="size-6 shrink-0 animate-pulse rounded-full bg-muted" />

                                <div className="flex-1 space-y-2">
                                    <div className="h-4 w-40 animate-pulse rounded bg-muted" />
                                    <div className="h-3 w-full animate-pulse rounded bg-muted" />
                                    <div className="h-3 w-28 animate-pulse rounded bg-muted" />
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {isError && (
                    <div className="flex flex-col items-center gap-3 py-8 text-center">
                        <p className="text-sm text-muted-foreground">
                            Não foi possível carregar o histórico do chamado.
                        </p>

                        <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => refetch()}
                        >
                            Tentar novamente
                        </Button>
                    </div>
                )}

                {!isLoading && !isError && history.length === 0 && (
                    <p className="py-8 text-center text-sm text-muted-foreground">
                        Nenhuma alteração registrada neste chamado.
                    </p>
                )}

                {!isLoading && !isError && history.length > 0 && (
                    <div className="relative space-y-6">
                        <div className="relative space-y-6">
                            <div className="absolute bottom-2 left-[11px] top-2 w-px bg-border" />

                            {history.map((item) => {
                                const Icon = getHistoryIcon(item.action);
                                return (
                                    <div
                                        key={item.id}
                                        className="relative flex gap-4"
                                    >
                                        <div className="relative z-10 flex size-6 shrink-0 items-center justify-center rounded-full border bg-background">
                                            <Icon className="size-3.5 text-muted-foreground" />
                                        </div>

                                        <div className="min-w-0 flex-1 pb-1">
                                            <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
                                                <p className="font-medium">
                                                    {getHistoryTitle(item.action)}
                                                </p>

                                                <time className="text-xs text-muted-foreground">
                                                    {formatDate(item.createdAt)}
                                                </time>
                                            </div>

                                            <p className="mt-1 text-sm text-muted-foreground">
                                                {getHistoryDescription(item)}
                                            </p>

                                            <p className="mt-2 text-xs text-muted-foreground">
                                                Realizado por{" "}
                                                <span className="font-medium text-foreground">
                                                    {item.performedBy.name}
                                                </span>
                                            </p>
                                        </div>
                                    </div>
                                )
                            })}
                        </div>
                    </div>
                )}
            </CardContent>
        </Card>
    );
}