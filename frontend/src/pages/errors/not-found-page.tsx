import { FileQuestion } from "lucide-react";
import { useNavigate } from "react-router-dom";

import { ErrorState } from "@/shared/components/error-state";

export function NotFoundPage() {
    const navigate = useNavigate();

    return (
        <div className="space-y-5">
            <div className="mx-auto flex size-14 items-center justify-center rounded-2xl bg-primary/15 text-primary-strong">
                <FileQuestion aria-hidden="true" />
            </div>
            <ErrorState
                title="Página não encontrada"
                description="O endereço acessado não existe ou foi alterado."
                actionLabel="Voltar ao início"
                onRetry={() => navigate("/")}
            />
        </div>
    );
}
