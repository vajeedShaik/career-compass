import { Check, X } from "lucide-react";

interface SkillChipsProps {
  matchedSkills: string[];
  missingSkills: string[];
}

export function SkillChips({ matchedSkills, missingSkills }: SkillChipsProps) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <div>
        <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
          Matched skills
        </p>
        <div className="flex flex-wrap gap-2">
          {matchedSkills.length === 0 && (
            <p className="text-xs text-muted-foreground">None matched</p>
          )}
          {matchedSkills.map((skill) => (
            <span
              key={skill}
              className="flex items-center gap-1 rounded-full bg-success/10 px-3 py-1 text-xs font-medium text-success"
            >
              <Check className="h-3 w-3" />
              {skill}
            </span>
          ))}
        </div>
      </div>
      <div>
        <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
          Missing skills
        </p>
        <div className="flex flex-wrap gap-2">
          {missingSkills.length === 0 && (
            <p className="text-xs text-muted-foreground">No gaps found</p>
          )}
          {missingSkills.map((skill) => (
            <span
              key={skill}
              className="flex items-center gap-1 rounded-full bg-destructive/10 px-3 py-1 text-xs font-medium text-destructive"
            >
              <X className="h-3 w-3" />
              {skill}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}
