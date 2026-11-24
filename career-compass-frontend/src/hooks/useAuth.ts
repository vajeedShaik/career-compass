import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { loginUser, registerUser, forgotPassword, resetPassword } from "@/api/auth";
import { useAuthContext } from "@/contexts/AuthContext";
import type {
  LoginPayload,
  RegisterPayload,
  ForgotPasswordPayload,
  ResetPasswordPayload,
} from "@/types/auth";
import { isAxiosError } from "axios";

function extractErrorMessage(error: unknown, fallback: string): string {
  if (isAxiosError(error) && typeof error.response?.data?.message === "string") {
    return error.response.data.message;
  }
  return fallback;
}

export function useLogin() {
  const { setSession } = useAuthContext();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (payload: LoginPayload) => loginUser(payload),
    onSuccess: (data) => {
      setSession(data);
      toast.success("Welcome back!");
      navigate("/", { replace: true });
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error, "Invalid email or password"));
    },
  });
}

export function useRegister() {
  const { setSession } = useAuthContext();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (payload: RegisterPayload) => registerUser(payload),
    onSuccess: (data) => {
      setSession(data);
      toast.success("Account created successfully!");
      navigate("/", { replace: true });
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error, "Could not create your account"));
    },
  });
}

export function useForgotPassword() {
  return useMutation({
    mutationFn: (payload: ForgotPasswordPayload) => forgotPassword(payload),
    onSuccess: () => {
      toast.success("If that email exists, a reset link has been sent.");
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error, "Something went wrong. Please try again."));
    },
  });
}

export function useResetPassword() {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (payload: ResetPasswordPayload) => resetPassword(payload),
    onSuccess: () => {
      toast.success("Password reset successfully. Please sign in.");
      navigate("/login", { replace: true });
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error, "This reset link is invalid or expired."));
    },
  });
}

export function useLogout() {
  const { clearSession } = useAuthContext();
  const navigate = useNavigate();

  return () => {
    clearSession();
    navigate("/login", { replace: true });
  };
}