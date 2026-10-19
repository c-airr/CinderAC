package pl.airr.cinderac.check.movement.speed;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;

@CheckInfo(name = "Speed", type = 'D', complexType = "Ground", description = "Moving too quickly on the ground.")
public final class SpeedD extends GroundSpeedCheck {
    public SpeedD(PlayerData data) {
        super(data);
    }

    private static final Exempt[] INSTANT = {Exempt.SERVER_POSITION};

    @Override
    protected boolean variantD() {
        return true;
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin() || skip()) return;
        PositionTracker pos = data.position;
        double dxz = pos.deltaXZ, dy = pos.deltaY;
        boolean inside = pos.insideBlock || pos.insideBlock2;
        int ticks = pos.mathGroundTicks;
        double max = baseLimit(ticks, pos.nearStairs || pos.sinceStairsTicks < 20);
        if (pos.sinceBlockAboveTicks < 5) max += 0.1;
        if (pos.sinceBlockAboveTicks < 20 && ticks < 15 && !inside) max += 0.1;
        if (pos.nearSlab) max += 0.1;
        if (pos.sinceSlabTicks < 20) max += 0.075;
        if (pos.sinceIceTicks < 40) max += 0.125;
        if (pos.nearTrapdoor) max += 0.125;
        if (exempt(Exempt.CAKE)) max += 0.1;
        if (exempt(Exempt.STAIRS, Exempt.SLAB)) max += 0.45;
        if (exempt(Exempt.BED)) max += 0.25;
        if (exempt(Exempt.FENCE)) max += 0.225;
        if (pos.nearAmethyst) {
            max += 0.05f;
            if (dy == 0.4375 || dy == 0.5625) max += 0.25;
        }
        if (exempt(Exempt.SNOW) && dy == 0.5) max += 0.15f;
        if (pos.nearSculkSensor) max += 0.125;
        if (pos.nearLantern) max += 0.075;
        if (pos.nearChain) {
            max += 0.05;
            if (dy == 0.59375) max += 0.2;
        }
        if (pos.nearHeavyCore) max += 0.25;
        max += modernExtras(ticks);
        if (max < 0.295) max = 0.295;
        double diff = dxz - max;
        boolean exempt = exempt(Exempt.FLIGHT, Exempt.JOINED, Exempt.CREATIVE, Exempt.SHULKER, Exempt.SHULKER_BOX, Exempt.DOLPHINS_GRACE,
                Exempt.TELEPORT, Exempt.ILLEGAL_BLOCK, Exempt.CHUNK, Exempt.RIPTIDE, Exempt.VEHICLE, Exempt.BOAT, Exempt.FISHING_ROD,
                Exempt.ENTITY_CRAM_FIX, Exempt.DEATH, Exempt.SLEEPING, Exempt.ENDER_PEARL, Exempt.FROZEN, Exempt.CHORUS_FRUIT, Exempt.SPECTATOR,
                Exempt.WORLD_CHANGE, Exempt.ANVIL, Exempt.CANCELLED_MOVE, Exempt.GLIDING, Exempt.ELYTRA, Exempt.DRAGON_DAMAGE);
        verdict(diff, cfg.getDouble("min-difference", 0.0025), exempt, INSTANT, "speed=" + dxz + " max=" + max + " diff=" + diff
                + " ticks=" + ticks + " deltaY=" + dy + " tp=" + data.action.sinceServerPositionTicks);
    }
}
