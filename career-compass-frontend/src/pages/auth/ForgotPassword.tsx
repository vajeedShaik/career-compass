import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link } from "react-router-dom";
import { Loader2 } from "lucide-react";
import { AuthLayout } from "@/layouts/AuthLayout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { forgotPasswordSchema, type ForgotPasswordFormValues } from "@/utils/schemas/auth";
import { useForgotPassword } from "@/hooks/useAuth";

export default function ForgotPassword() {
    const forgotPassword = useForgotPassword();

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<ForgotPasswordFormValues>({
        resolver: zodResolver(forgotPasswordSchema),
    });

    function onSubmit(values: ForgotPasswordFormValues) {
        if (forgotPassword.isPending) return;
        forgotPassword.mutate(values);
    }

    return (
        <AuthLayout
            title="Reset your password"
            subtitle="Enter your email and we'll send you a reset link"
        >
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5" noValidate>
                <div className="space-y-2">
                    <Label htmlFor="email">Email</Label>
                    <Input
                        id="email"
                        type="email"
                        autoComplete="email"
                        placeholder="you@example.com"
                        aria-invalid={!!errors.email}
                        {...register("email")}
                    />
                    {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
                </div>

                <Button type="submit" className="w-full" disabled={forgotPassword.isPending}>
                    {forgotPassword.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                    Send reset link
                </Button>
            </form>

            <p className="mt-6 text-center text-sm text-muted-foreground">
                Remembered your password?{" "}
                <Link to="/login" className="font-medium text-primary hover:underline">
                    Sign in
                </Link>
            </p>
        </AuthLayout>
    );
}