import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { TicketForm } from "@/features/tickets/components/ticket-form";
import { useCreateTicket } from "@/features/tickets/hooks/use-tickets";
import type { CreateTicketFormData } from "@/features/tickets/schemas/create-ticket.schema";
import { Link, useNavigate } from "react-router-dom";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";

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
            <div>
                <h1 className="text-2xl font-bold tracking-tight">
                    Novo Ticket
                </h1>
                <p className="text-muted-foreground">
                    Descreva o problema para que a equipe possa te ajudar.
                </p>
            </div>

            <Card>
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
                <Link to="/tickets">Voltar</Link>
            </Button>
        </div>
    )
}