import { createBrowserRouter } from "react-router-dom";

import { LoginPage } from "@/pages/login/login-page";
import { RegisterPage } from "@/pages/register/register-page";
import { TicketDetailsPage } from "@/pages/tickets/ticket-details-page";
import { TicketsPage } from "@/pages/tickets/tickets-page";
import { CreateTicketPage } from "@/pages/tickets/create-ticket-page";
import { UsersPage } from "@/pages/users/users-page";
import { ClientHomePage } from "@/pages/home/client-home-page";
import { TicketQueuesPage } from "@/pages/queues/ticket-queues-page";
import { AdminDashboardPage } from "@/pages/dashboard/admin-dashboard-page";
import { AgentDashboardPage } from "@/pages/dashboard/agent-dashboard-page";
import { ProtectedRoute } from "@/features/auth/components/protected.route"
import { InitialRoute } from "@/features/auth/components/initial-route";
import { AppLayout } from "@/layouts/app-layout";

export const router = createBrowserRouter([
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
                        path: "/",
                        element: <InitialRoute />,
                    },
                    {
                        path: "/home",
                        element: <ClientHomePage />,
                    },
                    {
                        path: "/dashboard",
                        element: <AdminDashboardPage />,
                    },
                    {
                        path: "/agent/dashboard",
                        element: <AgentDashboardPage />,
                    },
                    {
                        path: "/tickets",
                        element: <TicketsPage />,
                    },
                    {
                        path: "/queues",
                        element: <TicketQueuesPage />,
                    },
                    {
                        path: "/tickets/:id",
                        element: <TicketDetailsPage />,
                    },
                    {
                        path: "/tickets/new",
                        element: <CreateTicketPage />,
                    },
                    {
                        path: "/users",
                        element: <UsersPage />,
                    }
                ]
            }

        ],
    },
]);
