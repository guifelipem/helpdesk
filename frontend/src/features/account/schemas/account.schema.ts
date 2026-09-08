import { z } from "zod";

export const accountSchema = z.object({
    name: z.string().trim().min(1, "O nome é obrigatório").max(255, "O nome deve ter no máximo 255 caracteres"),
    email: z.email("Informe um e-mail válido").max(255, "O e-mail deve ter no máximo 255 caracteres"),
});

export const changePasswordSchema = z
    .object({
        currentPassword: z.string().min(1, "Informe sua senha atual"),
        newPassword: z.string().min(6, "A nova senha deve ter no mínimo 6 caracteres"),
        confirmPassword: z.string().min(1, "Confirme a nova senha"),
    })
    .refine((data) => data.newPassword === data.confirmPassword, {
        message: "As senhas não coincidem",
        path: ["confirmPassword"],
    });

export type AccountFormData = z.infer<typeof accountSchema>;
export type ChangePasswordFormData = z.infer<typeof changePasswordSchema>;
