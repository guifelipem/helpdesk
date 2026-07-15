import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export function TicketCardSkeleton() {
    return (
        <Card>
            <CardHeader className="space-y-3">
                <div className="flex items-center justify-between gap-4">
                    <Skeleton className="h-5 w-2/3" />
                    <Skeleton className="h-5 w-20" />
                </div>

                <Skeleton className="h-4 w-24" />
            </CardHeader>

            <CardContent className="space-y-3">
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-5/6" />
                <Skeleton className="h-4 w-1/2" />
            </CardContent>
        </Card>
    )
}

export function TicketListSkeleton() {
    return (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {Array.from({ length: 6 }).map((_, index) => (
                <TicketCardSkeleton key={index} />
            ))}
        </div>
    )
}