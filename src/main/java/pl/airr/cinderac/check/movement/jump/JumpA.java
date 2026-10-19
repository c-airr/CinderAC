package pl.airr.cinderac.check.movement.jump;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.MotionTable;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;
import pl.airr.cinderac.util.VMath;

@CheckInfo(name = "Jump", type = 'A', complexType = "Motion", description = "Invalid jump motion.")
public final class JumpA extends Check {
    public JumpA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick()) return;
        PositionTracker pos = data.position;
        // the first condition of the original (an action flag) is never set - only the bed check remains
        if (pos.sinceBedTicks < 30) return;
        int air = pos.airTicks;
        double dy = pos.deltaY;
        if (air == 6 && dy < 0.033 || air == 7 && dy < 0.033) return;
        if (data.action.jumpStrengthAttribute != 0.42 || data.action.sinceGravityChangeTicks < 20
                || data.action.sinceJumpStrengthChangeTicks < 20 || data.action.gravityAttribute != 0.08) return;
        if (air >= 9 || air <= 0) return;
        if (MotionTable.ghostWater(pos, dy)) return;
        double diff = Math.abs(dy - (MotionTable.jump(air) + data.action.jumpBoostAmplifier * 0.1f));
        // right after joining the position is unreliable until the player moves away from the join spot
        if (System.currentTimeMillis() - data.joinTime < 30000L
                && VMath.magnitude(pos.x - pos.joinX, pos.y - pos.joinY, pos.z - pos.joinZ) < 4.0) return;
        boolean damage = data.action.sinceDamageTicks < 20;
        boolean bridge = !data.action.bridging && exempt(Exempt.BLOCK_PLACE);
        boolean explosion = data.velocity.sinceExplosionTicks < 20;
        boolean exempt = exempt(Exempt.VELOCITY, Exempt.COLLIDING_VERTICALLY, Exempt.HOPPER, Exempt.SLIME, Exempt.SCAFFOLDING, Exempt.BOAT, Exempt.TRAPDOOR,
                Exempt.STAIRS, Exempt.SHULKER, Exempt.FLIGHT, Exempt.CREATIVE, Exempt.SLAB, Exempt.ILLEGAL_BLOCK, Exempt.JUMP_BOOST_RAN_OUT,
                Exempt.FENCE, Exempt.JUMP_BOOST, Exempt.HONEY, Exempt.FISHING_ROD, Exempt.WEB, Exempt.FULLY_STUCK, Exempt.SHULKER_BOX,
                Exempt.VEHICLE, Exempt.BED, Exempt.SEA_PICKLE, Exempt.CHAIN, Exempt.SLEEPING, Exempt.CANCELLED_BREAK, Exempt.ANVIL,
                Exempt.PISTON, Exempt.TURTLE_EGG, Exempt.DOOR, Exempt.DEATH, Exempt.PARTIALLY_STUCK, Exempt.WALL, Exempt.PLACED_WEB,
                Exempt.BUBBLE_COLUMN, Exempt.FROZEN, Exempt.KELP, Exempt.BLOCK_BREAK, Exempt.SWEET_BERRIES, Exempt.EMPTIED_BUCKET,
                Exempt.FARMLAND, Exempt.LENIENT_SCAFFOLDING, Exempt.ATTACK_DAMAGE, Exempt.SEAGRASS, Exempt.DRIPSTONE, Exempt.WATERLOGGED,
                Exempt.CHEST, Exempt.SOUL_SAND, Exempt.GLITCHED_BLOCKS_ABOVE, Exempt.POWDER_SNOW, Exempt.SIGN, Exempt.JOINED, Exempt.TELEPORT,
                Exempt.LIQUID, Exempt.CLIMBABLE, Exempt.GLIDING, Exempt.RIPTIDE, Exempt.ELYTRA, Exempt.ENDER_PEARL, Exempt.LEVITATION, Exempt.SNOW, Exempt.CONDUIT, Exempt.PACKET_EXPLOSION)
                || pos.nearCandle || pos.nearWeb || pos.nearHeavyCore || pos.nearAmethyst || pos.nearLantern || pos.nearDriedGhast;
        if (dy > 0.0 && diff > 1.0E-10 && !exempt && !explosion && !damage && !bridge) {
            if (increaseBuffer() > maxBuffer() || diff > 0.5) fail("deltaY=" + dy + " ticks=" + air + " difference=" + diff);
        } else {
            decreaseBuffer();
        }
    }
}
