export const ticketQueryKeys = {
    all: ["tickets"] as const,

    lists: () => [...ticketQueryKeys.all, "list"] as const,

    list: (params?: unknown) => [...ticketQueryKeys.lists(), params] as const,

    details: () => [...ticketQueryKeys.all, "detail"] as const,

    detail: (id: number) => [...ticketQueryKeys.details(), id] as const,

    queues: () => [...ticketQueryKeys.all, "queues"] as const,

    queue: (queue: string, params?: unknown) => [...ticketQueryKeys.queues(), queue, params] as const,

    queueSummary: () => [...ticketQueryKeys.queues(), "summary"] as const,
};
