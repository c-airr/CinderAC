package pl.airr.cinderac.check.movement.wallclimb;

import org.bukkit.Material;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.Blocks;

@CheckInfo(name = "Wall Climb", type = 'A', complexType = "Spider", description = "Climbing up a wall.")
public final class WallClimbA extends Check {
    public WallClimbA(PlayerData data) {
        super(data);
    }

    private int edgeTicks;

    @Override
    public void handle(Packet p) {
        if (!p.isPosition()) return;
        double xm = Math.abs(data.position.x % 1.0), zm = Math.abs(data.position.z % 1.0);
        double dy = data.position.deltaY;
        if (data.action.gravityAttribute != 0.08 || data.action.sinceGravityChangeTicks < 30) return;
        if (Math.abs(dy - 0.20000005f) < 0.001) return;
        double placed = data.action.distanceToLastBreak;
        if (placed < 1.25 && placed > 0.0) return;
        boolean blockAbove = data.position.blockAbove;
        boolean edge = xm > 0.67 && xm < 0.73 || zm > 0.67 && zm < 0.73 || xm > 0.27 && xm < 0.33 || zm > 0.27 && zm < 0.33;
        edgeTicks = edge ? edgeTicks + 1 : 0;
        if (data.position.below1 != null && !Blocks.isAir(data.position.below1)) edgeTicks = 0;
        if (data.position.blocksAbove == null || data.position.blocksBelow == null) return;
        int airAbove = 0, airBelow = 0;
        for (Material m : data.position.blocksAbove) if (Blocks.isAir(m)) airAbove++;
        for (Material m : data.position.blocksBelow) if (Blocks.isAir(m)) airBelow++;
        boolean invalid = edgeTicks > 5 && dy > 0.1 && airAbove == airBelow;
        boolean exempt = exempt(Exempt.CLIMBABLE, Exempt.SCAFFOLDING, Exempt.JUMP_BOOST, Exempt.ELYTRA, Exempt.SNOW, Exempt.BLOCK_PLACE, Exempt.FLIGHT,
                Exempt.CREATIVE, Exempt.GLIDING, Exempt.LEVITATION, Exempt.DEAD, Exempt.HAPPY_GHAST, Exempt.VELOCITY, Exempt.RIPTIDE,
                Exempt.CANCELLED_PLACE, Exempt.NEAR_GROUND, Exempt.POWDER_SNOW, Exempt.BOAT, Exempt.SLIME, Exempt.LIQUID, Exempt.END_ROD,
                Exempt.SHULKER, Exempt.SHULKER_BOX, Exempt.FISHING_ROD, Exempt.FENCE, Exempt.WALL, Exempt.PACKET_EXPLOSION, Exempt.KELP,
                Exempt.CARPET, Exempt.BUBBLE_COLUMN, Exempt.WATERLOGGED, Exempt.EMPTIED_BUCKET, Exempt.TRAPDOOR);
        if (invalid && !exempt && blockAbove) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy + " ticks=" + edgeTicks);
        } else {
            decreaseBuffer();
        }
    }
}
