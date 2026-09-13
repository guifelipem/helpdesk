import { isRouteErrorResponse, useRouteError } from "react-router-dom";

import { ErrorState } from "@/shared/components/error-state";

export function RouteErrorPage() {
    const error = useRouteError();
    const isNotFound = isRouteErrorResponse(error) && error.status === 404;

    return (
        <main className="app-shell-background flex min-h-screen items-center justify-center p-4">
            <div className="w-full max-w-2xl rounded-2xl bg-card p-4 shadow-xl">
                <ErrorState
                    title={isNotFound ? "Página não encontrada" : "Não foi possível abrir esta página"}
                    description={isNotFound
                        ? "O endereço acessado não existe ou foi alterado."
                        : "Ocorreu uma falha inesperada ao exibir esta página."}
                    actionLabel="Recarregar página"
                    onRetry={() => window.location.reload()}
                />
            </div>
        </main>
    );
}
