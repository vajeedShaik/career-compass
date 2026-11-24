export interface ResumeSummary {
    id: string;
    originalFilename: string;
    createdAt: string;
}

export interface JobSummary {
    id: string;
    title: string | null;
    companyName: string | null;
    createdAt: string;
}

export interface MatchSummary {
    id: string;
    matchScore: number;
    createdAt: string;
}

export interface HistoryResponse {
    resumes: ResumeSummary[];
    jobDescriptions: JobSummary[];
    matches: MatchSummary[];
}