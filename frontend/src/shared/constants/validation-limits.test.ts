import { describe, expect, it } from "vitest";

import { changePasswordSchema } from "@/features/account/schemas/account.schema";
import { registerSchema } from "@/features/auth/schemas/register.schema";
import { createTicketSchema } from "@/features/tickets/schemas/create-ticket.schema";
import { VALIDATION_LIMITS } from "./validation-limits";

describe("limites de validação", () => {
    it("mantém senhas limitadas ao máximo aceito pelo backend", () => {
        const password = "a".repeat(VALIDATION_LIMITS.password + 1);
        expect(registerSchema.safeParse({ name: "Nome", email: "a@b.com", password, confirmPassword: password }).success).toBe(false);
        expect(changePasswordSchema.safeParse({ currentPassword: password, newPassword: password, confirmPassword: password }).success).toBe(false);
    });

    it("usa os limites de título e descrição do backend", () => {
        const result = createTicketSchema.safeParse({
            title: "a".repeat(VALIDATION_LIMITS.ticketTitle),
            description: "a".repeat(VALIDATION_LIMITS.ticketDescription),
            priority: "LOW",
        });
        expect(result.success).toBe(true);
    });
});
