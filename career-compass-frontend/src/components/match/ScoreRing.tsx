import { RadialBarChart, RadialBar, PolarAngleAxis } from "recharts";
import { useThemeContext } from "@/contexts/ThemeContext";
import { chartColors, chartColorsDark } from "@/theme/tokens";

interface ScoreRingProps {
  score: number;
}

export function ScoreRing({ score }: ScoreRingProps) {
  const { theme } = useThemeContext();
  const colors = theme === "dark" ? chartColorsDark : chartColors;

  const color = score >= 70 ? colors.success : score >= 50 ? colors.warning : colors.destructive;
  const data = [{ name: "score", value: score, fill: color }];

  return (
    <div className="relative flex h-40 w-40 items-center justify-center">
      <RadialBarChart
        width={160}
        height={160}
        cx="50%"
        cy="50%"
        innerRadius="75%"
        outerRadius="100%"
        barSize={12}
        data={data}
        startAngle={90}
        endAngle={-270}
      >
        <PolarAngleAxis type="number" domain={[0, 100]} angleAxisId={0} tick={false} />
        <RadialBar background dataKey="value" cornerRadius={8} />
      </RadialBarChart>
      <div className="absolute flex flex-col items-center">
        <span className="text-3xl font-bold">{Math.round(score)}%</span>
        <span className="text-xs text-muted-foreground">match</span>
      </div>
    </div>
  );
}
