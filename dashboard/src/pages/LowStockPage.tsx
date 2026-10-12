import { useQuery } from "@tanstack/react-query";
import { api, money, type LowStock } from "../api";
import { useAuth } from "../auth";

export function LowStockPage() {
  const { session } = useAuth();
  const stock = useQuery({
    queryKey: ["low-stock"],
    queryFn: () => api<LowStock[]>("/api/stock/low", session?.token ?? null),
  });

  return (
    <section>
      <header className="page-head">
        <h1>Low stock</h1>
        <p>Products at or under their threshold. A stock change is also pushed back to the marketplace.</p>
      </header>
      {stock.isError ? <p className="error">Could not load stock.</p> : null}
      {stock.data?.length === 0 ? <p className="empty">Nothing is under threshold.</p> : null}
      <div className="cards">
        {stock.data?.map((item) => (
          <article key={item.barcode} className="panel">
            <h2>{item.name}</h2>
            <p className="barcode">{item.barcode}</p>
            <p className="qty">
              {item.quantity} <span>left, threshold {item.threshold}</span>
            </p>
            <p>{money(item.salePriceMinor)}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
