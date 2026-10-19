package pl.airr.cinderac.check;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

/** Entity attributes across versions (1.9-1.21.2: GENERIC_*, newer: no prefix). */
public final class Attributes {
    private static Attribute movementSpeed;

    private Attributes() {
    }

    /** Base movement speed of an entity; 0 when unavailable. */
    public static double movementSpeed(Attributable entity) {
        if (movementSpeed == null) movementSpeed = lookup("GENERIC_MOVEMENT_SPEED", "MOVEMENT_SPEED");
        if (movementSpeed == null) return 0.0;
        AttributeInstance a = entity.getAttribute(movementSpeed);
        return a == null ? 0.0 : a.getBaseValue();
    }

    private static Attribute lookup(String... names) {
        for (String n : names) {
            try {
                return (Attribute) Attribute.class.getField(n).get(null);
            } catch (ReflectiveOperationException ignored) {
                // try the next name
            }
        }
        return null;
    }
}
