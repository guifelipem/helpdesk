export type UserRole =
    | "CLIENT"
    | "AGENT"
    | "ADMIN";

export type User = {
    id: number;
    name: string;
    email: string;
    role: UserRole;
};