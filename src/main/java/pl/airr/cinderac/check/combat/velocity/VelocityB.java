package pl.airr.cinderac.check.combat.velocity;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.VMath;

@CheckInfo(name = "Velocity", type = 'B', complexType = "Horizontal", description = "Horizontal velocity modifications.")
public final class VelocityB extends Check {
    public VelocityB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isFlying() || data.position.nearWorldBorder) return;
        if (data.position.blocksAroundNotAir || data.position.blockAbove || !data.velocity.hasPendingVelocity) return;
        double dxz = data.position.deltaXZ, dy = data.position.deltaY;
        double velocity = VMath.hypot(data.velocity.velocityX, data.velocity.velocityZ);
        if (velocity < 0.01 || dy == (double) 0.42f) return;
        double ratio = dxz / velocity;
        // the original requires an empty velocity queue while "hasPending" is set - in practice the check rarely gets past this
        if (!data.velocity.pendingVelocities.isEmpty()) return;
        boolean exempt = exempt(Exempt.COLLIDING_VERTICALLY, Exempt.LIQUID, Exempt.FROZEN, Exempt.CHUNK, Exempt.FALL_DAMAGE, Exempt.ENDER_PEARL,
                Exempt.WEB, Exempt.VOID, Exempt.SLOW_FALLING, Exempt.PLACED_WEB, Exempt.TRAPDOOR, Exempt.SOUL_SAND, Exempt.SLIME,
                Exempt.COMBO_MODE, Exempt.FLIGHT, Exempt.NETHERITE_ARMOR, Exempt.VEHICLE, Exempt.WALL, Exempt.MYTHIC_MOB, Exempt.ELYTRA,
                Exempt.SWEET_BERRIES, Exempt.FENCE);
        if (ratio < 0.21 && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("percent=" + ratio * 100.0 + "% deltaXZ=" + dxz + " velocityXZ=" + velocity);
        } else {
            decreaseBuffer();
        }
    }
}
