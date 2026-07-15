import axios from "axios";

export const api = axios.create({
    baseURL: "http://localhost:8080/api",
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("helpdesk:token");

    const isAuthRoute = config.url === "/auth/login" || config.url === "/auth/register";

    if (token && !isAuthRoute) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});