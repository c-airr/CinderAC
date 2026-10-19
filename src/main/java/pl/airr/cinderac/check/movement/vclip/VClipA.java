package pl.airr.cinderac.check.movement.vclip;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.MotionTable;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "VClip", type = 'A', complexType = "Clip", description = "Large vertical movement.")
public final class VClipA extends Check {
    public VClipA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        if (data.action.flyingTicks < 30) return;
        int air = data.position.airTicks;
        double dy = data.position.deltaY;
        if (data.action.gravityAttribute > 0.1) return;
        if (air >= 5 || air <= 0) return;
        double diff = Math.abs(dy - MotionTable.fall(air));
        boolean velocity = data.velocity.sinceVelocityTicks < 50;
        boolean damage = data.action.sinceDamageTicks < 20;
        boolean exempt = exempt(Exempt.VELOCITY, Exempt.JOINED, Exempt.TELEPORT, Exempt.LIQUID, Exempt.CLIMBABLE, Exempt.GLIDING, Exempt.RIPTIDE,
                Exempt.COLLIDING_VERTICALLY, Exempt.HOPPER, Exempt.SLIME, Exempt.SCAFFOLDING, Exempt.BOAT, Exempt.TRAPDOOR, Exempt.STAIRS,
                Exempt.SHULKER, Exempt.FLIGHT, Exempt.CREATIVE, Exempt.SLAB, Exempt.ILLEGAL_BLOCK, Exempt.FENCE, Exempt.JUMP_BOOST, Exempt.HONEY,
                Exempt.FISHING_ROD, Exempt.WEB, Exempt.FULLY_STUCK, Exempt.SHULKER_BOX, Exempt.ELYTRA, Exempt.VEHICLE, Exempt.BED,
                Exempt.BLOCK_PLACE, Exempt.SLEEPING, Exempt.CANCELLED_BREAK, Exempt.ANVIL, Exempt.PISTON, Exempt.TURTLE_EGG, Exempt.DOOR,
                Exempt.DEATH, Exempt.PARTIALLY_STUCK, Exempt.WALL, Exempt.PLACED_WEB, Exempt.ENDER_PEARL, Exempt.LEVITATION, Exempt.BUBBLE_COLUMN,
                Exempt.FROZEN, Exempt.BLOCK_BREAK, Exempt.SWEET_BERRIES, Exempt.EMPTIED_BUCKET, Exempt.FARMLAND, Exempt.LENIENT_SCAFFOLDING,
                Exempt.ATTACK_DAMAGE, Exempt.SEAGRASS, Exempt.DRIPSTONE, Exempt.WATERLOGGED, Exempt.CHEST, Exempt.SOUL_SAND,
                Exempt.GLITCHED_BLOCKS_ABOVE, Exempt.POWDER_SNOW);
        if (!velocity && !damage && !exempt && dy < 0.0 && diff > 0.75) fail("deltaY=" + dy + " ticks=" + air);
    }
}
