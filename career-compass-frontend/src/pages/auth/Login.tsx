import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link } from "react-router-dom";
import { Loader2 } from "lucide-react";

import { AuthLayout } from "@/layouts/AuthLayout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

import { loginSchema, type LoginFormValues } from "@/utils/schemas/auth";
import { useLogin } from "@/hooks/useAuth";

export default function Login() {
  const login = useLogin();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  });

  function onSubmit(values: LoginFormValues) {
    if (login.isPending) return;

    login.mutate(values);
  }

  const submitHandler = handleSubmit(onSubmit);

  return (
      <AuthLayout
          title="Welcome back"
          subtitle="Sign in to continue to your dashboard"
      >
        <div className="space-y-5">
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
                autoComplete="current-password"
                placeholder="••••••••"
                aria-invalid={!!errors.password}
                {...register("password")}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    e.preventDefault();
                    submitHandler();
                  }
                }}
            />

            {errors.password && (
                <p className="text-xs text-destructive">
                  {errors.password.message}
                </p>
            )}
          </div>

          {/* Sign In Button */}
          <Button
              type="button"
              className="w-full"
              disabled={login.isPending}
              onClick={() => submitHandler()}
          >
            {login.isPending && (
                <Loader2 className="h-4 w-4 animate-spin" />
            )}
            Sign in
          </Button>

          {/* Forgot Password */}
          <p className="text-center text-sm">
            <Link
                to="/forgot-password"
                className="text-muted-foreground hover:text-primary hover:underline"
            >
              Forgot your password?
            </Link>
          </p>
        </div>

        {/* Register */}
        <p className="mt-6 text-center text-sm text-muted-foreground">
          Don't have an account?{" "}
          <Link
              to="/register"
              className="font-medium text-primary hover:underline"
          >
            Create one
          </Link>
        </p>
      </AuthLayout>
  );
}
