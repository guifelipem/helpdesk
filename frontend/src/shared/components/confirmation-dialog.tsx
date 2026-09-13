import { useEffect, useId } from "react";
import { TriangleAlert, X } from "lucide-react";

import { Button } from "@/components/ui/button";

type ConfirmationDialogProps = {
    open: boolean;
    title: string;
    description: string;
    confirmLabel: string;
    isPending?: boolean;
    destructive?: boolean;
    onConfirm: () => void;
    onClose: () => void;
};

export function ConfirmationDialog({
    open,
    title,
    description,
    confirmLabel,
    isPending = false,
    destructive = false,
    onConfirm,
    onClose,
}: ConfirmationDialogProps) {
    const titleId = useId();
    const descriptionId = useId();

    useEffect(() => {
        if (!open) return;

        const previouslyFocused = document.activeElement as HTMLElement | null;
        function closeOnEscape(event: KeyboardEvent) {
            if (event.key === "Escape" && !isPending) onClose();
        }

        document.addEventListener("keydown", closeOnEscape);
        return () => {
            document.removeEventListener("keydown", closeOnEscape);
            previouslyFocused?.focus();
        };
    }, [isPending, onClose, open]);

    if (!open) return null;

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/65 p-4 backdrop-blur-sm"
            onMouseDown={(event) => {
                if (event.target === event.currentTarget && !isPending) onClose();
            }}
        >
            <section
                role="alertdialog"
                aria-modal="true"
                aria-labelledby={titleId}
                aria-describedby={descriptionId}
                className="w-full max-w-md rounded-3xl border border-border bg-surface-elevated p-6 shadow-2xl"
            >
                <div className="flex items-start justify-between gap-4">
                    <span className="flex size-11 shrink-0 items-center justify-center rounded-2xl bg-amber-100 text-amber-700 dark:bg-amber-950/60 dark:text-amber-300">
                        <TriangleAlert className="size-5" aria-hidden="true" />
                    </span>
                    <div className="min-w-0 flex-1">
                        <h2 id={titleId} className="text-xl font-bold">{title}</h2>
                        <p id={descriptionId} className="mt-2 text-sm leading-6 text-muted-foreground">{description}</p>
                    </div>
                    <Button type="button" size="icon" variant="ghost" aria-label="Fechar" disabled={isPending} onClick={onClose}>
                        <X aria-hidden="true" />
                    </Button>
                </div>

                <div className="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
                    <Button type="button" variant="outline" autoFocus disabled={isPending} onClick={onClose}>Cancelar</Button>
                    <Button type="button" variant={destructive ? "destructive" : "default"} disabled={isPending} onClick={onConfirm}>
                        {isPending ? "Aguarde..." : confirmLabel}
                    </Button>
                </div>
            </section>
        </div>
    );
}
