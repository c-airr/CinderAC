package pl.airr.cinderac.check.movement.fastclimb;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.Blocks;

@CheckInfo(name = "Fast Climb", type = 'A', complexType = "Limit", description = "Climbing too quickly")
public final class FastClimbA extends Check {
    public FastClimbA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        int ticks = data.position.climbableAtBodyTicks;
        double dy = data.position.deltaY;
        if (data.action.gravityAttribute < 0.08) return;
        double placed = data.action.distanceToLastBreak;
        if (placed < 1.25 && placed > 0.0) return;
        boolean onClimbable = Blocks.CLIMBABLE.is(data.position.below1) && Blocks.CLIMBABLE.is(data.position.below2);
        if (data.velocity.sinceVelocityTicks < 20 || data.velocity.sinceExplosionTicks < 20 || !onClimbable) return;
        boolean invalid = dy > 0.118 && ticks > 3 && data.position.climbableAtBody;
        boolean exempt = exempt(Exempt.JUMP_BOOST, Exempt.FLIGHT, Exempt.CREATIVE, Exempt.GLIDING, Exempt.LEVITATION, Exempt.SLEEPING,
                Exempt.ENDER_PEARL, Exempt.TELEPORT, Exempt.WORLD_CHANGE, Exempt.DEATH, Exempt.SCAFFOLDING, Exempt.LIQUID, Exempt.WATERLOGGED,
                Exempt.BLOCK_PLACE, Exempt.RIPTIDE, Exempt.BUBBLE_COLUMN, Exempt.SLIME, Exempt.PROJECTILE_DAMAGE, Exempt.WALL);
        if (invalid && !exempt) {
            if (increaseBuffer() > maxBuffer() || dy > 1.0) fail("deltaY=" + dy + " ticks=" + ticks);
        } else {
            decreaseBuffer();
        }
    }
}
