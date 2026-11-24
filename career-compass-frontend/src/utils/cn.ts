import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Merges Tailwind class lists safely, resolving conflicting utility classes
 * (e.g. "p-2" vs "p-4") in favor of the later one. Required by every
 * shadcn/ui component generated in later phases.
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
