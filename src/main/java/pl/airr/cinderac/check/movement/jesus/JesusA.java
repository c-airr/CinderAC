package pl.airr.cinderac.check.movement.jesus;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;

@CheckInfo(name = "Jesus", type = 'A', complexType = "Ground", description = "Walking on water.")
public final class JesusA extends Check {
    public JesusA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        PositionTracker pos = data.position;
        double dxz = pos.deltaXZ;
        if (pos.y > 250.0) return;
        boolean exempt = exempt(Exempt.LILY_PAD, Exempt.STAIRS, Exempt.SLAB, Exempt.CAMPFIRE, Exempt.CARPET, Exempt.END_ROD, Exempt.FENCE, Exempt.BOAT, Exempt.HAPPY_GHAST, Exempt.CANCELLED_PLACE, Exempt.CHAIN, Exempt.SNOW, Exempt.SWIMMING_JESUS, Exempt.BED,
                Exempt.TRAPDOOR, Exempt.DOLPHINS_GRACE, Exempt.FLOWER_POT, Exempt.SPECTATOR, Exempt.SCAFFOLDING, Exempt.SKULL,
                Exempt.SEA_PICKLE, Exempt.NEAR_SOLID, Exempt.NOT_MOVING, Exempt.BLOCK_PLACE, Exempt.FLIGHT, Exempt.GLIDING, Exempt.LEVITATION,
                Exempt.VEHICLE, Exempt.RIPTIDE, Exempt.TELEPORT, Exempt.CHUNK)
                || pos.nearHeavyCore || pos.nearRepeater || pos.nearCandle || pos.nearDripleaf || pos.sinceBoatTicks < 20;
        if (pos.liquidBelow && (pos.onGround || pos.mathematicallyOnGround) && !exempt && dxz > 0.1) {
            if (increaseBuffer() > maxBuffer()) {
                failNoInfo();
                data.action.pendingSwimFix = true;
            }
        } else {
            decreaseBuffer();
        }
    }
}
