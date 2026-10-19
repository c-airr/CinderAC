package pl.airr.cinderac.check.movement.elytra;

import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.check.VelocityBonus;
import pl.airr.cinderac.check.movement.speed.GroundSpeedCheck;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Elytra", type = 'N', complexType = "Ground", description = "Moving too quickly on the ground.", experimental = true)
public final class ElytraN extends GroundSpeedCheck {
    public ElytraN(PlayerData data) {
        super(data);
    }

    private static final Exempt[] INSTANT = {Exempt.SERVER_POSITION, Exempt.CHORUS_FRUIT};

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin() || skip()) return;
        PositionTracker pos = data.position;
        double dxz = pos.deltaXZ, dy = pos.deltaY;
        int ticks = pos.groundTicks;
        double max = baseLimit(ticks, pos.nearStairs);
        if (ServerVersion.atLeast(16) && pos.sinceSoulSpeedTicks < 30 && data.action.speedAmplifier > 3) max += 1.25;
        max += VelocityBonus.of(data.velocity.sinceVelocityTicks, data.velocity.maxVelocityXZ);
        if (pos.sinceSlabAroundTicks < 5) max += 0.025;
        if (max < 0.295) max = 0.295;
        if ((pos.sinceGlidingTicks < 20 || data.action.sinceGlidingTicks < 20) && ticks < 5) max += pos.lastTickDeltaXZ;
        if (pos.sinceSpeedTicks < 10 && pos.sinceSpeedTicks > 0) max += data.action.lastSpeedAmplifier * 0.075;
        if (pos.sinceFireworkTicks < 20 && ticks < 5) max += 1.5;
        double diff = dxz - max;
        boolean exempt = exempt(Exempt.FLIGHT, Exempt.JOINED, Exempt.CREATIVE, Exempt.SHULKER, Exempt.SHULKER_BOX, Exempt.DOLPHINS_GRACE,
                Exempt.TELEPORT, Exempt.ILLEGAL_BLOCK, Exempt.CHUNK, Exempt.RIPTIDE, Exempt.VEHICLE, Exempt.BOAT, Exempt.FISHING_ROD,
                Exempt.ENTITY_CRAM_FIX, Exempt.DEATH, Exempt.SLEEPING, Exempt.ENDER_PEARL, Exempt.FROZEN, Exempt.CHORUS_FRUIT, Exempt.SPECTATOR,
                Exempt.WORLD_CHANGE, Exempt.ANVIL, Exempt.CANCELLED_MOVE);
        verdict(diff, 0.01, exempt || !data.action.wearingElytra, INSTANT,
                "speed=" + dxz + " max=" + max + " diff=" + diff + " ticks=" + ticks + " deltaY=" + dy);
    }
}
