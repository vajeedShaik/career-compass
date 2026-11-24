/**
 * Central color/design token reference for the app. The actual CSS custom
 * properties live in src/index.css (both :root and .dark blocks) — this
 * file exists so components can reference token names in TypeScript
 * (e.g. for Recharts colors, which need raw values, not Tailwind classes)
 * without duplicating the color values by hand.
 */
export const chartColors = {
  primary: "hsl(243, 75%, 59%)",
  success: "hsl(142, 71%, 45%)",
  warning: "hsl(38, 92%, 50%)",
  destructive: "hsl(0, 72%, 51%)",
  muted: "hsl(215, 16%, 47%)",
} as const;

export const chartColorsDark = {
  primary: "hsl(243, 82%, 68%)",
  success: "hsl(142, 65%, 45%)",
  warning: "hsl(38, 85%, 55%)",
  destructive: "hsl(0, 63%, 45%)",
  muted: "hsl(215, 16%, 65%)",
} as const;
