import { Headphones, LayoutDashboard, LogOut, Ticket, UserRound } from "lucide-react";
import { NavLink, Outlet } from "react-router-dom";

import { useAuthStore } from "@/features/auth/store/auth.store";

export function AppLayout() {
    const { user, logout } = useAuthStore();
    const canSeeDashboard = useAuthStore((state) => state.canAccessDashboard());

    const navClass = ({ isActive }: { isActive: boolean }) =>
        `group flex items-center gap-3 rounded-xl px-3.5 py-3 text-sm font-medium transition-all ${
            isActive
                ? "bg-white/12 text-white shadow-[inset_0_0_0_1px_#ffffff14]"
                : "text-white/65 hover:bg-white/7 hover:text-white"
        }`;

    return (
        <div className="app-shell-background min-h-screen lg:grid lg:grid-cols-[260px_1fr]">
            <aside className="border-b border-white/10 bg-[#1c0b2b] px-4 py-4 text-white lg:fixed lg:inset-y-0 lg:left-0 lg:w-[260px] lg:border-b-0 lg:border-r lg:px-5 lg:py-6">
                <div className="flex items-center justify-between lg:block">
                    <div className="flex items-center gap-3 px-2">
                        <div className="flex size-10 items-center justify-center rounded-xl bg-gradient-to-br from-[#6f95ff] to-[#5c65c0] shadow-lg shadow-[#6f95ff]/20">
                            <Headphones className="size-5" />
                        </div>
                        <div>
                            <p className="text-lg font-bold tracking-tight">Helpdesk</p>
                            <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-[#6f95ff]">Support center</p>
                        </div>
                    </div>

                    <button type="button" onClick={logout} className="rounded-xl p-2 text-white/60 transition hover:bg-white/10 hover:text-white lg:hidden" aria-label="Sair">
                        <LogOut className="size-5" />
                    </button>
                </div>

                <nav className="mt-4 flex gap-2 lg:mt-10 lg:flex-col">
                    <NavLink to="/tickets" className={navClass}>
                        <Ticket className="size-4 text-[#6f95ff]" />
                        Tickets
                    </NavLink>

                    {canSeeDashboard && (
                        <NavLink to="/dashboard" className={navClass}>
                            <LayoutDashboard className="size-4 text-[#6f95ff]" />
                            Dashboard
                        </NavLink>
                    )}
                </nav>

                <div className="absolute bottom-6 left-5 right-5 hidden rounded-2xl border border-white/10 bg-white/6 p-3 lg:block">
                    <div className="flex items-center gap-3">
                        <div className="flex size-9 items-center justify-center rounded-xl bg-[#413b6b] text-[#aebfff]"><UserRound className="size-4" /></div>
                        <div className="min-w-0 flex-1">
                            <p className="truncate text-sm font-semibold">{user?.name}</p>
                            <p className="text-[10px] font-semibold uppercase tracking-wider text-white/45">{user?.role}</p>
                        </div>
                        <button type="button" onClick={logout} className="rounded-lg p-2 text-white/45 transition hover:bg-white/10 hover:text-white" aria-label="Sair"><LogOut className="size-4" /></button>
                    </div>
                </div>
            </aside>

            <div className="min-w-0 lg:col-start-2">
                <header className="hidden h-20 items-center justify-end border-b border-[#413b6b]/10 bg-white/60 px-8 backdrop-blur-xl lg:flex">
                    <div className="flex items-center gap-3 text-right">
                        <div><p className="text-sm font-semibold text-[#301c41]">{user?.name}</p><p className="text-xs text-muted-foreground">Bem-vindo de volta</p></div>
                        <div className="flex size-10 items-center justify-center rounded-full bg-[#ececff] text-[#5c65c0] ring-4 ring-white"><UserRound className="size-4" /></div>
                    </div>
                </header>

                <main className="mx-auto w-full max-w-[1440px] p-4 sm:p-6 lg:p-8 xl:p-10"><Outlet /></main>
            </div>
        </div>
    );
}
