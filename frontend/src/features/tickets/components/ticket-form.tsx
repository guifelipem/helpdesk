import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

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
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-2">
                <Label htmlFor="title">Título</Label>
                <Input 
                    id="title"
                    placeholder="Ex: Não consigo acessar o sistema"
                    {...register("title")}
                />
                {errors.title && (
                    <p className="text-sm text-red-500">
                        {errors.title.message}
                    </p>
                )}
            </div>

            <div className="space-y-2">
                <Label htmlFor="description">Descrição</Label>
                <textarea 
                    id="description"
                    placeholder="Descreva o problema com o máximo de detalhes possível."
                    className="min-h-32 w-full rounded-md border border-input bg-transparent"
                    {...register("description")}
                />
                {errors.description && (
                    <p className="text-sm text-red-500">
                        {errors.description.message}
                    </p>
                )}
            </div>

            <div className="space-y-2">
                <Label htmlFor="priority">Prioridade</Label>
                <select
                    id="priority"
                    className="h-9 w-full rounded-md border border-input bg-transparent px-3"
                    {...register("priority")}
                >
                    <option value="Low">Baixa</option>
                    <option value="MEDIUM">Média</option>
                    <option value="HIGH">Alta</option>
                    <option value="CRITICAL">Crítica</option>
                </select>
                {errors.priority && (
                    <p className="text-sm text-red-500">
                        {errors.priority.message}
                    </p>
                )}
            </div>

            <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Criando..." : "Criar ticket"}
            </Button>
        </form>
    )
}