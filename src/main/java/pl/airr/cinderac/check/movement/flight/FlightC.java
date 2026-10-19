package pl.airr.cinderac.check.movement.flight;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;

@CheckInfo(name = "Flight", type = 'C', complexType = "Ascension", description = "Invalid Y-Axis movement.")
public final class FlightC extends Check {
    public FlightC(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        PositionTracker pos = data.position;
        if (cfg.getBoolean("ignore-ghost-blocks", true) && pos.onGround && pos.allAir && pos.mathematicallyOnGround) return;
        if (data.action.gravityAttribute != 0.08) return;
        if (Settings.get().ghostBlocksIgnoreLegacyVia && pos.y < 5.0 && pos.y > -5.0) return;
        if (data.action.sinceDragonDamageTicks < 200) return;
        int ticks = pos.allAirTicks;
        double dy = pos.deltaY;
        int limit = 7;
        if (exempt(Exempt.BLOCK_PLACE, Exempt.CANCELLED_PLACE)) limit += 15;
        // as in the original: skips every move with |dy| below ~0.084, not just the jump apex
        if (Math.abs(dy) - 0.08307781780646728 < 0.001) return;
        boolean invalid = ticks > limit && dy > 0.0;
        boolean bed = pos.sinceBedTicks < 30;
        boolean explosion = data.velocity.sinceExplosionTicks < 100;
        boolean slime = pos.sinceSlimeTicks < 80 || data.action.sincePlaceSlimeTicks < 80;
        boolean exempt = exempt(Exempt.SHULKER_BOX, Exempt.BOAT, Exempt.WORLD_CHANGE, Exempt.JUMP_BOOST, Exempt.FENCE, Exempt.LAGGED_NEAR_GROUND,
                Exempt.ILLEGAL_BLOCK, Exempt.SHULKER, Exempt.FROZEN, Exempt.FISHING_ROD, Exempt.CANCELLED_PLACE, Exempt.NOT_MOVING,
                Exempt.SPECTATOR, Exempt.CHORUS_FRUIT, Exempt.PISTON, Exempt.NEAR_GROUND, Exempt.LAGGED_NEAR_GROUND_MODERN,
                Exempt.EMPTIED_BUCKET, Exempt.GLIDING, Exempt.ELYTRA, Exempt.PLACED_SLIME, Exempt.DEAD,
                Exempt.FLIGHT, Exempt.CREATIVE, Exempt.JOINED, Exempt.LIQUID, Exempt.LEVITATION, Exempt.SLOW_FALLING, Exempt.RIPTIDE,
                Exempt.VEHICLE, Exempt.LENIENT_SCAFFOLDING, Exempt.TELEPORT, Exempt.ENDER_PEARL, Exempt.CHUNK, Exempt.COMBO_MODE,
                Exempt.MYTHIC_MOB, Exempt.CLIMBABLE) || pos.slabAbove;
        boolean damage = data.action.sinceNonFallDamageTicks < 40 && ticks < 30 || data.velocity.sinceVelocityAppliedTicks < 40;
        if (invalid && !exempt && !slime && !damage && !bed && !explosion) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy + " ticks=" + ticks + " delay=" + data.keepAlive.flyingDelayMillis + " tp=" + data.action.sinceServerPositionTicks + " v=" + data.velocity.sinceVelocityTicks);
        } else {
            decreaseBuffer();
        }
    }
}
