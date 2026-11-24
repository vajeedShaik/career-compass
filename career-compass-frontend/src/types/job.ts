export interface JobDescriptionResponse {
  id: string;
  title: string | null;
  companyName: string | null;
  requiredSkills: string[];
  minEducationLevel: string | null;
  minYearsExperience: number | null;
  createdAt: string;
}
