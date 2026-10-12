import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { api, when, type Question } from "../api";
import { useAuth } from "../auth";

export function QuestionsPage() {
  const { session } = useAuth();
  const token = session?.token ?? null;
  const client = useQueryClient();
  const questions = useQuery({
    queryKey: ["questions"],
    queryFn: () => api<Question[]>("/api/questions", token),
  });
  const canEdit = session?.role === "ADMIN" || session?.role === "OPERATOR";

  return (
    <section>
      <header className="page-head">
        <h1>Questions</h1>
        <p>Each new question gets a draft from past answers. Approve it to post the reply.</p>
      </header>
      {questions.isError ? <p className="error">Could not load questions.</p> : null}
      {questions.data?.length === 0 ? <p className="empty">No questions yet.</p> : null}
      <div className="stack">
        {questions.data?.map((question) => (
          <QuestionCard
            key={question.id}
            question={question}
            canEdit={canEdit}
            onSave={async (text, approve) => {
              const path = approve ? `/api/questions/${question.id}/approve` : `/api/questions/${question.id}/draft`;
              const method = approve ? "POST" : "PUT";
              await api(path, token, { method, body: JSON.stringify({ text }) });
              await client.invalidateQueries({ queryKey: ["questions"] });
            }}
          />
        ))}
      </div>
    </section>
  );
}

function QuestionCard({
  question,
  canEdit,
  onSave,
}: {
  question: Question;
  canEdit: boolean;
  onSave: (text: string, approve: boolean) => Promise<void>;
}) {
  const [text, setText] = useState(question.draftText ?? "");
  const [error, setError] = useState<string | null>(null);
  const save = useMutation({
    mutationFn: (approve: boolean) => onSave(text, approve),
    onError: () => setError("The desk rejected that change."),
  });
  const approved = question.status === "APPROVED";

  return (
    <article className="panel question">
      <header>
        <div>
          <strong>{question.customerName}</strong>
          <span>{question.productBarcode}</span>
        </div>
        <em>{question.status}</em>
      </header>
      <p>{question.text}</p>
      <textarea value={text} onChange={(event) => setText(event.target.value)} disabled={!canEdit || approved} rows={4} />
      {error ? <p className="error">{error}</p> : null}
      <footer>
        <time>{when(question.askedAt)}</time>
        {canEdit && !approved ? (
          <div className="actions">
            <button type="button" onClick={() => save.mutate(false)} disabled={save.isPending}>
              Save draft
            </button>
            <button type="button" className="primary" onClick={() => save.mutate(true)} disabled={save.isPending}>
              Approve
            </button>
          </div>
        ) : null}
      </footer>
    </article>
  );
}
