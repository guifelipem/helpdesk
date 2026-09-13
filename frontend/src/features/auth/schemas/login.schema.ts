import { z } from "zod";
import { VALIDATION_LIMITS } from "@/shared/constants/validation-limits";

export const loginSchema = z.object({
    email: z
        .email("Informe um e-mail válido")
        .min(1, "O e-mail é obrigatório")
        .max(VALIDATION_LIMITS.email, "O e-mail deve ter no máximo 255 caracteres"),

    password: z
        .string()
        .min(1, "A senha é obrigatória")
        .max(VALIDATION_LIMITS.password, "A senha deve ter no máximo 72 caracteres"),
});

export type LoginFormData = z.infer<typeof loginSchema>;
