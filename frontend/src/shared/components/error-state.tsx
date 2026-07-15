import { Button } from "@/components/ui/button"

type ErrorStateProps = {
    title?: string;
    description?: string;
    onRetry: () => void;
    isRetrying?: boolean;
};

export function ErrorState({
    title = "Não foi possível carregar os dados",
    description = "Ocorreu um erro ao buscar as informações. Verifique sua conexão e tente novamente.",
    onRetry,
    isRetrying = false,
}: ErrorStateProps) {
    return (
        <div className="flex min-h-64 flex-col items-center justify-center rounded-lg border border-dashed p-6 text-center">
            <h2 className="text-lg font-semibold"> 
                {title}
            </h2>

            <p className="mt-2 max-w-md text-sm text-muted-foreground">
                {description}
            </p>

            <Button
                type="button"
                className="mt-4"
                onClick={onRetry}
                disabled={isRetrying}
            >
                {isRetrying ? "Tentando novamente..." : "Tentar novamente"}
            </Button>
        </div>
    )
}