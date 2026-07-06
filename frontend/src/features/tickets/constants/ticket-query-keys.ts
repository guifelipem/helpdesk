export const ticketQueryKeys = {
    all: ["tickets"] as const,

    lists: () => [...ticketQueryKeys.all, "list"] as const,

    list: (params?: unknown) => [...ticketQueryKeys.lists(), params] as const,

    details: () => [...ticketQueryKeys.all, "detail"] as const,

    detail: (id: number) => [...ticketQueryKeys.details(), id] as const,
};