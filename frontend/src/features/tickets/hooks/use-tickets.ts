import { useMutation, useQuery, useQueryClient, type UseQueryOptions } from "@tanstack/react-query";
import { keepPreviousData } from "@tanstack/react-query";

import { assignTicketToMe, closeTicket, createTicket, findAllTickets, findMyTickets, findTicketById, findTicketQueue, findTicketQueueSummary, rejectTicketResolution, returnTicketToQueue, sendTicketToAgent, transferTicket, updateTicketStatus } from "../api/ticket.api";
import type { FindAllTicketsParams, FindMyTicketsParams } from "../types/find-all-tickets-params";
import { ticketQueryKeys } from "../constants/ticket-query-keys";
import { ticketHistoryQueryKeys } from "@/features/history/constants/ticket-history-query-keys";

import type { PageResponse } from "@/shared/types/page-response";
import type { Ticket } from "../types/ticket.types";
import type { TicketQueue, TicketQueueParams, TicketQueueSummary } from "../types/ticket-queue.types";

export function useTickets(
    params?: FindAllTicketsParams,
    options?: Omit<UseQueryOptions<PageResponse<Ticket>>, "queryKey" | "queryFn">
) {
    return useQuery({
        queryKey: ticketQueryKeys.list(params),
        queryFn: () => findAllTickets(params),
        placeholderData: keepPreviousData,
        ...options,
    });
}

export function useMyTickets(
    params?: FindMyTicketsParams,
    options?: Omit<UseQueryOptions<PageResponse<Ticket>>, "queryKey" | "queryFn">
) {
    return useQuery({
        queryKey: [...ticketQueryKeys.all, "my", params],
        queryFn: () => findMyTickets(params),
        placeholderData: keepPreviousData,
        ...options,
    });
}

export function useTicket(id: number) {
    return useQuery({
        queryKey: ticketQueryKeys.detail(id),
        queryFn: () => findTicketById(id),
        enabled: !!id,
    });
}

export function useCreateTicket() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: createTicket,

        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: ticketQueryKeys.all,
            });
        },
    });
}

export function useAssignTicket() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: assignTicketToMe,

        onSuccess: (ticket) => {
            queryClient.invalidateQueries({
                queryKey: ticketQueryKeys.lists(),
            });
            queryClient.invalidateQueries({
                queryKey: ticketQueryKeys.queues(),
            });

            queryClient.invalidateQueries({
                queryKey: ticketQueryKeys.detail(ticket.id),
            });
            queryClient.invalidateQueries({
                queryKey: ticketHistoryQueryKeys.history(ticket.id),
            });
        },
    });
}

export function useUpdateTicketStatus() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: updateTicketStatus,

        onSuccess: (ticket) => {
            queryClient.invalidateQueries({
                queryKey: ticketQueryKeys.lists(),
            });
            queryClient.invalidateQueries({
                queryKey: ticketQueryKeys.queues(),
            });

            queryClient.invalidateQueries({
                queryKey: ticketQueryKeys.detail(ticket.id),
            });
            queryClient.invalidateQueries({
                queryKey: ticketHistoryQueryKeys.history(ticket.id),
            });
        },
    });
}

export function useCloseTicket() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: closeTicket,

        onSuccess: (ticket) => {
            queryClient.invalidateQueries({
                queryKey: ticketQueryKeys.all,
            });

            queryClient.invalidateQueries({
                queryKey: ticketHistoryQueryKeys.history(ticket.id),
            });
        },
    });
}

export function useRejectTicketResolution() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: rejectTicketResolution,

        onSuccess: (ticket) => {
            queryClient.invalidateQueries({
                queryKey: ticketQueryKeys.all,
            });

            queryClient.invalidateQueries({
                queryKey: ticketHistoryQueryKeys.history(ticket.id),
            });
        },
    });
}

export function useSendTicketToAgent() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: sendTicketToAgent,

        onSuccess: (ticket) => {
            queryClient.invalidateQueries({
                queryKey: ticketQueryKeys.all,
            });

            queryClient.invalidateQueries({
                queryKey: ticketHistoryQueryKeys.history(ticket.id),
            });
        },
    });
}

export function useTicketQueue(
    queue: TicketQueue,
    params?: TicketQueueParams,
    options?: Omit<UseQueryOptions<PageResponse<Ticket>>, "queryKey" | "queryFn">
) {
    return useQuery({
        queryKey: ticketQueryKeys.queue(queue, params),
        queryFn: () => findTicketQueue(queue, params),
        ...options,
    });
}

export function useTicketQueueSummary(
    options?: Omit<UseQueryOptions<TicketQueueSummary>, "queryKey" | "queryFn">
) {
    return useQuery({
        queryKey: ticketQueryKeys.queueSummary(),
        queryFn: findTicketQueueSummary,
        ...options,
    });
}

function useInvalidateManagedTicket() {
    const queryClient = useQueryClient();
    return (ticket: Ticket) => {
        queryClient.invalidateQueries({ queryKey: ticketQueryKeys.lists() });
        queryClient.invalidateQueries({ queryKey: ticketQueryKeys.detail(ticket.id) });
        queryClient.invalidateQueries({ queryKey: ticketHistoryQueryKeys.history(ticket.id) });
    };
}

export function useReturnTicketToQueue() {
    const invalidateTicket = useInvalidateManagedTicket();
    return useMutation({ mutationFn: returnTicketToQueue, onSuccess: invalidateTicket });
}

export function useTransferTicket() {
    const invalidateTicket = useInvalidateManagedTicket();
    return useMutation({ mutationFn: transferTicket, onSuccess: invalidateTicket });
}
