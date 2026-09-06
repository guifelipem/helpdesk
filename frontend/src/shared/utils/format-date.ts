export function formatDate(date: string | Date) {
    return new Intl.DateTimeFormat("pt-BR", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    }).format(new Date(date));
}

export function formatRelativeDate(date: string | Date) {
    const differenceInSeconds = Math.round((new Date(date).getTime() - Date.now()) / 1000);
    const formatter = new Intl.RelativeTimeFormat("pt-BR", { numeric: "auto" });
    const absoluteDifference = Math.abs(differenceInSeconds);

    if (absoluteDifference < 60) {
        return "agora";
    }

    if (absoluteDifference < 3_600) {
        return formatter.format(Math.round(differenceInSeconds / 60), "minute");
    }

    if (absoluteDifference < 86_400) {
        return formatter.format(Math.round(differenceInSeconds / 3_600), "hour");
    }

    return formatter.format(Math.round(differenceInSeconds / 86_400), "day");
}
