import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { Check, FileText, Briefcase, TrendingUp, Sparkles, Loader2 } from "lucide-react";
import { ResumeUploadStep } from "@/components/upload/ResumeUploadStep";
import { JobInputStep } from "@/components/job/JobInputStep";
import { MatchResultsStep } from "@/components/match/MatchResultsStep";
import { RoadmapView } from "@/components/roadmap/RoadmapView";
import { ResourcesList } from "@/components/roadmap/ResourcesList";
import { ChatPanel } from "@/components/chat/ChatPanel";
import { Card, CardContent } from "@/components/ui/card";
import { useComputeMatch } from "@/hooks/useMatches";
import { cn } from "@/utils/cn";
import type { ResumeResponse } from "@/types/resume";
import type { JobDescriptionResponse } from "@/types/job";
import type { MatchResponse } from "@/types/match";
import type { RoadmapResponse } from "@/types/roadmap";

const STAGES = [
  { key: "resume", label: "Resume", icon: FileText },
  { key: "job", label: "Job", icon: Briefcase },
  { key: "match", label: "Match", icon: TrendingUp },
  { key: "roadmap", label: "Roadmap", icon: Sparkles },
] as const;

export default function Workspace() {
  const [resume, setResume] = useState<ResumeResponse | null>(null);
  const [job, setJob] = useState<JobDescriptionResponse | null>(null);
  const [match, setMatch] = useState<MatchResponse | null>(null);
  const [roadmap, setRoadmap] = useState<RoadmapResponse | null>(null);

  const computeMatch = useComputeMatch();

  // Once both resume and job exist, automatically compute the match —
  // no extra manual click needed for this transition, only for the
  // roadmap/email step, which is a more deliberate action.
  useEffect(() => {
    if (resume && job && !match && !computeMatch.isPending) {
      computeMatch.mutate(
        { resumeId: resume.id, jdId: job.id },
        { onSuccess: setMatch }
      );
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [resume, job]);

  const currentStageIndex = roadmap ? 3 : match ? 3 : job ? 2 : resume ? 1 : 0;

  return (
    <div className="mx-auto max-w-3xl space-y-8">
      {/* Stepper */}
      <div className="flex items-center justify-between">
        {STAGES.map((stage, i) => {
          const Icon = stage.icon;
          const isComplete = i < currentStageIndex || (i === 3 && !!roadmap);
          const isActive = i === currentStageIndex;
          return (
            <div key={stage.key} className="flex flex-1 items-center">
              <div className="flex flex-col items-center gap-1">
                <div
                  className={cn(
                    "flex h-9 w-9 items-center justify-center rounded-full border-2 transition-colors",
                    isComplete
                      ? "border-success bg-success text-success-foreground"
                      : isActive
                      ? "border-primary text-primary"
                      : "border-border text-muted-foreground"
                  )}
                >
                  {isComplete ? <Check className="h-4 w-4" /> : <Icon className="h-4 w-4" />}
                </div>
                <span className="text-xs text-muted-foreground">{stage.label}</span>
              </div>
              {i < STAGES.length - 1 && (
                <div
                  className={cn(
                    "mx-2 h-0.5 flex-1",
                    i < currentStageIndex ? "bg-success" : "bg-border"
                  )}
                />
              )}
            </div>
          );
        })}
      </div>

      {/* Step content */}
      {!resume && <ResumeUploadStep onComplete={setResume} />}

      {resume && !job && <JobInputStep onComplete={setJob} />}

      {resume && job && !match && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="flex flex-col items-center gap-3 py-10"
        >
          <Loader2 className="h-6 w-6 animate-spin text-primary" />
          <p className="text-sm text-muted-foreground">
            Computing your match score with AI...
          </p>
        </motion.div>
      )}

      {match && !roadmap && (
        <MatchResultsStep match={match} onRoadmapReady={setRoadmap} />
      )}

      {roadmap && (
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          className="space-y-6"
        >
          {roadmap.emailSent && (
            <div className="rounded-md bg-success/10 px-4 py-2 text-sm text-success">
              Your full report has been emailed to you.
            </div>
          )}

          <Card>
            <CardContent className="p-6">
              <RoadmapView content={roadmap.roadmapContent} />
              {roadmap.resourcesUsed.length > 0 && (
                <div className="mt-6">
                  <ResourcesList resources={roadmap.resourcesUsed} />
                </div>
              )}
            </CardContent>
          </Card>

          <ChatPanel roadmapId={roadmap.id} />
        </motion.div>
      )}
    </div>
  );
}
