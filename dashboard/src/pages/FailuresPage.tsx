import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, when, type Failure } from "../api";
import { useAuth } from "../auth";

export function FailuresPage() {
  const { session } = useAuth();
  const token = session?.token ?? null;
  const client = useQueryClient();
  const failures = useQuery({
    queryKey: ["failures"],
    queryFn: () => api<Failure[]>("/api/sync-failures", token),
  });
  const retry = useMutation({
    mutationFn: (id: string) => api<void>(`/api/sync-failures/${id}/retry`, token, { method: "POST" }),
    onSuccess: () => client.invalidateQueries({ queryKey: ["failures"] }),
  });
  const canRetry = session?.role === "ADMIN";

  return (
    <section>
      <header className="page-head">
        <h1>Failed syncs</h1>
        <p>Poison messages and calls that still failed after retries. An admin can republish one.</p>
      </header>
      {failures.isError ? <p className="error">Could not load failures.</p> : null}
      {retry.isError ? <p className="error">Retry was rejected. Admins can retry a failure.</p> : null}
      {failures.data?.length === 0 ? <p className="empty">No open failures.</p> : null}
      <ul className="list">
        {failures.data?.map((failure) => (
          <li key={failure.id} className="panel">
            <div>
              <strong>{failure.operation}</strong>
              <span>{failure.externalId ?? failure.retryTopic}</span>
            </div>
            <p>{failure.errorMessage}</p>
            <footer>
              <time>{when(failure.createdAt)}</time>
              <button type="button" disabled={!canRetry || retry.isPending} onClick={() => retry.mutate(failure.id)}>
                Retry
              </button>
            </footer>
          </li>
        ))}
      </ul>
    </section>
  );
}
