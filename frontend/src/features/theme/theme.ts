export type Theme = "light" | "dark";

const THEME_STORAGE_KEY = "helpdesk:theme";
const listeners = new Set<() => void>();

export function getTheme(): Theme {
    return document.documentElement.classList.contains("dark") ? "dark" : "light";
}

export function initializeTheme() {
    const storedTheme = localStorage.getItem(THEME_STORAGE_KEY);
    document.documentElement.classList.toggle("dark", storedTheme === "dark");
}

export function setTheme(theme: Theme) {
    document.documentElement.classList.toggle("dark", theme === "dark");
    localStorage.setItem(THEME_STORAGE_KEY, theme);
    listeners.forEach((listener) => listener());
}

export function subscribeToTheme(listener: () => void) {
    listeners.add(listener);
    return () => {
        listeners.delete(listener);
    };
}
