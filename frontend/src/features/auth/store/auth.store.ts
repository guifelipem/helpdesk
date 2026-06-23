import { create } from "zustand";

type AuthState = {
    token: string | null;
    isAuthenticated: boolean;
    setToken: (token: string) => void;
    logout: () => void;
};

export const useAuthStore = create<AuthState>((set) => ({
    token: localStorage.getItem("helpdesk:token"),
    isAuthenticated: !!localStorage.getItem("helpdesk:token"),

    setToken: (token) => {
        localStorage.setItem("helpdesk:token", token);

        set({token, isAuthenticated: true,});
    },

    logout: () => {
        localStorage.removeItem("helpdesk:token");

        set({token: null, isAuthenticated: false})
    },
}));