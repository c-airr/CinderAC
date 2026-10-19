package pl.airr.cinderac.check.movement.jesus;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;

@CheckInfo(name = "Jesus", type = 'C', complexType = "Y Motion", description = "Invalid Y-Change in water.")
public final class JesusC extends Check {
    public JesusC(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        PositionTracker pos = data.position;
        double dy = pos.deltaY;
        if (pos.y > 250.0) return;
        boolean exempt = exempt(Exempt.LILY_PAD, Exempt.STAIRS, Exempt.SLAB, Exempt.CAMPFIRE, Exempt.CARPET, Exempt.END_ROD, Exempt.FENCE, Exempt.BOAT, Exempt.WEB, Exempt.HAPPY_GHAST, Exempt.CANCELLED_PLACE, Exempt.CHAIN, Exempt.SNOW, Exempt.SWIMMING_JESUS,
                Exempt.BED, Exempt.TRAPDOOR, Exempt.FULLY_SUBMERGED, Exempt.VELOCITY, Exempt.BUBBLE_COLUMN, Exempt.SWIMMING_ON_OLD_VERSION,
                Exempt.SOUL_SAND, Exempt.CLIMBABLE, Exempt.SPECTATOR, Exempt.NEAR_SOLID, Exempt.SKULL, Exempt.FLOWER_POT, Exempt.CHUNK,
                Exempt.NOT_MOVING, Exempt.SCAFFOLDING, Exempt.FLIGHT, Exempt.GLIDING, Exempt.ELYTRA, Exempt.LEVITATION, Exempt.VEHICLE,
                Exempt.RIPTIDE, Exempt.TELEPORT)
                || pos.nearHeavyCore || pos.nearRepeater || pos.nearWeb || pos.nearCandle || pos.nearDripleaf;
        if (pos.liquidBelow && !exempt && Math.abs(dy) < 0.001) {
            if (increaseBuffer() > maxBuffer()) {
                fail("deltaY=" + dy);
                data.action.pendingSwimFix = true;
            }
        } else {
            decreaseBuffer();
        }
    }
}
