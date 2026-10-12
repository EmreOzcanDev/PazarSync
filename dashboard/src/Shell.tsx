import { NavLink } from "react-router-dom";
import type { ReactNode } from "react";
import { useAuth } from "./auth";

const links = [
  { to: "/", label: "Overview" },
  { to: "/stock", label: "Low stock" },
  { to: "/failures", label: "Failed syncs" },
  { to: "/questions", label: "Questions" },
];

export function Shell({ children }: { children: ReactNode }) {
  const { session, logout } = useAuth();
  return (
    <div className="shell">
      <aside>
        <p className="mark">PazarSync</p>
        <p className="aside-note">Seller desk</p>
        <nav>
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} end={link.to === "/"}>
              {link.label}
            </NavLink>
          ))}
        </nav>
        <div className="who">
          <strong>{session?.username}</strong>
          <span>{session?.role}</span>
          <button type="button" onClick={logout}>
            Sign out
          </button>
        </div>
      </aside>
      <main>{children}</main>
    </div>
  );
}
