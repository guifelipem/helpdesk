import { Navigate } from "react-router-dom";

import { useAuthStore } from "../store/auth.store";

export function InitialRoute() {
    const role = useAuthStore((state) => state.user?.role);

    const destination = role === "CLIENT" ? "/home" : role === "AGENT" ? "/queues" : "/tickets";

    return <Navigate to={destination} replace />;
}
