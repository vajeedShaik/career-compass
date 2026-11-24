import { Link } from "react-router-dom";
import { Compass } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-background px-4 text-center">
      <Compass className="h-10 w-10 text-muted-foreground" />
      <h1 className="text-3xl font-bold">404</h1>
      <p className="text-sm text-muted-foreground">This page doesn't exist.</p>
      <Button asChild>
        <Link to="/">Back to Dashboard</Link>
      </Button>
    </div>
  );
}
