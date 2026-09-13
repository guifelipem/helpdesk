export const VALIDATION_LIMITS = {
    userName: 255,
    email: 255,
    password: 72,
    ticketTitle: 255,
    ticketDescription: 10_000,
    comment: 5_000,
    rejectionReason: 2_000,
} as const;
