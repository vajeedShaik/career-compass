import { useState } from "react";
import { Sun, Moon, LogOut, Mail, Trash2, Loader2, Pencil } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Switch } from "@/components/ui/switch";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useAuthContext } from "@/contexts/AuthContext";
import { useThemeContext } from "@/contexts/ThemeContext";
import { useLogout } from "@/hooks/useAuth";
import { useUpdateUsername, useDeleteAccount } from "@/hooks/useUser";

export default function Settings() {
  const { user } = useAuthContext();
  const { theme, toggleTheme } = useThemeContext();
  const logout = useLogout();
  const updateUsername = useUpdateUsername();
  const deleteAccount = useDeleteAccount();

  const [isEditing, setIsEditing] = useState(false);
  const [usernameInput, setUsernameInput] = useState(user?.username ?? "");

  function handleSaveUsername() {
    const trimmed = usernameInput.trim();
    if (!trimmed || trimmed === user?.username) {
      setIsEditing(false);
      return;
    }
    updateUsername.mutate(trimmed, { onSuccess: () => setIsEditing(false) });
  }

  function handleDeleteAccount() {
    const confirmed = window.confirm(
        "Delete your account permanently? This removes all your resumes, job descriptions, matches, and roadmaps. This cannot be undone."
    );
    if (confirmed) {
      deleteAccount.mutate();
    }
  }

  return (
      <div className="mx-auto max-w-2xl space-y-6">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Settings</h1>
          <p className="mt-1 text-sm text-muted-foreground">Manage your account and preferences.</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">Account</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-2 text-sm">
              <Mail className="h-4 w-4 text-muted-foreground" />
              {user?.email}
            </div>

            <div>
              <p className="mb-1.5 text-xs font-medium text-muted-foreground">Username</p>
              {isEditing ? (
                  <div className="flex items-center gap-2">
                    <Input
                        value={usernameInput}
                        onChange={(e) => setUsernameInput(e.target.value)}
                        disabled={updateUsername.isPending}
                        autoFocus
                    />
                    <Button size="sm" onClick={handleSaveUsername} disabled={updateUsername.isPending}>
                      {updateUsername.isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                      Save
                    </Button>
                    <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => {
                          setUsernameInput(user?.username ?? "");
                          setIsEditing(false);
                        }}
                        disabled={updateUsername.isPending}
                    >
                      Cancel
                    </Button>
                  </div>
              ) : (
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">{user?.username}</span>
                    <Button size="sm" variant="outline" onClick={() => setIsEditing(true)}>
                      <Pencil className="h-3.5 w-3.5" />
                      Edit
                    </Button>
                  </div>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">Appearance</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center justify-between">
            <span className="flex items-center gap-2 text-sm">
              {theme === "dark" ? <Moon className="h-4 w-4" /> : <Sun className="h-4 w-4" />}
              Dark mode
            </span>
              <Switch checked={theme === "dark"} onCheckedChange={toggleTheme} />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">Session</CardTitle>
          </CardHeader>
          <CardContent>
            <Button variant="outline" onClick={logout}>
              <LogOut className="h-4 w-4" />
              Log out
            </Button>
          </CardContent>
        </Card>

        <Card className="border-destructive/40">
          <CardHeader>
            <CardTitle className="text-base text-destructive">Danger zone</CardTitle>
          </CardHeader>
          <CardContent>
            <Button
                variant="destructive"
                onClick={handleDeleteAccount}
                disabled={deleteAccount.isPending}
            >
              {deleteAccount.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                  <Trash2 className="h-4 w-4" />
              )}
              Delete account
            </Button>
            <p className="mt-2 text-xs text-muted-foreground">
              Permanently deletes your account and all associated data. This cannot be undone.
            </p>
          </CardContent>
        </Card>
      </div>
  );
}