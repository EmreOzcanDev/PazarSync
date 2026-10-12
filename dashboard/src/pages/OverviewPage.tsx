import { useQuery } from "@tanstack/react-query";
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { api, money, type DailySales, type Failure, type LowStock } from "../api";
import { useAuth } from "../auth";

export function OverviewPage() {
  const { session } = useAuth();
  const token = session?.token ?? null;
  const sales = useQuery({
    queryKey: ["sales"],
    queryFn: () => api<DailySales[]>("/api/sales", token),
  });
  const stock = useQuery({
    queryKey: ["low-stock"],
    queryFn: () => api<LowStock[]>("/api/stock/low", token),
  });
  const failures = useQuery({
    queryKey: ["failures"],
    queryFn: () => api<Failure[]>("/api/sync-failures", token),
  });

  const rows = sales.data ?? [];
  const total = rows.reduce((sum, row) => sum + row.totalMinor, 0);
  const orders = rows.reduce((sum, row) => sum + row.orderCount, 0);

  return (
    <section>
      <header className="page-head">
        <h1>Overview</h1>
        <p>Sales land here after the importer publishes them and the order service stores them once.</p>
      </header>
      <div className="kpis">
        <article>
          <span>Sales in range</span>
          <strong>{money(total)}</strong>
        </article>
        <article>
          <span>Orders</span>
          <strong>{orders}</strong>
        </article>
        <article>
          <span>Low stock</span>
          <strong>{stock.data?.length ?? "–"}</strong>
        </article>
        <article>
          <span>Open failures</span>
          <strong>{failures.data?.length ?? "–"}</strong>
        </article>
      </div>
      <div className="panel chart">
        {sales.isError ? <p className="error">Could not load sales.</p> : null}
        <ResponsiveContainer width="100%" height={280}>
          <LineChart data={rows}>
            <CartesianGrid stroke="#e7dfd2" vertical={false} />
            <XAxis dataKey="day" tick={{ fill: "#5c564c", fontSize: 12 }} />
            <YAxis tick={{ fill: "#5c564c", fontSize: 12 }} />
            <Tooltip formatter={(value) => money(Number(value))} />
            <Line type="monotone" dataKey="totalMinor" stroke="#9a3412" strokeWidth={2.4} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </section>
  );
}
