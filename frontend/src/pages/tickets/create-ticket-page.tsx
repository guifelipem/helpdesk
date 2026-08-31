import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { TicketForm } from "@/features/tickets/components/ticket-form";
import { useCreateTicket } from "@/features/tickets/hooks/use-tickets";
import type { CreateTicketFormData } from "@/features/tickets/schemas/create-ticket.schema";
import { Link, useNavigate } from "react-router-dom";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";
import { ArrowLeft, PlusCircle } from "lucide-react";

export function CreateTicketPage() {
    const navigate = useNavigate();
    const createTicketMutation = useCreateTicket();

    function handleCreateTicket(data: CreateTicketFormData) {
        createTicketMutation.mutate(data, {
            onSuccess: (ticket) => {
                navigate(`/tickets/${ticket.id}`);
            },
        });
    }

    const errorMessage = getApiErrorMessage(
        createTicketMutation.error,
        "Não foi possível criar o chamado. Tente novamente."
    );

    return (
        <div className="space-y-6">
            <div className="rounded-3xl bg-gradient-to-r from-[#301c41] to-[#413b6b] px-6 py-7 text-white shadow-[0_20px_50px_-30px_#301c41]">
                <div className="mb-3 flex size-10 items-center justify-center rounded-xl bg-white/10 text-[#aebfff]"><PlusCircle className="size-5" /></div>
                <h1 className="text-3xl font-bold tracking-tight">
                    Novo chamado
                </h1>
                <p className="mt-2 text-white/60">
                    Descreva o problema para que a equipe possa te ajudar.
                </p>
            </div>

            <Card className="max-w-3xl">
                <CardContent>
                    <TicketForm
                        onSubmit={handleCreateTicket}
                        isSubmitting={createTicketMutation.isPending}
                    />

                    {createTicketMutation.isError && (
                        <p className="mt-4 text-sm text-destructive">
                            {errorMessage}
                        </p>
                    )}
                </CardContent>
            </Card>

            <Button asChild variant="outline">
                <Link to="/tickets"><ArrowLeft /> Voltar</Link>
            </Button>
        </div>
    )
}
