import { useMutation, useQuery, useQueryClient, type UseQueryOptions } from "@tanstack/react-query";
import { keepPreviousData } from "@tanstack/react-query";

import { assignTicketToMe, closeTicket, createTicket, findAllTickets, findMyTickets, findTicketById, updateTicketStatus } from "../api/ticket.api";
import type { FindAllTicketsParams } from "../types/find-all-tickets-params";
import { ticketQueryKeys } from "../constants/ticket-query-keys";
import { ticketHistoryQueryKeys } from "@/features/history/constants/ticket-history-query-keys";

import type { PageResponse } from "@/shared/types/page-response";
import type { Ticket } from "../types/ticket.types";

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
    options?: Omit<UseQueryOptions<Ticket[]>, "queryKey" | "queryFn">
) {
    return useQuery({
        queryKey: [...ticketQueryKeys.all, "my"],
        queryFn: findMyTickets,
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

export function useAssignTickets() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: assignTicketToMe,

        onSuccess: (ticket) => {
            queryClient.invalidateQueries({
                queryKey: ticketQueryKeys.lists(),
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