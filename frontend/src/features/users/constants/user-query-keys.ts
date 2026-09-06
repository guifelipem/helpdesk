export const userQueryKeys = {
    all: ["users"] as const,
    list: (params?: unknown) => [...userQueryKeys.all, "list", params] as const,
    activeAgents: () => [...userQueryKeys.all, "active-agents"] as const,
};
