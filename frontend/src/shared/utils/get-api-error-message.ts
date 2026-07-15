import axios from "axios";

import type { ApiErrorResponse } from "../types/api-error-response";

export function getApiErrorMessage(
    error: unknown,
    fallbackMessage: string
): string {
    if (!axios.isAxiosError<ApiErrorResponse>(error)) {
        return fallbackMessage;
    }

    return error.response?.data?.message ?? fallbackMessage;
}