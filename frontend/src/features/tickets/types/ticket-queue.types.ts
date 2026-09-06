export type TicketQueue =
    | "AVAILABLE"
    | "MY_TICKETS"
    | "WAITING_CLIENT"
    | "WAITING_AGENT"
    | "RESOLVED";

export type TicketQueueParams = {
    page?: number;
    size?: number;
};

export type TicketQueueSummary = {
    available: number;
    myTickets: number;
    waitingClient: number;
    waitingAgent: number;
    resolved: number;
};
