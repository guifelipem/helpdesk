import { Navigate, Outlet } from "react-router-dom";

import { useAuthStore } from "../store/auth.store";
import { useAuthMe } from "../hooks/useAuthMe";

export function ProtectedRoute() {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    const user = useAuthStore((state) => state.user);

    const { isPending } = useAuthMe();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />
    }

    if (!user && isPending) {
        return <p>Carregando usuário...</p>;
    }

    return <Outlet />
}