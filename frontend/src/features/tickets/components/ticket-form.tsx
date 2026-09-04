import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

import { createTicketSchema, type CreateTicketFormData } from "../schemas/create-ticket.schema";

type TicketFormProps = {
    onSubmit: (data: CreateTicketFormData) => void;
    isSubmitting?: boolean;
};

export function TicketForm({ onSubmit, isSubmitting }: TicketFormProps) {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<CreateTicketFormData>({
        resolver: zodResolver(createTicketSchema),
        defaultValues: {
            title: "",
            description: "",
            priority: "LOW",
        },
    });

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <div className="space-y-2">
                <Label htmlFor="title">Título</Label>
                <Input 
                    id="title"
                    placeholder="Ex: Não consigo acessar o sistema"
                    aria-invalid={!!errors.title}
                    aria-describedby={errors.title ? "title-error" : undefined}
                    {...register("title")}
                />
                {errors.title && (
                    <p id="title-error" role="alert" className="text-sm text-destructive">
                        {errors.title.message}
                    </p>
                )}
            </div>

            <div className="space-y-2">
                <Label htmlFor="description">Descrição</Label>
                <Textarea 
                    id="description"
                    placeholder="Descreva o problema com o máximo de detalhes possível."
                    className="min-h-32"
                    aria-invalid={!!errors.description}
                    aria-describedby={errors.description ? "description-error" : undefined}
                    {...register("description")}
                />
                {errors.description && (
                    <p id="description-error" role="alert" className="text-sm text-destructive">
                        {errors.description.message}
                    </p>
                )}
            </div>

            <div className="space-y-2">
                <Label htmlFor="priority">Prioridade</Label>
                <select
                    id="priority"
                    className="h-11 w-full rounded-xl border border-input bg-white/80 px-3.5 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/20"
                    aria-invalid={!!errors.priority}
                    aria-describedby={errors.priority ? "priority-error" : undefined}
                    {...register("priority")}
                >
                    <option value="LOW">Baixa</option>
                    <option value="MEDIUM">Média</option>
                    <option value="HIGH">Alta</option>
                </select>
                {errors.priority && (
                    <p id="priority-error" role="alert" className="text-sm text-destructive">
                        {errors.priority.message}
                    </p>
                )}
            </div>

            <Button type="submit" size="lg" disabled={isSubmitting}>
                {isSubmitting ? "Criando..." : "Criar chamado"}
            </Button>
        </form>
    )
}
