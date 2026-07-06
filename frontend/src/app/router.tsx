import { createBrowserRouter, Navigate } from "react-router-dom";

import { LoginPage } from "@/pages/login/login-page";
import { RegisterPage } from "@/pages/register/register-page";
import { TicketDetailsPage } from "@/pages/tickets/ticket-details-page";
import { TicketsPage } from "@/pages/tickets/tickets-page";
import { ProtectedRoute } from "@/features/auth/components/protected.route"
import { AppLayout } from "@/layouts/app-layout";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <Navigate to="/tickets" replace />,
    },
    {
        path: "/login",
        element: <LoginPage />,
    },
    {
        path: "/register",
        element: <RegisterPage />,
    },
    {
        element: <ProtectedRoute />,
        children: [
            {
                element: <AppLayout />,
                children: [
                    {
                        path: "/tickets",
                        element: <TicketsPage />,
                    },
                    {
                        path: "/tickets/:id",
                        element: <TicketDetailsPage />,
                    },
                ]
            }

        ],
    },
]);