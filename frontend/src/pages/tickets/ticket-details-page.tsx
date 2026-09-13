import axios from "axios";
import { useNavigate, useParams } from "react-router-dom";

import { ErrorState } from "@/shared/components/error-state";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";
import { useTicket } from "@/features/tickets/hooks/use-tickets";
import { TicketDetails } from "@/features/tickets/components/ticket-details";
import { TicketDetailsSkeleton } from "@/features/tickets/components/ticket-details-skeleton";

export function TicketDetailsPage() {
    const { id } = useParams();
    const navigate = useNavigate();

    const ticketId = Number(id);
    const hasValidId = Number.isInteger(ticketId) && ticketId > 0;

    const ticketQuery = useTicket(hasValidId ? ticketId : 0);
    const status = axios.isAxiosError(ticketQuery.error) ? ticketQuery.error.response?.status : undefined;

    if (!hasValidId || status === 404) {
        return (
            <ErrorState
                title="Chamado não encontrado"
                description="O chamado informado não existe ou não está mais disponível."
                actionLabel="Voltar aos chamados"
                onRetry={() => navigate("/tickets")}
            />
        );
    }

    if (status === 403) {
        return (
            <ErrorState
                title="Acesso negado"
                description="Você não possui permissão para visualizar este chamado."
                actionLabel="Voltar aos chamados"
                onRetry={() => navigate("/tickets")}
            />
        );
    }

    if (ticketQuery.isPending) return <TicketDetailsSkeleton />;

    if (ticketQuery.isError || !ticketQuery.data) {
        return (
            <ErrorState
                title="Não foi possível carregar o chamado"
                description={getApiErrorMessage(ticketQuery.error, "Ocorreu uma falha temporária. Verifique sua conexão e tente novamente.")}
                onRetry={() => ticketQuery.refetch()}
                isRetrying={ticketQuery.isFetching}
            />
        );
    }

    return <TicketDetails ticket={ticketQuery.data} />;
}
