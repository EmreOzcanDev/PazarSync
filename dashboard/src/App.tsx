import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./auth";
import { Shell } from "./Shell";
import { FailuresPage } from "./pages/FailuresPage";
import { LoginPage } from "./pages/LoginPage";
import { LowStockPage } from "./pages/LowStockPage";
import { OverviewPage } from "./pages/OverviewPage";
import { QuestionsPage } from "./pages/QuestionsPage";

export function App() {
  const { session } = useAuth();
  if (!session) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }
  return (
    <Shell>
      <Routes>
        <Route path="/" element={<OverviewPage />} />
        <Route path="/stock" element={<LowStockPage />} />
        <Route path="/failures" element={<FailuresPage />} />
        <Route path="/questions" element={<QuestionsPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Shell>
  );
}
