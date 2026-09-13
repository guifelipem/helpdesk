import { Skeleton } from "@/components/ui/skeleton";

export function TicketQueueSkeleton() {
    return (
        <div className="space-y-3" aria-label="Carregando fila de chamados">
            {Array.from({ length: 5 }).map((_, index) => (
                <div key={index} className="rounded-2xl border border-border bg-card/80 p-4 sm:p-5">
                    <div className="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
                        <div className="flex-1 space-y-3">
                            <Skeleton className="h-3 w-24" />
                            <Skeleton className="h-5 w-full max-w-xl" />
                            <div className="flex flex-wrap gap-3">
                                <Skeleton className="h-4 w-32" />
                                <Skeleton className="h-4 w-40" />
                                <Skeleton className="h-4 w-28" />
                            </div>
                        </div>
                        <div className="flex gap-2">
                            <Skeleton className="h-10 w-28" />
                            <Skeleton className="h-10 w-24" />
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
}
