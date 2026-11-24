import { useQuery } from "@tanstack/react-query";
import { getHistory } from "@/api/history";

export function useHistory() {
    return useQuery({
        queryKey: ["history"],
        queryFn: getHistory,
    });
}