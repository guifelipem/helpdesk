export const ticketQueryKeys = {
    all: ["tickets"] as const,

    lists: () => [...ticketQueryKeys.all, "list"] as const,

    list: (params?: unknown) => [...ticketQueryKeys.lists(), params] as const,

    details: () => [...ticketQueryKeys.all, "detail"] as const,

    detail: (id: number) => [...ticketQueryKeys.details(), id] as const,

    queues: () => [...ticketQueryKeys.all, "queues"] as const,

    queue: (queue: string, params?: unknown) => [...ticketQueryKeys.queues(), queue, params] as const,

    queueSummary: () => [...ticketQueryKeys.queues(), "summary"] as const,

    agentDashboard: () => [...ticketQueryKeys.all, "agent-dashboard"] as const,

    agentPerformance: (from: string, to: string) => [...ticketQueryKeys.all, "agent-performance", from, to] as const,

    adminDashboard: () => [...ticketQueryKeys.all, "admin-dashboard"] as const,

    adminPerformance: (from: string, to: string) => [...ticketQueryKeys.all, "admin-performance", from, to] as const,
};
