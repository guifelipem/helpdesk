import { z } from "zod";
import { VALIDATION_LIMITS } from "@/shared/constants/validation-limits";

export const registerSchema = z
    .object({
        name: z.string().trim().min(1, "O nome é obrigatório").max(VALIDATION_LIMITS.userName, "O nome deve ter no máximo 255 caracteres"),
        email: z.email("Informe um e-mail válido").max(VALIDATION_LIMITS.email, "O e-mail deve ter no máximo 255 caracteres"),
        password: z.string().min(6, "A senha deve ter no mínimo 6 caracteres").max(VALIDATION_LIMITS.password, "A senha deve ter no máximo 72 caracteres"),
        confirmPassword: z.string().min(1, "Confirme sua senha").max(VALIDATION_LIMITS.password, "A senha deve ter no máximo 72 caracteres"),
    })
    .refine((data) => data.password === data.confirmPassword, {
        message: "As senhas não coincidem",
        path: ["confirmPassword"],
    });

export type RegisterFormData = z.infer<typeof registerSchema>;
