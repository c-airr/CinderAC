package pl.airr.cinderac.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Statistics in exactly the form the check thresholds are calibrated for.
 * Note: "variance" is the sum of squared deviations (not divided by n), "deviation" is its square root.
 */
public final class VMath {
    public static final double EXPANDER = Math.pow(2.0, 24.0);

    private VMath() {
    }

    public static double hypot(double x, double z) {
        return Math.sqrt(x * x + z * z);
    }

    public static double magnitude(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static double mean(Collection<? extends Number> data) {
        if (data == null || data.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Number n : data) sum += n.doubleValue();
        return sum / data.size();
    }

    /** Sum of squared deviations from the mean (not divided by n). */
    public static double variance(Collection<? extends Number> data) {
        double sum = 0.0;
        int n = 0;
        for (Number d : data) {
            sum += d.doubleValue();
            n++;
        }
        double mean = sum / n;
        double var = 0.0;
        for (Number d : data) var += Math.pow(d.doubleValue() - mean, 2.0);
        return var;
    }

    public static double deviation(Collection<? extends Number> data) {
        return Math.sqrt(variance(data));
    }

    /** 3 * (mean - median) / variance(). */
    public static double skewness(Collection<? extends Number> data) {
        double sum = 0.0;
        int n = 0;
        List<Double> sorted = new ArrayList<>();
        for (Number d : data) {
            sum += d.doubleValue();
            n++;
            sorted.add(d.doubleValue());
        }
        Collections.sort(sorted);
        double mean = sum / n;
        double median = n % 2 != 0 ? sorted.get(n / 2) : (sorted.get((n - 1) / 2) + sorted.get(n / 2)) / 2.0;
        return 3.0 * (mean - median) / variance(data);
    }

    /** Kurtosis variant with the denominator (sum of squares / sum)^2. */
    public static double kurtosis(Collection<? extends Number> data) {
        double sum = 0.0;
        int n = 0;
        for (Number d : data) {
            sum += d.doubleValue();
            n++;
        }
        if (n < 3.0) return 0.0;
        double a = n * (n + 1.0) / ((n - 1.0) * (n - 2.0) * (n - 3.0));
        double b = 3.0 * Math.pow(n - 1.0, 2.0) / ((n - 2.0) * (n - 3.0));
        double mean = sum / n;
        double s2 = 0.0, s4 = 0.0;
        for (Number d : data) {
            s2 += Math.pow(mean - d.doubleValue(), 2.0);
            s4 += Math.pow(mean - d.doubleValue(), 4.0);
        }
        return a * (s4 / Math.pow(s2 / sum, 2.0)) - b;
    }

    public static double median(List<Double> sorted) {
        if (sorted.size() % 2 == 0) return (sorted.get(sorted.size() / 2) + sorted.get(sorted.size() / 2 - 1)) / 2.0;
        return sorted.get(sorted.size() / 2);
    }

    /** CPS from a sample of delays in ms. */
    public static double cps(Collection<? extends Number> delays) {
        return 20.0 / mean(delays) * 50.0;
    }

    public static int duplicates(Collection<?> data) {
        return data.size() - new HashSet<>(data).size();
    }

    public static int distinct(Collection<?> data) {
        return new HashSet<>(data).size();
    }

    /** Most frequent value (on a tie the first one found wins). */
    public static int mode(Collection<Integer> data) {
        int best = data.iterator().next();
        int bestCount = 0;
        for (Integer a : data) {
            int count = 1;
            for (Integer b : data) {
                if (b.equals(a)) count++;
                if (count <= bestCount) continue;
                best = a;
                bestCount = count;
            }
        }
        return best;
    }

    /** Most frequent numeric value (any Number type). */
    public static Number modeElevated(Collection<? extends Number> data) {
        Map<Number, Integer> counts = new HashMap<>();
        for (Number n : data) counts.put(n, counts.getOrDefault(n, 0) + 1);
        Number best = null;
        int bestCount = -1;
        for (Map.Entry<Number, Integer> e : counts.entrySet()) {
            if (e.getValue() > bestCount) {
                best = e.getKey();
                bestCount = e.getValue();
            }
        }
        if (best == null) throw new NullPointerException();
        return best;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    /** Whether a number < 1 is printed in scientific notation (very small values). */
    public static boolean isScientific(Number n) {
        return n.doubleValue() < 1.0 && Double.toString(n.doubleValue()).contains("E");
    }

    public static long gcd(long a, long b) {
        return b <= 16384L ? a : gcd(b, a % b);
    }

    public static double gcd(double a, double b) {
        if (a < b) return gcd(b, a);
        if (Math.abs(b) < 0.001) return a;
        return gcd(b, a - Math.floor(a / b) * b);
    }

    /** Smallest difference between two angles (0..180). */
    public static float angleDistance(float a, float b) {
        float d = Math.abs(a % 360.0f - b % 360.0f);
        return (float) Math.abs(Math.min(360.0 - d, d));
    }

    public static double angleDistance(double a, double b) {
        double d = Math.abs(a % 360.0 - b % 360.0);
        return Math.abs(Math.min(360.0 - d, d));
    }

    public static double rawAngleDifference(double a, double b) {
        return Math.abs(a % 360.0 - b % 360.0);
    }

    /** Angle (0..360) from (x1,z1) to (x2,z2), computed as atan2(dz, dx). */
    public static double angleTo(double x1, double z1, double x2, double z2) {
        double deg = Math.toDegrees(Math.atan2(z1 - z2, x2 - x1));
        return deg < 0.0 ? 360.0 + deg : deg;
    }

    public static float wrapDegrees(float v) {
        v %= 360.0f;
        if (v >= 180.0f) v -= 360.0f;
        if (v < -180.0f) v += 360.0f;
        return v;
    }
}
