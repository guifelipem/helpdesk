export const ticketQueryKeys = {
        all: ['tickets'] as const,

        detail: (id: number) => [...ticketQueryKeys.all, 'detail', id] as const,

        history: (id: number) => [...ticketQueryKeys.all, 'history', id] as const,
};