import { useQuery } from "@tanstack/react-query";
import { getDashboard } from "@/api/dashboard";

/**
 * A read (GET), so this uses useQuery, not useMutation — unlike the
 * upload/match/roadmap flows in Workspace, which are user-triggered
 * actions. This fetches automatically when Dashboard mounts.
 */
export function useDashboard() {
    return useQuery({
        queryKey: ["dashboard"],
        queryFn: getDashboard,
    });
}