export const ticketHistoryQueryKeys = {
        all: ['tickets'] as const,

        detail: (id: number) => [...ticketHistoryQueryKeys.all, 'detail', id] as const,

        history: (id: number) => [...ticketHistoryQueryKeys.all, 'history', id] as const,
};