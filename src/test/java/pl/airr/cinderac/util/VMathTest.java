package pl.airr.cinderac.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Statistics as in the original (e.g. variance without dividing by n) - checks are calibrated for these formulas. */
class VMathTest {
    private static final List<Double> SAMPLE = Arrays.asList(1.0, 2.0, 3.0, 4.0);

    @Test
    void varianceIsSumOfSquaresWithoutDivision() {
        assertEquals(5.0, VMath.variance(SAMPLE), 1e-12);
        assertEquals(Math.sqrt(5.0), VMath.deviation(SAMPLE), 1e-12);
    }

    @Test
    void skewnessUsesMeanMinusMedianOverVariance() {
        // mean 4, median 2.5, "variance" 50 -> 3 * 1.5 / 50
        assertEquals(0.09, VMath.skewness(Arrays.asList(1.0, 2.0, 3.0, 10.0)), 1e-12);
    }

    @Test
    void cpsFromDelays() {
        assertEquals(20.0, VMath.cps(Arrays.asList(50, 50, 50)), 1e-12);
    }

    @Test
    void longGcdStopsBelowThreshold() {
        // a divisor <= 16384 ends the recursion and returns a (how the original computes mouse sensitivity)
        assertEquals(100L, VMath.gcd(100L, 50L));
        assertEquals(16777216L, VMath.gcd(50331648L, 33554432L));
    }

    @Test
    void doubleGcd() {
        assertEquals(0.1, VMath.gcd(0.3, 0.2), 1e-9);
    }

    @Test
    void modeAndDuplicates() {
        assertEquals(2, VMath.mode(Arrays.asList(1, 2, 2, 3)));
        assertEquals(1, VMath.duplicates(Arrays.asList(1, 1, 2)));
    }
}
