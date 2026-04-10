import { useEffect, useState } from "react";
import { Card, CardContent } from "@/components/ui/card";

type FetchState =
    | { status: "loading" }
    | { status: "success"; message: string }
    | { status: "error"; error: string };

export default function App() {
  const [state, setState] = useState<FetchState>({ status: "loading" });

  useEffect(() => {
    fetch("/api/hello")
        .then((res) => {
          if (!res.ok) throw new Error(`HTTP ${res.status}`);
          return res.text();
        })
        .then((message) => setState({ status: "success", message }))
        .catch((err: unknown) =>
            setState({
              status: "error",
              error: err instanceof Error ? err.message : "Unknown error",
            })
        );
  }, []);

  return (
      <main className="min-h-screen flex items-center justify-center bg-white">
        <Card className="w-full max-w-md">
          <CardContent>
            {state.status === "loading" && (
                <p className="text-muted-foreground text-sm">Loading...</p>
            )}
            {state.status === "success" && (
                <p className="text-foreground text-lg font-medium">{state.message}</p>
            )}
            {state.status === "error" && (
                <p className="text-destructive text-sm">Error: {state.error}</p>
            )}
          </CardContent>
        </Card>
      </main>
  );
}