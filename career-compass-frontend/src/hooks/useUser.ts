import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";
import { isAxiosError } from "axios";
import { updateUsername, deleteAccount } from "@/api/users";
import { useAuthContext } from "@/contexts/AuthContext";
import { useLogout } from "@/hooks/useAuth";

export function useUpdateUsername() {
    const { updateUsername: setLocalUsername } = useAuthContext();

    return useMutation({
        mutationFn: (username: string) => updateUsername(username),
        onSuccess: (data) => {
            setLocalUsername(data.username);
            toast.success("Username updated");
        },
        onError: (error) => {
            const message = isAxiosError(error)
                ? error.response?.data?.message ?? "Failed to update username"
                : "Failed to update username";
            toast.error(message);
        },
    });
}

export function useDeleteAccount() {
    const logout = useLogout();

    return useMutation({
        mutationFn: () => deleteAccount(),
        onSuccess: () => {
            toast.success("Your account has been deleted");
            logout();
        },
        onError: () => {
            toast.error("Failed to delete account");
        },
    });
}