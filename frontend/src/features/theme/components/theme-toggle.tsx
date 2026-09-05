import { Moon, Sun } from "lucide-react";
import { useSyncExternalStore } from "react";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { getTheme, setTheme, subscribeToTheme } from "../theme";

type ThemeToggleProps = {
    className?: string;
};

export function ThemeToggle({ className }: ThemeToggleProps) {
    const theme = useSyncExternalStore(subscribeToTheme, getTheme);
    const isDark = theme === "dark";

    return (
        <Button
            type="button"
            variant="outline"
            size="icon-lg"
            className={cn("rounded-full", className)}
            onClick={() => setTheme(isDark ? "light" : "dark")}
            aria-label={isDark ? "Ativar tema claro" : "Ativar tema escuro"}
            title={isDark ? "Ativar tema claro" : "Ativar tema escuro"}
        >
            {isDark ? <Sun /> : <Moon />}
        </Button>
    );
}
