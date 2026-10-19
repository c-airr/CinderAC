package pl.airr.cinderac.check.movement.flight;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;

@CheckInfo(name = "Flight", type = 'D', complexType = "Glide", description = "Invalid Y-Axis movement.")
public final class FlightD extends Check {
    public FlightD(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        PositionTracker pos = data.position;
        if (cfg.getBoolean("ignore-ghost-blocks", true) && pos.onGround && pos.allAir && pos.mathematicallyOnGround) return;
        if (data.action.gravityAttribute != 0.08) return;
        if (Settings.get().ghostBlocksIgnoreLegacyVia && pos.y < 5.0 && pos.y > -5.0) return;
        int ticks = pos.allAirTicks;
        double dy = pos.deltaY;
        if (data.action.sinceDragonDamageTicks < 200) return;
        if (dy + 0.09800000190734881 <= 0.001) return;
        if (data.action.jumpStrengthAttribute > 2.0 || data.action.sinceGravityChangeTicks < 20) return;
        boolean explosion = data.velocity.sinceExplosionTicks < 100;
        boolean bed = pos.sinceBedTicks < 30;
        boolean exempt = exempt(Exempt.LAGGED_NEAR_GROUND, Exempt.DEAD, Exempt.WEB, Exempt.JUMP_BOOST_RAN_OUT, Exempt.EMPTIED_BUCKET,
                Exempt.WORLD_CHANGE, Exempt.ILLEGAL_BLOCK, Exempt.BOAT, Exempt.LIQUID, Exempt.NOT_MOVING, Exempt.FENCE, Exempt.SHULKER,
                Exempt.TRAPDOOR, Exempt.SHULKER_BOX, Exempt.NEAR_GROUND, Exempt.FISHING_ROD, Exempt.LAGGED_NEAR_GROUND_MODERN, Exempt.VOID,
                Exempt.CANCELLED_PLACE, Exempt.SPECTATOR, Exempt.SERVER_POSITION, Exempt.PISTON, Exempt.HIGH_JUMP_BOOST, Exempt.SLIME,
                Exempt.PARTIALLY_STUCK, Exempt.FULLY_STUCK, Exempt.RESPAWN, Exempt.PLACED_SLIME, Exempt.GLIDING, Exempt.ELYTRA,
                Exempt.FLIGHT, Exempt.CREATIVE, Exempt.JOINED, Exempt.LIQUID, Exempt.LEVITATION, Exempt.SLOW_FALLING, Exempt.RIPTIDE,
                Exempt.VEHICLE, Exempt.LENIENT_SCAFFOLDING, Exempt.TELEPORT, Exempt.ENDER_PEARL, Exempt.CHUNK, Exempt.COMBO_MODE,
                Exempt.MYTHIC_MOB, Exempt.CLIMBABLE) || pos.slabAbove;
        int limit = 18;
        if (exempt(Exempt.BLOCK_PLACE, Exempt.CANCELLED_PLACE)) limit += 30;
        boolean invalid = ticks > limit && dy > -0.5;
        int velocity = data.velocity.sinceVelocityTicks;
        if (invalid && !exempt && !explosion && !bed && velocity > 30) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy + " ticks=" + ticks + " delay=" + data.keepAlive.flyingDelayMillis + " tp=" + data.action.sinceServerPositionTicks + " v=" + velocity);
        } else {
            decreaseBuffer();
        }
    }
}
