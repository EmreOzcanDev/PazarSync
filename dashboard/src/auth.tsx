import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import { api, type Session } from "./api";

type AuthValue = {
  session: Session | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthValue | null>(null);
const storageKey = "pazarsync.session";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => {
    const raw = localStorage.getItem(storageKey);
    return raw ? (JSON.parse(raw) as Session) : null;
  });

  const value = useMemo<AuthValue>(
    () => ({
      session,
      async login(username, password) {
        const next = await api<Session>("/api/auth/login", null, {
          method: "POST",
          body: JSON.stringify({ username, password }),
        });
        localStorage.setItem(storageKey, JSON.stringify(next));
        setSession(next);
      },
      logout() {
        localStorage.removeItem(storageKey);
        setSession(null);
      },
    }),
    [session],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// The hook lives next to the provider so session state stays in one place.
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthValue {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error("AuthProvider is missing");
  }
  return value;
}
