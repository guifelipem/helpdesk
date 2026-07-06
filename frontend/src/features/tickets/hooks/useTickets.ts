import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { assignTicketToMe, createTicket, findAllTickets, findMyTickets, findTicketById, updateTicketStatus } from "../api/ticket.api";
import type { FindAllTicketsParams } from "../types/find-all-tickets-params";
import { ticketQueryKeys } from "../constants/ticket-query-keys";

export function useTickets(params?: FindAllTicketsParams) {
    return useQuery({
        queryKey: ticketQueryKeys.list(params),
        queryFn: () => findAllTickets(params),
    });
}

export function useMyTickets() {
    return useQuery({
        queryKey: [...ticketQueryKeys.all, "my"],
        queryFn: findMyTickets,
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
        },
    });
}