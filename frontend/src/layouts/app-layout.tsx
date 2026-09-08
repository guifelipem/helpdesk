import { Headphones, House, Inbox, LayoutDashboard, ListChecks, LogOut, Ticket, UserRound, UsersRound } from "lucide-react";
import { NavLink, Outlet } from "react-router-dom";

import { useAuthStore } from "@/features/auth/store/auth.store";
import { ThemeToggle } from "@/features/theme/components/theme-toggle";

export function AppLayout() {
    const { user, logout } = useAuthStore();

    const navClass = ({ isActive }: { isActive: boolean }) =>
        `group flex items-center gap-3 rounded-xl px-3.5 py-3 text-sm font-medium transition-all ${
            isActive
                ? "bg-white/18 text-white shadow-[inset_0_0_0_1px_#ffffff24]"
                : "text-white/80 hover:bg-white/10 hover:text-white"
        }`;

    return (
        <div className="app-shell-background min-h-screen lg:grid lg:grid-cols-[260px_1fr]">
            <aside className="border-b border-white/10 bg-gradient-to-b from-secondary to-accent px-4 py-4 text-white lg:fixed lg:inset-y-0 lg:left-0 lg:w-[260px] lg:border-b-0 lg:border-r lg:px-5 lg:py-6 dark:from-background dark:to-[#4657a9]">
                <div className="flex items-center justify-between lg:block">
                    <div className="flex items-center gap-3 px-2">
                        <div className="flex size-10 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-accent text-primary-foreground shadow-lg shadow-primary/20">
                            <Headphones className="size-5" />
                        </div>
                        <div>
                            <p className="text-lg font-bold tracking-tight">Helpdesk</p>
                            <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-white/70">Support center</p>
                        </div>
                    </div>

                    <div className="flex items-center gap-1 lg:hidden">
                        <ThemeToggle className="border-white/15 bg-white/10 text-white hover:bg-white/20 hover:text-white" />
                        <button type="button" onClick={logout} className="rounded-xl p-2 text-white/80 transition hover:bg-white/10 hover:text-white" aria-label="Sair">
                            <LogOut className="size-5" />
                        </button>
                    </div>
                </div>

                <nav className="mt-4 flex gap-2 lg:mt-10 lg:flex-col">
                    {user?.role === "CLIENT" ? (
                        <>
                            <NavLink to="/home" className={navClass}>
                                <House className="size-4 text-white/75" />
                                Início
                            </NavLink>
                            <NavLink to="/tickets" className={navClass}>
                                <ListChecks className="size-4 text-white/75" />
                                Meus chamados
                            </NavLink>
                        </>
                    ) : user?.role === "AGENT" ? (
                        <>
                            <NavLink to="/agent/dashboard" className={navClass}>
                                <House className="size-4 text-white/75" />
                                Início
                            </NavLink>
                            <NavLink to="/queues" className={navClass}>
                                <Inbox className="size-4 text-white/75" />
                                Filas
                            </NavLink>
                            <NavLink to="/tickets" className={navClass}>
                                <Ticket className="size-4 text-white/75" />
                                Chamados
                            </NavLink>
                        </>
                    ) : (
                        <>
                            <NavLink to="/dashboard" className={navClass}>
                                <LayoutDashboard className="size-4 text-white/75" />
                                Dashboard
                            </NavLink>
                            <NavLink to="/tickets" className={navClass}>
                                <Ticket className="size-4 text-white/75" />
                                Chamados
                            </NavLink>
                        </>
                    )}

                    {user?.role === "ADMIN" && (
                        <NavLink to="/users" className={navClass}>
                            <UsersRound className="size-4 text-white/75" />
                            Usuários
                        </NavLink>
                    )}

                    <NavLink to="/account" className={navClass}>
                        <UserRound className="size-4 text-white/75" />
                        Minha conta
                    </NavLink>

                </nav>

                <div className="absolute bottom-6 left-5 right-5 hidden rounded-2xl border border-white/10 bg-white/10 p-3 lg:block">
                    <div className="flex items-center gap-3">
                        <div className="flex size-9 items-center justify-center rounded-xl bg-white/15 text-white"><UserRound className="size-4" /></div>
                        <NavLink to="/account" className="min-w-0 flex-1 rounded-lg outline-none focus-visible:ring-2 focus-visible:ring-primary">
                            <p className="truncate text-sm font-semibold">{user?.name}</p>
                            <p className="text-[10px] font-semibold uppercase tracking-wider text-white/70">{user?.role}</p>
                        </NavLink>
                        <button type="button" onClick={logout} className="rounded-lg p-2 text-white/70 transition hover:bg-white/10 hover:text-white" aria-label="Sair"><LogOut className="size-4" /></button>
                    </div>
                </div>
            </aside>

            <div className="min-w-0 lg:col-start-2">
                <header className="hidden h-20 items-center justify-end border-b border-border bg-card/70 px-8 backdrop-blur-xl dark:bg-surface-elevated/85 lg:flex">
                    <div className="flex items-center gap-3 text-right">
                        <ThemeToggle className="mr-2" />
                        <div><p className="text-sm font-semibold text-foreground">{user?.name}</p><p className="text-xs text-muted-foreground">Bem-vindo de volta</p></div>
                        <div className="flex size-10 items-center justify-center rounded-full bg-secondary text-secondary-foreground ring-4 ring-card"><UserRound className="size-4" /></div>
                    </div>
                </header>

                <main className="mx-auto w-full max-w-[1440px] p-4 sm:p-6 lg:p-8 xl:p-10"><Outlet /></main>
            </div>
        </div>
    );
}
