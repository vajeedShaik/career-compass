export interface MatchResponse {
  id: string;
  resumeId: string;
  jdId: string;
  matchScore: number;
  matchedSkills: string[];
  missingSkills: string[];
  scoringNotes: string;
  createdAt: string;
}
