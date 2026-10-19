package pl.airr.cinderac.check.movement.jesus;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;
import pl.airr.cinderac.util.Blocks;

@CheckInfo(name = "Jesus", type = 'D', complexType = "Jump", description = "Jumping on/in water.")
public final class JesusD extends Check {
    public JesusD(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        PositionTracker pos = data.position;
        double dy = pos.deltaY;
        if (pos.y > 250.0) return;
        boolean airBelow = pos.below1 != null && Blocks.isAir(pos.below1);
        boolean exempt = exempt(Exempt.LILY_PAD, Exempt.STAIRS, Exempt.SLAB, Exempt.CAMPFIRE, Exempt.CARPET, Exempt.END_ROD, Exempt.FENCE, Exempt.BOAT, Exempt.NOT_MOVING, Exempt.CANCELLED_PLACE, Exempt.CHAIN, Exempt.SNOW, Exempt.SWIMMING_JESUS, Exempt.BED,
                Exempt.TRAPDOOR, Exempt.FULLY_SUBMERGED, Exempt.VELOCITY, Exempt.HAPPY_GHAST, Exempt.BUBBLE_COLUMN,
                Exempt.SWIMMING_ON_OLD_VERSION, Exempt.SPECTATOR, Exempt.CLIMBABLE, Exempt.NEAR_SOLID, Exempt.SLIME, Exempt.HONEY, Exempt.CHUNK,
                Exempt.SCAFFOLDING, Exempt.SKULL, Exempt.FLIGHT, Exempt.GLIDING, Exempt.ELYTRA, Exempt.LEVITATION, Exempt.VEHICLE,
                Exempt.RIPTIDE, Exempt.TELEPORT)
                || pos.nearRepeater || pos.nearHeavyCore || pos.nearDripleaf;
        if (pos.liquidBelow && airBelow && !exempt) {
            // in the original the buffer only grows when moving up; with dy <= 0 nothing happens
            if (dy > 0.0 && increaseBuffer() > maxBuffer()) {
                fail("deltaY=" + dy);
                data.action.pendingSwimFix = true;
            }
        } else {
            decreaseBuffer();
        }
    }
}
