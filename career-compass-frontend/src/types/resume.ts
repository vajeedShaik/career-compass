export interface ProjectData {
  name: string;
  description: string;
  technologies: string[];
}

export interface ResumeResponse {
  id: string;
  originalFilename: string;
  candidateName: string | null;
  extractedSkills: string[];
  softSkills: string[];
  certifications: string[];
  educationLevel: string | null;
  yearsOfExperience: number | null;
  projects: ProjectData[];
  createdAt: string;
}
