package pl.airr.cinderac.check.movement.jesus;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;

@CheckInfo(name = "Jesus", type = 'B', complexType = "Motion", description = "Invalid Y-change in water.")
public final class JesusB extends Check {
    public JesusB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        PositionTracker pos = data.position;
        double dy = pos.deltaY, dxz = pos.deltaXZ;
        if (pos.y > 250.0) return;
        boolean exempt = exempt(Exempt.LILY_PAD, Exempt.STAIRS, Exempt.SLAB, Exempt.CAMPFIRE, Exempt.CARPET, Exempt.END_ROD, Exempt.FENCE, Exempt.BOAT, Exempt.FLOWER_POT, Exempt.CANCELLED_PLACE, Exempt.CHAIN, Exempt.SNOW, Exempt.SWIMMING_JESUS, Exempt.BED,
                Exempt.TRAPDOOR, Exempt.FULLY_SUBMERGED, Exempt.BUBBLE_COLUMN, Exempt.VELOCITY, Exempt.DEPTH_STRIDER, Exempt.DOLPHINS_GRACE,
                Exempt.SEA_PICKLE, Exempt.SWIMMING_ON_OLD_VERSION, Exempt.SPECTATOR, Exempt.SOUL_SAND, Exempt.NOT_MOVING, Exempt.NEAR_SOLID,
                Exempt.CHUNK, Exempt.SKULL, Exempt.CLIMBABLE, Exempt.HAPPY_GHAST, Exempt.SCAFFOLDING, Exempt.SLOW_FALLING, Exempt.FLIGHT,
                Exempt.GLIDING, Exempt.ELYTRA, Exempt.LEVITATION, Exempt.VEHICLE, Exempt.RIPTIDE, Exempt.TELEPORT)
                || pos.nearHeavyCore || pos.nearCandle || pos.nearRepeater || pos.nearDripleaf || pos.sinceBoatTicks < 20;
        double max = 0.11;
        if (pos.notSolidBelowTicks < 10) max += 0.05;
        // without water below the buffer is left unchanged
        if (!pos.liquidBelow || exempt) return;
        if (Math.abs(dy) < 0.015 && dxz > max) {
            if (increaseBuffer() > maxBuffer()) {
                fail("deltaY=" + dy + " deltaXZ=" + dxz);
                data.action.pendingSwimFix = true;
            }
        } else {
            decreaseBuffer();
        }
    }
}
