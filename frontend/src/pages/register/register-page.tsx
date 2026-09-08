import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { Headphones, Mail, UserRound } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { login, getMe, register as registerUser } from "@/features/auth/api/auth.api";
import { PasswordInput } from "@/features/auth/components/password-input";
import { registerSchema, type RegisterFormData } from "@/features/auth/schemas/register.schema";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { ThemeToggle } from "@/features/theme/components/theme-toggle";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";

export function RegisterPage() {
  const navigate = useNavigate();
  const setToken = useAuthStore((state) => state.setToken);
  const setUser = useAuthStore((state) => state.setUser);
  const form = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: { name: "", email: "", password: "", confirmPassword: "" },
  });

  const registerMutation = useMutation({
    mutationFn: async (data: RegisterFormData) => {
      await registerUser({ name: data.name, email: data.email, password: data.password });
      const loginResponse = await login({ email: data.email, password: data.password });
      setToken(loginResponse.token);
      const user = await getMe();
      setUser(user);
    },
    onSuccess: () => navigate("/", { replace: true }),
  });

  const errorMessage = getApiErrorMessage(registerMutation.error, "Não foi possível criar sua conta. Tente novamente.");

  return (
    <main className="auth-background relative flex min-h-screen items-center justify-center overflow-hidden px-4 py-12">
      <ThemeToggle className="absolute right-4 top-4 z-10 border-white/20 bg-white/10 text-white hover:bg-white/20 hover:text-white" />
      <div className="absolute left-[8%] top-[12%] size-56 rounded-full border border-white/10" />
      <div className="absolute bottom-[8%] right-[7%] size-80 rounded-full border border-white/8" />
      <Card className="relative w-full max-w-md border-white/20 bg-card/96 px-2 py-2 shadow-[0_30px_80px_-25px_#00000090] backdrop-blur-xl">
        <CardHeader className="pb-2 text-center">
          <div className="mx-auto mb-4 flex size-12 items-center justify-center rounded-2xl bg-gradient-to-br from-primary to-accent text-[#030607] shadow-lg shadow-primary/25 dark:text-white"><Headphones className="size-6" /></div>
          <p className="text-xs font-bold uppercase tracking-[0.22em] text-primary-strong">Helpdesk</p>
          <CardTitle className="mt-2 text-2xl font-bold tracking-tight">Crie sua conta</CardTitle>
          <CardDescription>Cadastre-se para abrir e acompanhar seus chamados.</CardDescription>
        </CardHeader>

        <CardContent>
          <form onSubmit={form.handleSubmit((data) => registerMutation.mutate(data))} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Nome</Label>
              <div className="relative">
                <UserRound className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input id="name" autoComplete="name" placeholder="Seu nome" className="pl-10" aria-invalid={!!form.formState.errors.name} {...form.register("name")} />
              </div>
              {form.formState.errors.name && <p role="alert" className="text-sm text-destructive">{form.formState.errors.name.message}</p>}
            </div>

            <div className="space-y-2">
              <Label htmlFor="register-email">E-mail</Label>
              <div className="relative">
                <Mail className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input id="register-email" type="email" autoComplete="email" placeholder="voce@email.com" className="pl-10" aria-invalid={!!form.formState.errors.email} {...form.register("email")} />
              </div>
              {form.formState.errors.email && <p role="alert" className="text-sm text-destructive">{form.formState.errors.email.message}</p>}
            </div>

            <div className="space-y-2">
              <Label htmlFor="register-password">Senha</Label>
              <PasswordInput id="register-password" autoComplete="new-password" placeholder="Mínimo de 6 caracteres" aria-invalid={!!form.formState.errors.password} {...form.register("password")} />
              {form.formState.errors.password && <p role="alert" className="text-sm text-destructive">{form.formState.errors.password.message}</p>}
            </div>

            <div className="space-y-2">
              <Label htmlFor="confirm-password">Confirmar senha</Label>
              <PasswordInput id="confirm-password" autoComplete="new-password" placeholder="Digite a senha novamente" aria-invalid={!!form.formState.errors.confirmPassword} {...form.register("confirmPassword")} />
              {form.formState.errors.confirmPassword && <p role="alert" className="text-sm text-destructive">{form.formState.errors.confirmPassword.message}</p>}
            </div>

            {registerMutation.isError && <p role="alert" className="text-sm text-destructive">{errorMessage}</p>}

            <Button type="submit" className="w-full" disabled={registerMutation.isPending}>
              {registerMutation.isPending ? "Criando conta..." : "Criar conta"}
            </Button>
            <p className="text-center text-sm text-muted-foreground">
              Já tem uma conta?{" "}<Link to="/login" className="font-semibold text-primary-strong underline-offset-4 hover:underline">Entrar</Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </main>
  );
}
