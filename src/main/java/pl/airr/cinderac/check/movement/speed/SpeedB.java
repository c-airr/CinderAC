package pl.airr.cinderac.check.movement.speed;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.check.movement.speed.GroundSpeedCheck;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Speed", type = 'B', complexType = "Ground", description = "Moving too quickly on the ground.")
public final class SpeedB extends GroundSpeedCheck {
    public SpeedB(PlayerData data) {
        super(data);
    }

    private static final Exempt[] INSTANT = {Exempt.SERVER_POSITION, Exempt.CHORUS_FRUIT};

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin() || skip()) return;
        double dxz = data.position.deltaXZ, dy = data.position.deltaY;
        int ticks = data.position.groundTicks;
        double max = baseLimit(ticks, data.position.nearStairs);
        if (data.position.sinceSlabAroundTicks < 5) max += 0.025;
        if (data.position.nearLantern) max += 0.02;
        if (data.position.sinceBlockAboveTicks < 10 && (data.position.nearChain || data.position.nearConduit || data.position.nearLantern)) max += 0.1;
        max += modernExtras(ticks);
        if (max < 0.295) max = 0.295;
        double diff = dxz - max;
        boolean exempt = exempt(Exempt.FLIGHT, Exempt.JOINED, Exempt.CREATIVE, Exempt.SHULKER, Exempt.SHULKER_BOX, Exempt.DOLPHINS_GRACE,
                Exempt.TELEPORT, Exempt.ILLEGAL_BLOCK, Exempt.CHUNK, Exempt.RIPTIDE, Exempt.VEHICLE, Exempt.BOAT, Exempt.FISHING_ROD,
                Exempt.ENTITY_CRAM_FIX, Exempt.DEATH, Exempt.SLEEPING, Exempt.ENDER_PEARL, Exempt.FROZEN, Exempt.CHORUS_FRUIT, Exempt.SPECTATOR,
                Exempt.WORLD_CHANGE, Exempt.ANVIL, Exempt.CANCELLED_MOVE, Exempt.ELYTRA, Exempt.GLIDING, Exempt.DRAGON_DAMAGE);
        verdict(diff, cfg.getDouble("min-difference", 0.0025), exempt, INSTANT,
                "speed=" + dxz + " max=" + max + " diff=" + diff + " ticks=" + ticks + " deltaY=" + dy);
    }
}
