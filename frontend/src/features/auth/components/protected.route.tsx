import { Navigate, Outlet } from "react-router-dom";

import { ErrorState } from "@/shared/components/error-state";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";
import { useAuthStore } from "../store/auth.store";
import { useAuthMe } from "../hooks/useAuthMe";

export function ProtectedRoute() {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    const user = useAuthStore((state) => state.user);

    const { isPending, isError, error, refetch, isFetching } = useAuthMe();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />
    }

    if (!user && isPending) {
        return <p>Carregando usuário...</p>;
    }

    if (!user && isError) {
    return (
        <ErrorState
            title="Não foi possível carregar sua sessão"
            description={getApiErrorMessage(
                error,
                "Não foi possível conectar ao servidor. Tente novamente."
            )}
            onRetry={() => refetch()}
            isRetrying={isFetching}
        />
    );
}

    return <Outlet />
}