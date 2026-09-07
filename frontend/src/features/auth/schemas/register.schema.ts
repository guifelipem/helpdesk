import { z } from "zod";

export const registerSchema = z
    .object({
        name: z.string().trim().min(1, "O nome é obrigatório"),
        email: z.email("Informe um e-mail válido"),
        password: z.string().min(6, "A senha deve ter no mínimo 6 caracteres"),
        confirmPassword: z.string().min(1, "Confirme sua senha"),
    })
    .refine((data) => data.password === data.confirmPassword, {
        message: "As senhas não coincidem",
        path: ["confirmPassword"],
    });

export type RegisterFormData = z.infer<typeof registerSchema>;
