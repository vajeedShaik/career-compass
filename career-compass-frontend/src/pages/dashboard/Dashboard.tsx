import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import { FileText, BarChart3, TrendingUp, Upload, Sparkles, Loader2 } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/shared/EmptyState";
import { useAuthContext } from "@/contexts/AuthContext";
import { useDashboard } from "@/hooks/useDashboard";

export default function Dashboard() {
  const { user } = useAuthContext();
  const displayName = user?.username ?? "there";
  const { data, isLoading, isError } = useDashboard();

  const statCards = [
    { label: "Resumes uploaded", icon: FileText, value: data?.resumesUploaded },
    { label: "Reports generated", icon: BarChart3, value: data?.reportsGenerated },
    {
      label: "Average match score",
      icon: TrendingUp,
      value: data ? `${Math.round(data.averageMatchScore)}%` : undefined,
    },
  ];

  return (
      <motion.div
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3 }}
          className="mx-auto max-w-5xl space-y-8"
      >
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Hello, {displayName}</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Welcome to your Career Compass dashboard.
          </p>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          {statCards.map((stat) => {
            const Icon = stat.icon;
            return (
                <Card key={stat.label}>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium text-muted-foreground">
                      {stat.label}
                    </CardTitle>
                    <Icon className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    {isLoading ? (
                        <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
                    ) : isError ? (
                        <p className="text-sm text-destructive">Failed to load</p>
                    ) : (
                        <>
                          <p className="text-2xl font-semibold">{stat.value ?? "0"}</p>
                          {(stat.value === 0 || stat.value === "0%") && (
                              <p className="mt-1 text-xs text-muted-foreground">No data yet</p>
                          )}
                        </>
                    )}
                  </CardContent>
                </Card>
            );
          })}
        </div>

        <div>
          <h2 className="mb-3 text-sm font-semibold text-foreground">Quick actions</h2>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Card className="transition-shadow hover:shadow-md">
              <CardContent className="flex items-center gap-4 p-5">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                  <Upload className="h-5 w-5 text-primary" />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium">Upload a resume</p>
                  <p className="text-xs text-muted-foreground">Start a new analysis</p>
                </div>
                <Button size="sm" variant="outline" asChild>
                  <Link to="/workspace">Start</Link>
                </Button>
              </CardContent>
            </Card>

            <Card className="transition-shadow hover:shadow-md">
              <CardContent className="flex items-center gap-4 p-5">
                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                  <Sparkles className="h-5 w-5 text-primary" />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium">Generate a match report</p>
                  <p className="text-xs text-muted-foreground">Score your resume against a JD</p>
                </div>
                <Button size="sm" variant="outline" asChild>
                  <Link to="/workspace">Start</Link>
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>

        <div>
          <h2 className="mb-3 text-sm font-semibold text-foreground">Recent activity</h2>
          {isLoading ? (
              <div className="flex justify-center py-10">
                <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
              </div>
          ) : data && data.recentActivity.length > 0 ? (
              <div className="space-y-2">
                {data.recentActivity.map((item, i) => (
                    <div
                        key={i}
                        className="flex items-center justify-between rounded-md border border-border px-4 py-2.5 text-sm"
                    >
                      <span>{item.message}</span>
                      <span className="text-xs text-muted-foreground">
                  {new Date(item.createdAt).toLocaleDateString()}
                </span>
                    </div>
                ))}
              </div>
          ) : (
              <EmptyState
                  icon={FileText}
                  title="No activity yet"
                  description="Once you upload a resume and run your first match, your recent activity will show up here."
              />
          )}
        </div>
      </motion.div>
  );
}