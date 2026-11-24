import { motion } from "framer-motion";
import { cn } from "@/utils/cn";

interface PasswordStrengthMeterProps {
  password: string;
}

function calculateStrength(password: string): number {
  if (!password) return 0;
  let score = 0;
  if (password.length >= 8) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/[a-z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;
  return score;
}

const LEVELS = [
  { label: "Very weak", color: "bg-destructive" },
  { label: "Weak", color: "bg-destructive" },
  { label: "Fair", color: "bg-warning" },
  { label: "Good", color: "bg-warning" },
  { label: "Strong", color: "bg-success" },
];

export function PasswordStrengthMeter({ password }: PasswordStrengthMeterProps) {
  const score = calculateStrength(password);
  const level = LEVELS[Math.max(score - 1, 0)];
  const percent = (score / 5) * 100;

  if (!password) return null;

  return (
    <div className="mt-2 space-y-1.5">
      <div className="h-1.5 w-full overflow-hidden rounded-full bg-muted">
        <motion.div
          className={cn("h-full rounded-full", level.color)}
          initial={{ width: 0 }}
          animate={{ width: `${percent}%` }}
          transition={{ duration: 0.3, ease: "easeOut" }}
        />
      </div>
      <p className="text-xs text-muted-foreground">{level.label}</p>
    </div>
  );
}
