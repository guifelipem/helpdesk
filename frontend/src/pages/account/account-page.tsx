import { zodResolver } from "@hookform/resolvers/zod";
import { KeyRound, Mail, Save, ShieldCheck, UserRound } from "lucide-react";
import { useEffect } from "react";
import { useForm } from "react-hook-form";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { PasswordInput } from "@/features/auth/components/password-input";
import type { UserRole } from "@/features/auth/types/user.types";
import { useAccount, useChangePassword, useUpdateAccount } from "@/features/account/hooks/use-account";
import {
    accountSchema,
    changePasswordSchema,
    type AccountFormData,
    type ChangePasswordFormData,
} from "@/features/account/schemas/account.schema";
import { ErrorState } from "@/shared/components/error-state";
import { getApiErrorMessage } from "@/shared/utils/get-api-error-message";

const roleLabels: Record<UserRole, string> = {
    CLIENT: "Cliente",
    AGENT: "Agente",
    ADMIN: "Administrador",
};

export function AccountPage() {
    const account = useAccount();
    const updateAccount = useUpdateAccount();
    const changePassword = useChangePassword();
    const accountForm = useForm<AccountFormData>({
        resolver: zodResolver(accountSchema),
        defaultValues: { name: "", email: "" },
    });
    const passwordForm = useForm<ChangePasswordFormData>({
        resolver: zodResolver(changePasswordSchema),
        defaultValues: { currentPassword: "", newPassword: "", confirmPassword: "" },
    });

    useEffect(() => {
        if (account.data) {
            accountForm.reset({ name: account.data.name, email: account.data.email });
        }
    }, [account.data, accountForm]);

    function submitAccount(data: AccountFormData) {
        updateAccount.reset();
        updateAccount.mutate(data);
    }

    function submitPassword(data: ChangePasswordFormData) {
        changePassword.reset();
        changePassword.mutate(
            { currentPassword: data.currentPassword, newPassword: data.newPassword },
            { onSuccess: () => passwordForm.reset() },
        );
    }

    if (account.isError) {
        return (
            <ErrorState
                title="Não foi possível carregar sua conta"
                description={getApiErrorMessage(account.error, "Tente novamente em alguns instantes.")}
                onRetry={() => account.refetch()}
                isRetrying={account.isFetching}
            />
        );
    }

    return (
        <div className="space-y-7">
            <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#272e62] via-[#373384] to-[#4794b8] px-6 py-8 text-white shadow-[0_25px_60px_-30px_#030607] sm:px-8">
                <div className="absolute -right-12 -top-20 size-64 rounded-full border-[32px] border-white/5" />
                <div className="relative flex flex-col gap-5 sm:flex-row sm:items-center">
                    <div className="flex size-16 shrink-0 items-center justify-center rounded-2xl bg-white/15 ring-1 ring-white/20 backdrop-blur-sm">
                        <UserRound className="size-8" />
                    </div>
                    <div>
                        <p className="mb-2 text-xs font-bold uppercase tracking-[0.18em] text-white/70">Perfil do usuário</p>
                        <h1 className="text-3xl font-bold tracking-tight">Minha conta</h1>
                        <p className="mt-2 text-sm text-white/70">Gerencie seus dados pessoais e mantenha sua senha atualizada.</p>
                    </div>
                </div>
            </section>

            {account.isPending ? (
                <div className="grid gap-6 lg:grid-cols-2">
                    <div className="h-96 animate-pulse rounded-2xl bg-muted" />
                    <div className="h-96 animate-pulse rounded-2xl bg-muted" />
                </div>
            ) : (
                <div className="grid items-start gap-6 lg:grid-cols-2">
                    <Card>
                        <CardHeader className="border-b">
                            <CardTitle className="flex items-center gap-2 text-lg"><UserRound className="size-5 text-primary-strong" /> Dados pessoais</CardTitle>
                            <CardDescription>Atualize seu nome e o e-mail usado para entrar no sistema.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <form onSubmit={accountForm.handleSubmit(submitAccount)} className="space-y-5">
                                <div className="space-y-2">
                                    <Label htmlFor="account-name">Nome</Label>
                                    <div className="relative">
                                        <UserRound className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                                        <Input id="account-name" autoComplete="name" className="pl-10" aria-invalid={!!accountForm.formState.errors.name} {...accountForm.register("name")} />
                                    </div>
                                    {accountForm.formState.errors.name && <p role="alert" className="text-sm text-destructive">{accountForm.formState.errors.name.message}</p>}
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="account-email">E-mail</Label>
                                    <div className="relative">
                                        <Mail className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                                        <Input id="account-email" type="email" autoComplete="email" className="pl-10" aria-invalid={!!accountForm.formState.errors.email} {...accountForm.register("email")} />
                                    </div>
                                    {accountForm.formState.errors.email && <p role="alert" className="text-sm text-destructive">{accountForm.formState.errors.email.message}</p>}
                                </div>

                                <div className="space-y-2">
                                    <Label>Role</Label>
                                    <div className="flex h-11 items-center justify-between rounded-xl border border-border bg-muted/55 px-3.5">
                                        <span className="flex items-center gap-2 text-sm text-muted-foreground"><ShieldCheck className="size-4" /> Perfil de acesso</span>
                                        <Badge variant="outline">{roleLabels[account.data.role]}</Badge>
                                    </div>
                                    <p className="text-xs text-muted-foreground">Seu perfil de acesso é definido pela administração e não pode ser alterado aqui.</p>
                                </div>

                                {updateAccount.isSuccess && <p role="status" className="rounded-xl border border-emerald-300 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800 dark:border-emerald-800 dark:bg-emerald-950/45 dark:text-emerald-200">Dados atualizados com sucesso.</p>}
                                {updateAccount.isError && <p role="alert" className="rounded-xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">{getApiErrorMessage(updateAccount.error, "Não foi possível atualizar seus dados.")}</p>}

                                <Button type="submit" disabled={updateAccount.isPending || !accountForm.formState.isDirty}>
                                    <Save /> {updateAccount.isPending ? "Salvando..." : "Salvar alterações"}
                                </Button>
                            </form>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader className="border-b">
                            <CardTitle className="flex items-center gap-2 text-lg"><KeyRound className="size-5 text-primary-strong" /> Alterar senha</CardTitle>
                            <CardDescription>Por segurança, confirme sua senha atual antes de escolher uma nova.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <form onSubmit={passwordForm.handleSubmit(submitPassword)} className="space-y-5">
                                <div className="space-y-2">
                                    <Label htmlFor="current-password">Senha atual</Label>
                                    <PasswordInput id="current-password" autoComplete="current-password" placeholder="Digite sua senha atual" aria-invalid={!!passwordForm.formState.errors.currentPassword} {...passwordForm.register("currentPassword")} />
                                    {passwordForm.formState.errors.currentPassword && <p role="alert" className="text-sm text-destructive">{passwordForm.formState.errors.currentPassword.message}</p>}
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="new-password">Nova senha</Label>
                                    <PasswordInput id="new-password" autoComplete="new-password" placeholder="Mínimo de 6 caracteres" aria-invalid={!!passwordForm.formState.errors.newPassword} {...passwordForm.register("newPassword")} />
                                    {passwordForm.formState.errors.newPassword && <p role="alert" className="text-sm text-destructive">{passwordForm.formState.errors.newPassword.message}</p>}
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="confirm-new-password">Confirmar nova senha</Label>
                                    <PasswordInput id="confirm-new-password" autoComplete="new-password" placeholder="Digite a nova senha novamente" aria-invalid={!!passwordForm.formState.errors.confirmPassword} {...passwordForm.register("confirmPassword")} />
                                    {passwordForm.formState.errors.confirmPassword && <p role="alert" className="text-sm text-destructive">{passwordForm.formState.errors.confirmPassword.message}</p>}
                                </div>

                                {changePassword.isSuccess && <p role="status" className="rounded-xl border border-emerald-300 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800 dark:border-emerald-800 dark:bg-emerald-950/45 dark:text-emerald-200">Senha alterada com sucesso.</p>}
                                {changePassword.isError && <p role="alert" className="rounded-xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">{getApiErrorMessage(changePassword.error, "Não foi possível alterar sua senha.")}</p>}

                                <Button type="submit" disabled={changePassword.isPending}>
                                    <KeyRound /> {changePassword.isPending ? "Alterando..." : "Alterar senha"}
                                </Button>
                            </form>
                        </CardContent>
                    </Card>
                </div>
            )}
        </div>
    );
}
