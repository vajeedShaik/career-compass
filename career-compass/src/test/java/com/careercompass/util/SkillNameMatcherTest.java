package com.careercompass.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkillNameMatcherTest {

    @Test
    void matchesIdenticalSkillNames() {
        assertThat(SkillNameMatcher.matches("Docker", "Docker")).isTrue();
    }

    @Test
    void matchesDespiteSpacingAndPunctuationDifferences() {
        // The real bug found during manual testing: "Hibernate/JPA" vs "Hibernate / JPA"
        // failed under exact-string matching before this normalized approach was added.
        assertThat(SkillNameMatcher.matches("Hibernate/JPA", "Hibernate / JPA")).isTrue();
        assertThat(SkillNameMatcher.matches("Hibernate/JPA", "hibernate/jpa")).isTrue();
    }

    @Test
    void matchesIsCaseInsensitive() {
        assertThat(SkillNameMatcher.matches("REACT", "react")).isTrue();
    }

    @Test
    void documentsKnownFalsePositiveFromSubstringContainment() {
        // Known, accepted limitation flagged during manual testing: "SQL" is a
        // substring of "PostgreSQL", so normalized containment matches them
        // even though they're written as distinct skills. Documented here so
        // a future change to the matching strategy doesn't silently break
        // this understood, accepted tradeoff without a visible test failure.
        assertThat(SkillNameMatcher.matches("SQL", "PostgreSQL")).isTrue();
    }

    @Test
    void doesNotMatchUnrelatedSkills() {
        assertThat(SkillNameMatcher.matches("Docker", "Kubernetes")).isFalse();
        assertThat(SkillNameMatcher.matches("Java", "Python")).isFalse();
    }

    @Test
    void emptyOrNullInputsNeverMatch() {
        assertThat(SkillNameMatcher.matches("", "Java")).isFalse();
        assertThat(SkillNameMatcher.matches(null, "Java")).isFalse();
        assertThat(SkillNameMatcher.matches("Java", null)).isFalse();
    }

    @Test
    void normalizeStripsNonAlphanumericAndLowercases() {
        assertThat(SkillNameMatcher.normalize("Hibernate/JPA")).isEqualTo("hibernatejpa");
        assertThat(SkillNameMatcher.normalize("Hibernate / JPA")).isEqualTo("hibernatejpa");
        assertThat(SkillNameMatcher.normalize("REST APIs")).isEqualTo("restapis");
    }
}
