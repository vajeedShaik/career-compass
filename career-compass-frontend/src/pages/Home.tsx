import { useAuthContext } from "@/contexts/AuthContext";
import { useLogout } from "@/hooks/useAuth";
import { Button } from "@/components/ui/button";

/**
 * Intentionally minimal — verifies the auth flow (session persistence,
 * protected routing, logout) actually works end to end. The real Dashboard
 * UI is built in a later phase, on top of this same route.
 */
export default function Home() {
  const { user } = useAuthContext();
  const logout = useLogout();

  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-background text-foreground">
      <p className="text-lg">
        Signed in as <span className="font-medium">{user?.email}</span>
      </p>
      <p className="text-sm text-muted-foreground">
        Dashboard UI arrives in a later phase.
      </p>
      <Button variant="outline" onClick={logout}>
        Log out
      </Button>
    </div>
  );
}
