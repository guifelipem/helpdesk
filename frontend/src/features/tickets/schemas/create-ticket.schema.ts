import { z } from "zod";
import { VALIDATION_LIMITS } from "@/shared/constants/validation-limits";

export const createTicketSchema = z.object({
    title: z
        .string()
        .min(3, "O título deve ter pelo menos 3 caracteres.")
        .max(VALIDATION_LIMITS.ticketTitle, "O título deve ter no máximo 255 caracteres."),

    description: z
        .string()
        .min(10, "A descrição deve ter pelo menos 10 caracteres.")
        .max(VALIDATION_LIMITS.ticketDescription, "A descrição deve ter no máximo 10000 caracteres."),
    
    
    priority: z.enum(["LOW", "MEDIUM", "HIGH"]),
});

export type CreateTicketFormData = z.infer<typeof createTicketSchema>;
