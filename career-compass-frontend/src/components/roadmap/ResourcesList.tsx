import { ExternalLink } from "lucide-react";
import type { ResourceUsed } from "@/types/roadmap";

interface ResourcesListProps {
  resources: ResourceUsed[];
}

export function ResourcesList({ resources }: ResourcesListProps) {
  if (resources.length === 0) return null;

  return (
    <div className="space-y-2">
      <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
        Resources referenced
      </p>
      <div className="space-y-1.5">
        {resources.map((r, i) => (
          <a
            key={`${r.url}-${i}`}
            href={r.url}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 rounded-md border border-border px-3 py-2 text-sm transition-colors hover:bg-accent"
          >
            <ExternalLink className="h-3.5 w-3.5 shrink-0 text-muted-foreground" />
            <span className="min-w-0 flex-1 truncate">{r.title}</span>
            <span className="shrink-0 rounded-full bg-muted px-2 py-0.5 text-[10px] text-muted-foreground">
              {r.skill}
            </span>
          </a>
        ))}
      </div>
    </div>
  );
}
