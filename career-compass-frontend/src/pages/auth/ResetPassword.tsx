import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useSearchParams } from "react-router-dom";
import { Loader2, AlertCircle } from "lucide-react";
import { AuthLayout } from "@/layouts/AuthLayout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { PasswordStrengthMeter } from "@/components/auth/PasswordStrengthMeter";
import { resetPasswordSchema, type ResetPasswordFormValues } from "@/utils/schemas/auth";
import { useResetPassword } from "@/hooks/useAuth";

export default function ResetPassword() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");
    const resetPassword = useResetPassword();

    const {
        register,
        handleSubmit,
        watch,
        formState: { errors },
    } = useForm<ResetPasswordFormValues>({
        resolver: zodResolver(resetPasswordSchema),
    });

    const passwordValue = watch("newPassword") ?? "";

    function onSubmit(values: ResetPasswordFormValues) {
        if (!token || resetPassword.isPending) return;
        resetPassword.mutate({ token, newPassword: values.newPassword });
    }

    if (!token) {
        return (
            <AuthLayout title="Invalid link" subtitle="Something's not right">
                <div className="flex flex-col items-center gap-3 text-center">
                    <AlertCircle className="h-8 w-8 text-destructive" />
                    <p className="text-sm text-muted-foreground">
                        This reset link is missing its token. Please request a new one.
                    </p>
                    <Link to="/forgot-password" className="text-sm font-medium text-primary hover:underline">
                        Request a new link
                    </Link>
                </div>
            </AuthLayout>
        );
    }

    return (
        <AuthLayout title="Set a new password" subtitle="Choose a strong new password">
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5" noValidate>
                <div className="space-y-2">
                    <Label htmlFor="newPassword">New password</Label>
                    <Input
                        id="newPassword"
                        type="password"
                        autoComplete="new-password"
                        placeholder="••••••••"
                        aria-invalid={!!errors.newPassword}
                        {...register("newPassword")}
                    />
                    <PasswordStrengthMeter password={passwordValue} />
                    {errors.newPassword && (
                        <p className="text-xs text-destructive">{errors.newPassword.message}</p>
                    )}
                </div>

                <div className="space-y-2">
                    <Label htmlFor="confirmPassword">Confirm new password</Label>
                    <Input
                        id="confirmPassword"
                        type="password"
                        autoComplete="new-password"
                        placeholder="••••••••"
                        aria-invalid={!!errors.confirmPassword}
                        {...register("confirmPassword")}
                    />
                    {errors.confirmPassword && (
                        <p className="text-xs text-destructive">{errors.confirmPassword.message}</p>
                    )}
                </div>

                <Button type="submit" className="w-full" disabled={resetPassword.isPending}>
                    {resetPassword.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                    Reset password
                </Button>
            </form>
        </AuthLayout>
    );
}