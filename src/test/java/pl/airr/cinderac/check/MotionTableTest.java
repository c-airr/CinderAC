package pl.airr.cinderac.check;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MotionTableTest {
    @Test
    void jumpFollowsVanillaGravity() {
        assertNull(MotionTable.jump(0));
        assertEquals(0.41999998688697815, MotionTable.jump(1), 0.0);
        // next tick: (v - 0.08) * 0.98 (float as in the client)
        for (int t = 2; t <= 10; t++) {
            if (t == 6) continue;
            assertEquals((MotionTable.jump(t - 1) - 0.08) * 0.98f, MotionTable.jump(t), 1e-6, "tick " + t);
        }
        // at the apex the ~0.003 velocity is zeroed by the client, so it continues as a fall from zero
        assertEquals(MotionTable.fall(1), MotionTable.jump(6), 0.0);
        assertNull(MotionTable.jump(12));
    }

    @Test
    void fallStartsFromZero() {
        assertEquals(-0.07840000152587834, MotionTable.fall(1), 0.0);
        assertEquals((MotionTable.fall(1) - 0.08) * 0.98f, MotionTable.fall(2), 1e-6);
        assertNull(MotionTable.fall(8));
    }
}
