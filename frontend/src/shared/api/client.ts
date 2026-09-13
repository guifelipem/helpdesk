import axios from "axios";

import { useAuthStore } from "@/features/auth/store/auth.store";

export const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL ?? "http://localhost:8080/api",
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("helpdesk:token");

    const isAuthRoute = config.url === "/auth/login" || config.url === "/auth/register";

    if (token && !isAuthRoute) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        const status = axios.isAxiosError(error) ? error.response?.status : undefined;
        const url = axios.isAxiosError(error) ? error.config?.url : undefined;
        const isLoginRequest = url === "/auth/login" || url === "/auth/register";

        if (status === 401 && !isLoginRequest) {
            useAuthStore.getState().logout();
            if (window.location.pathname !== "/login") {
                window.location.assign("/login");
            }
        }

        return Promise.reject(error);
    },
);
