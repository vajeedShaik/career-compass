import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { setAuthToken } from "@/api/client";
import type { AuthResponse, AuthUser } from "@/types/auth";

interface AuthContextValue {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isInitializing: boolean;
  setSession: (response: AuthResponse) => void;
  clearSession: () => void;
  updateUsername: (username: string) => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const TOKEN_KEY = "cc_token";
const USER_KEY = "cc_user";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem(TOKEN_KEY);
    const storedUser = localStorage.getItem(USER_KEY);

    if (storedToken && storedUser) {
      try {
        const parsedUser: AuthUser = JSON.parse(storedUser);
        setToken(storedToken);
        setUser(parsedUser);
        setAuthToken(storedToken);
      } catch {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
      }
    }
    setIsInitializing(false);
  }, []);

  function setSession(response: AuthResponse) {
    if (!response?.token) {
      console.error("setSession called with invalid response:", response);
      return;
    }

    const authUser: AuthUser = {
      userId: response.userId,
      email: response.email,
      username: response.username,
    };

    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(authUser));

    setAuthToken(response.token);
    setToken(response.token);
    setUser(authUser);
  }

  function updateUsername(username: string) {
    setUser((prev) => {
      if (!prev) return prev;
      const updated = { ...prev, username };
      localStorage.setItem(USER_KEY, JSON.stringify(updated));
      return updated;
    });
  }

  function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setAuthToken(null);
    setToken(null);
    setUser(null);
  }

  return (
      <AuthContext.Provider
          value={{
            user,
            token,
            isAuthenticated: !!token,
            isInitializing,
            setSession,
            clearSession,
            updateUsername,
          }}
      >
        {children}
      </AuthContext.Provider>
  );
}

export function useAuthContext(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuthContext must be used within an AuthProvider");
  return ctx;
}