import { useQuery } from "@tanstack/react-query";
import axios from "axios";

import { getMe } from "../api/auth.api";
import { useAuthStore } from "../store/auth.store";

export function useAuthMe() {
    const token = useAuthStore((state) => state.token);
    const user = useAuthStore((state) => state.user);
    const setUser = useAuthStore((state) => state.setUser);
    const logout = useAuthStore((state) => state.logout);

    return useQuery({
        queryKey: ["auth", "me"],
        queryFn: async () => {
            try {
                const user = await getMe();
                setUser(user);
                return user;
            } catch (error) {
                if (axios.isAxiosError(error) && error.response?.status === 401) {
                    logout();
                }

                throw error;
            }
        },
        enabled: !!token && !user,
        retry: false,
    });
}