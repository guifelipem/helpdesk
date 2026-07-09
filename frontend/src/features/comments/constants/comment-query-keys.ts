export const commentQueryKeys = {
    all: ["comments"] as const,

    list: (ticketId: number) => [...commentQueryKeys.all, ticketId] as const,
};