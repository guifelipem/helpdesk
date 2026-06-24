import { create } from "zustand";

import type { User } from "@/features/auth/types/user.types";

type AuthState = {
    token: string | null;
    user: User | null;
    isAuthenticated: boolean;

    setToken: (token: string) => void;
    setUser: (user: User) => void;
    logout: () => void;
};

export const useAuthStore = create<AuthState>((set) => ({
    token: localStorage.getItem("helpdesk:token"),
    user: null,

    isAuthenticated: !!localStorage.getItem("helpdesk:token"),

    setToken: (token) => {
        localStorage.setItem("helpdesk:token", token);

        set({ token, isAuthenticated: true, });
    },

    setUser: (user) => {
        set({ user })
    },

    logout: () => {
        localStorage.removeItem("helpdesk:token");

        set({token: null, user: null, isAuthenticated: false,})
    },
}));

