package pl.airr.cinderac.check.movement.jesus;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Jesus", type = 'E', complexType = "Speed", description = "Moving too quickly on water.")
public final class JesusE extends Check {
    public JesusE(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        PositionTracker pos = data.position;
        double dxz = pos.deltaXZ;
        boolean exempt = exempt(Exempt.LILY_PAD, Exempt.STAIRS, Exempt.SLAB, Exempt.CAMPFIRE, Exempt.CARPET, Exempt.END_ROD, Exempt.FENCE,
                Exempt.FLOWER_POT, Exempt.HAPPY_GHAST, Exempt.NOT_MOVING, Exempt.CANCELLED_PLACE, Exempt.CHAIN, Exempt.SNOW,
                Exempt.SWIMMING_JESUS, Exempt.BED, Exempt.TRAPDOOR, Exempt.BOAT, Exempt.FULLY_SUBMERGED, Exempt.BUBBLE_COLUMN, Exempt.VELOCITY,
                Exempt.DEPTH_STRIDER, Exempt.DOLPHINS_GRACE, Exempt.FENCE_GATE, Exempt.SWIMMING_ON_OLD_VERSION, Exempt.SPECTATOR,
                Exempt.BLOCK_PLACE, Exempt.CLIMBABLE, Exempt.NEAR_SOLID, Exempt.SLOW_FALLING, Exempt.CHUNK, Exempt.WALL, Exempt.SCAFFOLDING,
                Exempt.SLIME, Exempt.SKULL, Exempt.FLIGHT, Exempt.GLIDING, Exempt.ELYTRA, Exempt.LEVITATION, Exempt.VEHICLE, Exempt.RIPTIDE,
                Exempt.TELEPORT)
                || pos.nearDripleaf || pos.nearHeavyCore || pos.nearRepeater || pos.nearCandle;
        double max = 0.166f;
        if (pos.notSolidBelowTicks < 10 || pos.sinceSwimmingTicks < 20) max += 0.35;
        if (ServerVersion.atLeast(9) && pos.sinceNearLivingEntityTicks < 20) max += 0.2;
        if (pos.liquidBelow && !exempt) {
            if (dxz > max && increaseBuffer() > maxBuffer()) {
                fail("deltaXZ=" + dxz + " max=" + max);
                data.action.pendingSwimFix = true;
            }
        } else {
            decreaseBuffer();
        }
    }
}
