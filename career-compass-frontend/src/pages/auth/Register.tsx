import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link } from "react-router-dom";
import { Loader2 } from "lucide-react";

import { AuthLayout } from "@/layouts/AuthLayout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { PasswordStrengthMeter } from "@/components/auth/PasswordStrengthMeter";

import {
  registerSchema,
  type RegisterFormValues,
} from "@/utils/schemas/auth";

import { useRegister } from "@/hooks/useAuth";

export default function Register() {
  const registerUser = useRegister();

  const {
    register,
    watch,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
  });

  const passwordValue = watch("password") ?? "";

  function onSubmit(values: RegisterFormValues) {
    if (registerUser.isPending) return;

    registerUser.mutate({
      username: values.username,
      email: values.email,
      password: values.password,
    });
  }

  return (
      <AuthLayout
          title="Create your account"
          subtitle="Start building your Career Compass"
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          {/* Username */}
          <div className="space-y-2">
            <Label htmlFor="username">Username</Label>

            <Input
                id="username"
                type="text"
                autoComplete="username"
                placeholder="e.g. vajeed"
                aria-invalid={!!errors.username}
                {...register("username")}
            />

            {errors.username && (
                <p className="text-xs text-destructive">
                  {errors.username.message}
                </p>
            )}
          </div>

          {/* Email */}
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

            {errors.email && (
                <p className="text-xs text-destructive">
                  {errors.email.message}
                </p>
            )}
          </div>

          {/* Password */}
          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>

            <Input
                id="password"
                type="password"
                autoComplete="new-password"
                placeholder="••••••••"
                aria-invalid={!!errors.password}
                {...register("password")}
            />

            <PasswordStrengthMeter password={passwordValue} />

            {errors.password && (
                <p className="text-xs text-destructive">
                  {errors.password.message}
                </p>
            )}
          </div>

          {/* Confirm Password */}
          <div className="space-y-2">
            <Label htmlFor="confirmPassword">Confirm password</Label>

            <Input
                id="confirmPassword"
                type="password"
                autoComplete="new-password"
                placeholder="••••••••"
                aria-invalid={!!errors.confirmPassword}
                {...register("confirmPassword")}
            />

            {errors.confirmPassword && (
                <p className="text-xs text-destructive">
                  {errors.confirmPassword.message}
                </p>
            )}
          </div>

          {/* Create Account */}
          <Button
              type="submit"
              className="w-full"
              disabled={registerUser.isPending}
          >
            {registerUser.isPending && (
                <Loader2 className="h-4 w-4 animate-spin" />
            )}

            Create account
          </Button>
        </form>

        {/* Login */}
        <p className="mt-6 text-center text-sm text-muted-foreground">
          Already have an account?{" "}
          <Link
              to="/login"
              className="font-medium text-primary hover:underline"
          >
            Sign in
          </Link>
        </p>
      </AuthLayout>
  );
}