import { motion } from "framer-motion";
import { Loader2, ArrowRight, GraduationCap, Briefcase } from "lucide-react";
import { Dropzone } from "@/components/upload/Dropzone";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useUploadResume } from "@/hooks/useResumes";
import type { ResumeResponse } from "@/types/resume";

interface ResumeUploadStepProps {
  onComplete: (resume: ResumeResponse) => void;
}

export function ResumeUploadStep({ onComplete }: ResumeUploadStepProps) {
  const upload = useUploadResume();

  function handleFileSelected(file: File) {
    upload.mutate(file, {
      onSuccess: (data) => {
        onComplete(data);
      },
    });
  }

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-lg font-semibold">Upload your resume</h2>
        <p className="text-sm text-muted-foreground">
          We'll extract your skills, education, experience, and projects automatically.
        </p>
      </div>

      <Dropzone onFileSelected={handleFileSelected} disabled={upload.isPending} />

      {upload.isPending && (
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <Loader2 className="h-4 w-4 animate-spin" />
          Analyzing your resume with AI — this can take a moment...
        </div>
      )}

      {upload.data && (
        <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3 }}
        >
          <Card>
            <CardContent className="space-y-4 p-5">
              <div className="flex flex-wrap gap-2">
                {upload.data.extractedSkills.map((skill) => (
                  <span
                    key={skill}
                    className="rounded-full bg-accent px-3 py-1 text-xs font-medium text-accent-foreground"
                  >
                    {skill}
                  </span>
                ))}
              </div>

              <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
                {upload.data.educationLevel && (
                  <span className="flex items-center gap-1.5">
                    <GraduationCap className="h-4 w-4" />
                    {upload.data.educationLevel}
                  </span>
                )}
                {upload.data.yearsOfExperience !== null && (
                  <span className="flex items-center gap-1.5">
                    <Briefcase className="h-4 w-4" />
                    {upload.data.yearsOfExperience} years experience
                  </span>
                )}
              </div>

              <Button onClick={() => onComplete(upload.data)} className="w-full">
                Continue
                <ArrowRight className="h-4 w-4" />
              </Button>
            </CardContent>
          </Card>
        </motion.div>
      )}
    </div>
  );
}
