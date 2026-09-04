export const ticketHistoryQueryKeys = {
        all: ['tickets'] as const,

        history: (id: number) => [...ticketHistoryQueryKeys.all, 'history', id] as const,
};
