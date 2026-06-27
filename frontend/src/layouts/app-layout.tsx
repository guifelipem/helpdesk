import { NavLink, Outlet } from "react-router-dom";
import { useAuthStore } from "@/features/auth/store/auth.store";

export function AppLayout() {

    const { user, logout } = useAuthStore();

    const canSeeDashboard = useAuthStore((state) => state.canAccessDashboard());

    return (
        <div className="min-h-screen bg-slate-100">
            <aside className="fixed left-0 top-0 h-screen w-64 bg-slate-900 px-4 py-6 text-white">
                <h1 className="mb-8 text-x1 font-bold">Help Desk</h1>

                <nav className="space-y-2">
                    <NavLink
                        to="/tickets"
                        className={({ isActive }) =>
                            `block rounded-lg px-3 py-2 ${isActive ? "bg-slate-800" : "hover:bg-slate-800"
                            }`
                        }
                    >
                        Tickets
                    </NavLink>

                    {canSeeDashboard && (
                        <NavLink
                            to="/dashboard"
                            className={({ isActive }) =>
                                `block rounded-lg px-3 py-2 ${isActive ? "bg-slate-800" : "hover:bg-slate-800"
                                }`
                            }
                        >
                            Dashboard
                        </NavLink>
                    )}
                </nav>
            </aside>

            <div className="ml-64 min-h-screen">
                <header className="flex h-16 items-center justify-between border-b bg-white px-6">
                    <div>
                        <p className="text-sm text-slate-500">Usuário logado</p>
                        <strong className="text-slate-900">{user?.name}</strong>
                        <span className="rounded-full bg-slate-100 px-2 py-1 text-xs font-medium text-slate-700">
                            {user?.role}
                        </span>
                    </div>

                    <button
                        type="button"
                        onClick={logout}
                        className="rounded-lg border px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
                    >
                        Sair
                    </button>
                </header>

                <main>
                    <Outlet />
                </main>
            </div>

        </div>
    )
}