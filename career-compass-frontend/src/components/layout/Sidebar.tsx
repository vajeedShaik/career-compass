import { NavLink } from "react-router-dom";
import { Compass, LayoutDashboard, History, Settings, Sun, Moon, LogOut, Sparkles } from "lucide-react";
import { cn } from "@/utils/cn";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Switch } from "@/components/ui/switch";
import { Button } from "@/components/ui/button";
import { useAuthContext } from "@/contexts/AuthContext";
import { useThemeContext } from "@/contexts/ThemeContext";
import { useLogout } from "@/hooks/useAuth";

interface NavItem {
  label: string;
  to: string;
  icon: typeof LayoutDashboard;
  comingSoon?: boolean;
}

const NAV_ITEMS: NavItem[] = [
  { label: "Dashboard", to: "/", icon: LayoutDashboard },
  { label: "New Analysis", to: "/workspace", icon: Sparkles },
  { label: "History", to: "/history", icon: History },
  { label: "Settings", to: "/settings", icon: Settings },
];

interface SidebarProps {
  className?: string;
  onNavigate?: () => void;
}

export function Sidebar({ className, onNavigate }: SidebarProps) {
  const { user } = useAuthContext();
  const { theme, toggleTheme } = useThemeContext();
  const logout = useLogout();

  const initial = user?.username?.charAt(0).toUpperCase() ?? "?";

  return (
    <aside
      className={cn(
        "flex h-full w-64 flex-col border-r border-border bg-card px-4 py-6",
        className
      )}
    >
      <div className="mb-8 flex items-center gap-2 px-2">
        <Compass className="h-6 w-6 text-primary" />
        <span className="text-lg font-semibold tracking-tight">Career Compass</span>
      </div>

      <nav className="flex-1 space-y-1">
        {NAV_ITEMS.map((item) => {
          const Icon = item.icon;
          if (item.comingSoon) {
            return (
              <div
                key={item.label}
                className="flex cursor-not-allowed items-center justify-between rounded-md px-3 py-2 text-sm text-muted-foreground opacity-60"
                aria-disabled
              >
                <span className="flex items-center gap-3">
                  <Icon className="h-4 w-4" />
                  {item.label}
                </span>
                <span className="rounded-full bg-muted px-2 py-0.5 text-[10px] font-medium">
                  Soon
                </span>
              </div>
            );
          }
          return (
            <NavLink
              key={item.label}
              to={item.to}
              end
              onClick={onNavigate}
              className={({ isActive }) =>
                cn(
                  "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                  isActive
                    ? "bg-accent text-accent-foreground"
                    : "text-muted-foreground hover:bg-accent/50 hover:text-foreground"
                )
              }
            >
              <Icon className="h-4 w-4" />
              {item.label}
            </NavLink>
          );
        })}
      </nav>

      <div className="space-y-4 border-t border-border pt-4">
        <div className="flex items-center justify-between px-2">
          <span className="flex items-center gap-2 text-sm text-muted-foreground">
            {theme === "dark" ? <Moon className="h-4 w-4" /> : <Sun className="h-4 w-4" />}
            Theme
          </span>
          <Switch
            checked={theme === "dark"}
            onCheckedChange={toggleTheme}
            aria-label="Toggle dark mode"
          />
        </div>

        <div className="flex items-center gap-3 rounded-md px-2 py-2">
          <Avatar>
            <AvatarFallback>{initial}</AvatarFallback>
          </Avatar>
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium">{user?.username}</p>
            <p className="truncate text-xs text-muted-foreground">{user?.email}</p>
          </div>
        </div>

        <Button variant="outline" size="sm" className="w-full" onClick={logout}>
          <LogOut className="h-4 w-4" />
          Log out
        </Button>
      </div>
    </aside>
  );
}
