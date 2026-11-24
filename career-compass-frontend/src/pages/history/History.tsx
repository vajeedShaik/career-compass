import { motion } from "framer-motion";
import { Loader2, FileText, Briefcase, TrendingUp, Archive } from "lucide-react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { EmptyState } from "@/components/shared/EmptyState";
import { useHistory } from "@/hooks/useHistory";

function formatDate(iso: string) {
    return new Date(iso).toLocaleDateString(undefined, {
        year: "numeric",
        month: "short",
        day: "numeric",
    });
}

export default function History() {
    const { data, isLoading, isError } = useHistory();

    if (isLoading) {
        return (
            <div className="flex justify-center py-20">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
        );
    }

    if (isError || !data) {
        return (
            <EmptyState
                icon={Archive}
                title="Couldn't load history"
                description="Something went wrong fetching your history. Try refreshing the page."
                className="py-16"
            />
        );
    }

    const isEmpty =
        data.resumes.length === 0 && data.jobDescriptions.length === 0 && data.matches.length === 0;

    return (
        <motion.div
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3 }}
            className="mx-auto max-w-3xl space-y-6"
        >
            <div>
                <h1 className="text-2xl font-semibold tracking-tight">History</h1>
                <p className="mt-1 text-sm text-muted-foreground">
                    Your past resumes, job descriptions, and match reports.
                </p>
            </div>

            {isEmpty ? (
                <EmptyState
                    icon={Archive}
                    title="No history yet"
                    description="Once you run your first analysis in the Workspace, it will show up here."
                    className="py-16"
                />
            ) : (
                <Tabs defaultValue="resumes">
                    <TabsList>
                        <TabsTrigger value="resumes">Resumes ({data.resumes.length})</TabsTrigger>
                        <TabsTrigger value="jobs">Job Descriptions ({data.jobDescriptions.length})</TabsTrigger>
                        <TabsTrigger value="matches">Matches ({data.matches.length})</TabsTrigger>
                    </TabsList>

                    <TabsContent value="resumes" className="space-y-2 pt-4">
                        {data.resumes.length === 0 && (
                            <p className="text-sm text-muted-foreground">No resumes yet.</p>
                        )}
                        {data.resumes.map((r) => (
                            <div
                                key={r.id}
                                className="flex items-center justify-between rounded-md border border-border px-4 py-2.5 text-sm"
                            >
                <span className="flex items-center gap-2">
                  <FileText className="h-4 w-4 text-muted-foreground" />
                    {r.originalFilename}
                </span>
                                <span className="text-xs text-muted-foreground">{formatDate(r.createdAt)}</span>
                            </div>
                        ))}
                    </TabsContent>

                    <TabsContent value="jobs" className="space-y-2 pt-4">
                        {data.jobDescriptions.length === 0 && (
                            <p className="text-sm text-muted-foreground">No job descriptions yet.</p>
                        )}
                        {data.jobDescriptions.map((j) => (
                            <div
                                key={j.id}
                                className="flex items-center justify-between rounded-md border border-border px-4 py-2.5 text-sm"
                            >
                <span className="flex items-center gap-2">
                  <Briefcase className="h-4 w-4 text-muted-foreground" />
                    {j.title ?? "Untitled role"} {j.companyName && `· ${j.companyName}`}
                </span>
                                <span className="text-xs text-muted-foreground">{formatDate(j.createdAt)}</span>
                            </div>
                        ))}
                    </TabsContent>

                    <TabsContent value="matches" className="space-y-2 pt-4">
                        {data.matches.length === 0 && (
                            <p className="text-sm text-muted-foreground">No matches yet.</p>
                        )}
                        {data.matches.map((m) => (
                            <div
                                key={m.id}
                                className="flex items-center justify-between rounded-md border border-border px-4 py-2.5 text-sm"
                            >
                <span className="flex items-center gap-2">
                  <TrendingUp className="h-4 w-4 text-muted-foreground" />
                    {Math.round(m.matchScore)}% match
                </span>
                                <span className="text-xs text-muted-foreground">{formatDate(m.createdAt)}</span>
                            </div>
                        ))}
                    </TabsContent>
                </Tabs>
            )}
        </motion.div>
    );
}