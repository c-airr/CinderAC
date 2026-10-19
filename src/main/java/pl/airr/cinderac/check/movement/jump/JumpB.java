package pl.airr.cinderac.check.movement.jump;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.data.PositionTracker;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Jump", type = 'B', complexType = "Height", description = "Invalid jump motion.")
public final class JumpB extends Check {
    public JumpB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick()) return;
        if (data.action.flyingTicks < 30) return;
        PositionTracker pos = data.position;
        double dy = pos.deltaY;
        int air = pos.airTicks;
        boolean bed = data.clientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) && pos.sinceBedTicks < 40;
        boolean exempt = exempt(Exempt.JOINED, Exempt.TELEPORT, Exempt.RIPTIDE, Exempt.VEHICLE, Exempt.HONEY, Exempt.GLIDING, Exempt.LEVITATION,
                Exempt.SLAB, Exempt.FLIGHT, Exempt.JUMP_BOOST_RAN_OUT, Exempt.BOAT, Exempt.BUBBLE_COLUMN, Exempt.WALL, Exempt.CREATIVE,
                Exempt.SHULKER_BOX, Exempt.ANVIL, Exempt.SLIME, Exempt.PISTON, Exempt.TURTLE_EGG, Exempt.DOOR, Exempt.DEATH, Exempt.ENDER_PEARL,
                Exempt.PLACED_SLIME, Exempt.CHORUS_FRUIT, Exempt.MYTHIC_MOB, Exempt.BLOCK_BREAK, Exempt.FISHING_ROD, Exempt.SLEEPING,
                Exempt.SHULKER) || pos.sinceFireworkTicks < 80 || pos.sinceWearingElytraTicks < 20;
        if (ServerVersion.atLeast(9) && pos.sinceRiptideTicks < 250) return;
        if (data.action.gravityAttribute < 0.0 || data.action.sinceGravityChangeTicks < 20) return;
        double limit = data.action.jumpStrengthAttribute;
        if (pos.nearDaylightDetector) limit += 0.1;
        if (pos.sinceFarmlandTicks < 10) limit += 1.0;
        if (pos.nearLantern) limit += 0.2;
        if (pos.nearChain) limit += 0.2;
        if (exempt(Exempt.SEA_PICKLE)) limit += 0.2f;
        if (pos.nearCake) limit += 0.1;
        if (pos.nearCampfire) limit += 0.2f;
        if (pos.nearSnow) limit += 0.1f;
        if (pos.nearAmethyst) limit += 0.25;
        if (pos.nearHeavyCore) limit += 0.1;
        if (pos.nearFlowerPot) limit += 0.1;
        if (pos.nearBed) limit += 0.1;
        if (exempt(Exempt.SKULL)) limit += 0.1;
        if (pos.nearPowderSnow) limit += 0.5;
        if (data.action.sinceHoglinDamageTicks < 20) limit += 0.15f;
        if (data.action.hasJumpBoost && data.action.jumpBoostAmplifier > 0) limit += data.action.jumpBoostAmplifier * 0.125f;
        if (pos.sinceFenceTicks < 5) limit += 0.125;
        if (data.action.sinceJumpStrengthChangeTicks < 20) limit += Math.max(data.action.jumpStrengthAttribute, data.action.lastJumpStrength);
        int explosionTicks = data.velocity.sinceExplosionTicks, velocityTicks = data.velocity.sinceVelocityTicks;
        if (data.velocity.hasPendingExplosion) {
            limit += 0.25 + Math.abs(data.velocity.explosionYSum);
        } else if (explosionTicks < 40 && !pos.onGround) {
            limit += Math.abs(data.velocity.lastExplosionY + 0.25) * Math.pow(0.98f, explosionTicks);
        }
        if (data.velocity.hasPendingVelocity) {
            limit += 0.1 + Math.abs(data.velocity.velocityY);
        } else if (velocityTicks < 40 && !pos.onGround) {
            limit += Math.abs(data.velocity.velocityY) * Math.pow(0.98f, velocityTicks);
        }
        if (pos.nearStairs) limit += 0.1;
        if (limit < 0.4) limit = 0.42f;
        if (air > 0 && dy > limit && !exempt && !bed) {
            if (increaseBuffer() > maxBuffer() || dy - limit > 1.0) {
                fail("deltaY=" + dy + " limit=" + limit + " tp=" + data.action.sinceServerPositionTicks + " v=" + data.velocity.velocityY
                        + " e=" + data.velocity.explosionYSum);
            }
        } else {
            decreaseBuffer();
        }
    }
}
