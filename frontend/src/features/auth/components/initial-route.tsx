import { Navigate } from "react-router-dom";

import { useAuthStore } from "../store/auth.store";

export function InitialRoute() {
    const role = useAuthStore((state) => state.user?.role);

    return <Navigate to={role === "CLIENT" ? "/home" : "/tickets"} replace />;
}
