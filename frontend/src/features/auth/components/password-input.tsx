import { useState, type ComponentProps } from "react";
import { Eye, EyeOff, LockKeyhole } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

type PasswordInputProps = Omit<ComponentProps<typeof Input>, "type">;

export function PasswordInput({ className, ...props }: PasswordInputProps) {
    const [isVisible, setIsVisible] = useState(false);

    return (
        <div className="relative">
            <LockKeyhole className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
                type={isVisible ? "text" : "password"}
                className={`pl-10 pr-11 ${className ?? ""}`}
                {...props}
            />
            <Button
                type="button"
                variant="ghost"
                size="icon"
                className="absolute right-1.5 top-1/2 -translate-y-1/2 text-muted-foreground hover:translate-y-[-50%]"
                onClick={() => setIsVisible((visible) => !visible)}
                aria-label={isVisible ? "Ocultar senha" : "Mostrar senha"}
                aria-pressed={isVisible}
            >
                {isVisible ? <EyeOff /> : <Eye />}
            </Button>
        </div>
    );
}
