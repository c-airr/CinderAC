package pl.airr.cinderac.check.movement.antilevitation;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Anti Levitation", type = 'A', complexType = "Ignore", description = "Ignored Levitation effect.")
public final class AntiLevitationA extends Check {
    public AntiLevitationA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick()) return;
        if (!ServerVersion.atLeast(9) || data.action.levitationAmplifier < 0) return;
        double dy = data.position.deltaY;
        boolean math = data.position.mathematicallyOnGround, ground = data.position.onGround;
        boolean invalid = data.action.hasLevitation && (ground || dy < 0.0 || !data.position.allAir && math);
        boolean exempt = exempt(Exempt.SCAFFOLDING, Exempt.VOID, Exempt.COLLIDING_VERTICALLY, Exempt.TRAPDOOR, Exempt.BED, Exempt.SLIME,
                Exempt.HONEY, Exempt.VELOCITY, Exempt.RIPTIDE, Exempt.JOINED, Exempt.SWIMMING, Exempt.CHUNK, Exempt.JOINED_CHUNK_LOAD, Exempt.WEB,
                Exempt.SLEEPING, Exempt.DEATH, Exempt.FROZEN, Exempt.BLOCK_PLACE, Exempt.BLOCK_BREAK, Exempt.FULLY_STUCK, Exempt.PARTIALLY_STUCK,
                Exempt.WORLD_CHANGE, Exempt.VEHICLE, Exempt.BOAT, Exempt.GLIDING, Exempt.SLOW_FALLING, Exempt.BUBBLE_COLUMN, Exempt.SPECTATOR,
                Exempt.DOLPHINS_GRACE, Exempt.SHULKER_BOX, Exempt.FENCE, Exempt.FENCE_GATE, Exempt.STAIRS, Exempt.TELEPORT, Exempt.CLIMBABLE,
                Exempt.FLIGHT, Exempt.CREATIVE, Exempt.LIQUID, Exempt.ELYTRA);
        if (invalid && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy + " onGround=" + ground + " amplifier=" + data.action.levitationAmplifier);
        } else {
            decreaseBuffer();
        }
    }
}
