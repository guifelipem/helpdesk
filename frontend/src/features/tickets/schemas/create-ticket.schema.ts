import { z } from "zod";

export const createTicketSchema = z.object({
    title: z
        .string()
        .min(3, "O título deve ter pelo menos 3 caracteres.")
        .max(120, "O título deve ter no máximo 120 caracteres."),

    description: z
        .string()
        .min(10, "A descrição deve ter pelo menos 10 caracteres.")
        .max(1000, "A descrição deve ter no máximo 1000 caracteres."),
    
    
    priority: z.enum(["LOW", "MEDIUM", "HIGH", "CRITICAL"]),
});

export type CreateTicketFormData = z.infer<typeof createTicketSchema>;