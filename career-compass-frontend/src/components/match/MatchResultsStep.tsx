import { motion } from "framer-motion";
import { Loader2, Sparkles } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ScoreRing } from "@/components/match/ScoreRing";
import { SkillChips } from "@/components/match/SkillChips";
import { useGenerateRoadmap } from "@/hooks/useRoadmaps";
import type { MatchResponse } from "@/types/match";
import type { RoadmapResponse } from "@/types/roadmap";

interface MatchResultsStepProps {
  match: MatchResponse;
  onRoadmapReady: (roadmap: RoadmapResponse) => void;
}

export function MatchResultsStep({ match, onRoadmapReady }: MatchResultsStepProps) {
  const generateRoadmap = useGenerateRoadmap();

  function handleGenerate() {
    generateRoadmap.mutate(match.id, { onSuccess: onRoadmapReady });
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      className="space-y-6"
    >
      <Card>
        <CardContent className="flex flex-col items-center gap-4 p-6">
          <ScoreRing score={match.matchScore} />
          {match.scoringNotes && (
            <p className="text-center text-sm text-muted-foreground">{match.scoringNotes}</p>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardContent className="p-6">
          <SkillChips matchedSkills={match.matchedSkills} missingSkills={match.missingSkills} />
        </CardContent>
      </Card>

      <Button onClick={handleGenerate} disabled={generateRoadmap.isPending} className="w-full">
        {generateRoadmap.isPending ? (
          <Loader2 className="h-4 w-4 animate-spin" />
        ) : (
          <Sparkles className="h-4 w-4" />
        )}
        Generate my roadmap
      </Button>
    </motion.div>
  );
}
