import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";

import { login, getMe } from "@/features/auth/api/auth.api";
import { loginSchema, type LoginFormData } from "@/features/auth/schemas/login.schema";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Headphones, LockKeyhole, Mail } from "lucide-react";

export function LoginPage() {
  const navigate = useNavigate();

  const setToken = useAuthStore((state) => state.setToken);
  const setUser = useAuthStore((state) => state.setUser);

  const form = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const loginMutation = useMutation({
    mutationFn: async (data: LoginFormData) => {
      const loginResponse = await login(data);

      setToken(loginResponse.token);

      const user = await getMe();

      setUser(user);

      return user;
    },
    onSuccess: () => {
      navigate("/tickets");
    },
  });

  function handleSubmit(data: LoginFormData) {
    loginMutation.mutate(data);
  }

  const errorMessage = getApiErrorMessage(
    loginMutation.error,
    "Não foi possível realizar o login. Tente novamente."
  )

  return (
    <main className="auth-background relative flex min-h-screen items-center justify-center overflow-hidden px-4 py-12">
      <div className="absolute left-[8%] top-[12%] size-56 rounded-full border border-white/10" />
      <div className="absolute bottom-[8%] right-[7%] size-80 rounded-full border border-white/8" />
      <Card className="relative w-full max-w-md border-white/20 bg-white/96 px-2 py-2 shadow-[0_30px_80px_-25px_#00000090] backdrop-blur-xl">
        <CardHeader className="pb-2 text-center">
          <div className="mx-auto mb-4 flex size-12 items-center justify-center rounded-2xl bg-gradient-to-br from-[#6f95ff] to-[#5c65c0] text-white shadow-lg shadow-[#5c65c0]/25"><Headphones className="size-6" /></div>
          <p className="text-xs font-bold uppercase tracking-[0.22em] text-[#5c65c0]">Helpdesk</p>
          <CardTitle className="mt-2 text-2xl font-bold tracking-tight">Bem-vindo de volta</CardTitle>
          <CardDescription>
            Acesse sua conta para gerenciar chamados.
          </CardDescription>
        </CardHeader>

        <CardContent>
          <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">E-mail</Label>
              <div className="relative">
                <Mail className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="email"
                  type="email"
                  autoComplete="email"
                  placeholder="voce@email.com"
                  className="pl-10"
                  aria-invalid={!!form.formState.errors.email}
                  aria-describedby={form.formState.errors.email ? "email-error" : undefined}
                  {...form.register("email")}
                />
              </div>
              {form.formState.errors.email && (
                <p id="email-error" role="alert" className="text-sm text-destructive">
                  {form.formState.errors.email.message}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Senha</Label>
              <div className="relative">
                <LockKeyhole className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="password"
                  type="password"
                  autoComplete="current-password"
                  placeholder="Sua senha"
                  className="pl-10"
                  aria-invalid={!!form.formState.errors.password}
                  aria-describedby={form.formState.errors.password ? "password-error" : undefined}
                  {...form.register("password")}
                />
              </div>
              {form.formState.errors.password && (
                <p id="password-error" role="alert" className="text-sm text-destructive">
                  {form.formState.errors.password.message}
                </p>
              )}
            </div>

            {loginMutation.isError && (
              <p role="alert" className="text-sm text-destructive">{errorMessage}</p>
            )}

            <Button
              type="submit"
              className="mt-2 w-full"
              disabled={loginMutation.isPending}
            >
              {loginMutation.isPending ? "Entrando..." : "Entrar"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </main>
  );
}
