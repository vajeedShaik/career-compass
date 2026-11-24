import { useState } from "react";
import { Loader2, ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Dropzone } from "@/components/upload/Dropzone";
import { useSubmitJob } from "@/hooks/useJobs";
import type { JobDescriptionResponse } from "@/types/job";
import { cn } from "@/utils/cn";

interface JobInputStepProps {
  onComplete: (job: JobDescriptionResponse) => void;
}

export function JobInputStep({ onComplete }: JobInputStepProps) {
  const [mode, setMode] = useState<"text" | "file">("text");
  const [title, setTitle] = useState("");
  const [rawText, setRawText] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const submitJob = useSubmitJob();

  function handleSubmit() {
    submitJob.mutate(
      { title: title || undefined, rawText: mode === "text" ? rawText : undefined, file: mode === "file" ? file ?? undefined : undefined },
      { onSuccess: onComplete }
    );
  }

  const canSubmit = mode === "text" ? rawText.trim().length > 0 : !!file;

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-lg font-semibold">Add the job description</h2>
        <p className="text-sm text-muted-foreground">
          Paste the JD text, or upload it as a PDF.
        </p>
      </div>

      <div className="space-y-2">
        <Label htmlFor="job-title">Role title (optional)</Label>
        <Input
          id="job-title"
          placeholder="e.g. Java Backend Developer"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
      </div>

      <div className="inline-flex rounded-md border border-border p-1">
        <button
          type="button"
          onClick={() => setMode("text")}
          className={cn(
            "rounded px-3 py-1.5 text-sm font-medium transition-colors",
            mode === "text" ? "bg-primary text-primary-foreground" : "text-muted-foreground"
          )}
        >
          Paste text
        </button>
        <button
          type="button"
          onClick={() => setMode("file")}
          className={cn(
            "rounded px-3 py-1.5 text-sm font-medium transition-colors",
            mode === "file" ? "bg-primary text-primary-foreground" : "text-muted-foreground"
          )}
        >
          Upload PDF
        </button>
      </div>

      {mode === "text" ? (
        <textarea
          value={rawText}
          onChange={(e) => setRawText(e.target.value)}
          placeholder="Paste the full job description here..."
          rows={8}
          className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
        />
      ) : (
        <Dropzone onFileSelected={setFile} disabled={submitJob.isPending} />
      )}

      <Button
        onClick={handleSubmit}
        disabled={!canSubmit || submitJob.isPending}
        className="w-full"
      >
        {submitJob.isPending ? (
          <Loader2 className="h-4 w-4 animate-spin" />
        ) : (
          <>
            Continue
            <ArrowRight className="h-4 w-4" />
          </>
        )}
      </Button>
    </div>
  );
}
