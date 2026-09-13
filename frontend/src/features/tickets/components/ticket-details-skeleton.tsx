import { Skeleton } from "@/components/ui/skeleton";

export function TicketDetailsSkeleton() {
    return (
        <div className="space-y-6" aria-label="Carregando detalhes do chamado" aria-busy="true">
            <Skeleton className="h-11 w-28 rounded-xl" />
            <Skeleton className="h-80 rounded-3xl" />
            <Skeleton className="h-48 rounded-2xl" />
            <Skeleton className="h-72 rounded-2xl" />
            <Skeleton className="h-56 rounded-2xl" />
        </div>
    );
}
