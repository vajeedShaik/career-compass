package com.careercompass.util;

/**
 * Matches skill names that mean the same thing but are phrased slightly
 * differently — e.g. "Hibernate/JPA" vs "Hibernate / JPA" vs "Hibernate and
 * JPA". Exact-string matching breaks on any of these; normalizing to just
 * lowercase alphanumerics and checking containment either direction is
 * resilient to spacing, punctuation, and word-order differences without
 * needing a full fuzzy-matching library for what's still a fairly small
 * skill vocabulary.
 */
public final class SkillNameMatcher {

    private SkillNameMatcher() {}

    public static String normalize(String skill) {
        if (skill == null) return "";
        return skill.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    public static boolean matches(String skillA, String skillB) {
        String a = normalize(skillA);
        String b = normalize(skillB);
        if (a.isEmpty() || b.isEmpty()) return false;
        return a.contains(b) || b.contains(a);
    }
}
