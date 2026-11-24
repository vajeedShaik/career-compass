package com.careercompass.util;

/**
 * Ranks education levels for comparison. Deliberately simple keyword
 * matching, not a formal taxonomy — good enough to catch clear mismatches
 * (e.g. Diploma vs a JD requiring a Master's) without over-engineering
 * something that's inherently fuzzy across regions and institutions.
 *
 * Unknown/unparseable levels default to a neutral rank (assume Bachelor's)
 * rather than penalizing — we'd rather under-penalize an ambiguous case
 * than unfairly reject a candidate based on a parsing gap.
 */
public final class EducationLevelUtil {

    private EducationLevelUtil() {}

    public static int rank(String level) {
        if (level == null || level.isBlank()) {
            return 2; // neutral default
        }
        String lower = level.toLowerCase();

        if (lower.contains("phd") || lower.contains("doctorate")) {
            return 4;
        }
        if (lower.contains("m.tech") || lower.contains("mtech") || lower.contains("m.sc")
                || lower.contains("msc") || lower.contains("mba") || lower.contains("master")) {
            return 3;
        }
        if (lower.contains("b.tech") || lower.contains("btech") || lower.contains("b.sc")
                || lower.contains("bsc") || lower.contains("bachelor") || lower.contains("b.e")) {
            return 2;
        }
        if (lower.contains("diploma") || lower.contains("associate")) {
            return 1;
        }
        return 2; // unrecognized text — neutral default
    }
}
