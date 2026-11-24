package com.careercompass.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class EducationLevelUtilTest {

    @ParameterizedTest
    @CsvSource({
            "PhD, 4",
            "Doctorate, 4",
            "M.Tech, 3",
            "MSc, 3",
            "MBA, 3",
            "Master's Degree, 3",
            "B.Tech, 2",
            "BSc, 2",
            "Bachelor of Engineering, 2",
            "Diploma, 1",
            "Associate Degree, 1"
    })
    void ranksKnownEducationLevelsCorrectly(String level, int expectedRank) {
        assertThat(EducationLevelUtil.rank(level)).isEqualTo(expectedRank);
    }

    @Test
    void unknownTextDefaultsToNeutralRankRatherThanPenalizing() {
        assertThat(EducationLevelUtil.rank("Some Unrecognized Qualification")).isEqualTo(2);
    }

    @Test
    void nullOrBlankDefaultsToNeutralRank() {
        assertThat(EducationLevelUtil.rank(null)).isEqualTo(2);
        assertThat(EducationLevelUtil.rank("")).isEqualTo(2);
        assertThat(EducationLevelUtil.rank("   ")).isEqualTo(2);
    }

    @Test
    void rankingIsCaseInsensitive() {
        assertThat(EducationLevelUtil.rank("phd")).isEqualTo(EducationLevelUtil.rank("PHD"));
        assertThat(EducationLevelUtil.rank("b.tech")).isEqualTo(EducationLevelUtil.rank("B.TECH"));
    }
}
