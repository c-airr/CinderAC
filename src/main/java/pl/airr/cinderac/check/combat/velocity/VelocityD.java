package pl.airr.cinderac.check.combat.velocity;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import org.bukkit.Location;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.util.McMath;

@CheckInfo(name = "Velocity", type = 'D', complexType = "Horizontal", description = "Horizontal velocity modifications.", experimental = true)
public final class VelocityD extends Check {
    public VelocityD(PlayerData data) {
        super(data);
    }

    private boolean moved, lastMoved;
    private int attacks;

    /** Movement key combinations (forward, strafe) as in the client: 8 directions + no input. */
    private static final float[][] INPUTS = {{1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {0, 0}};

    @Override
    public void handle(Packet p) {
        if (p.isAttack()) {
            if (p.interact().getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) ++attacks;
            return;
        }
        if (!p.isFlying() || isServerPositionTick() || data.position.nearWorldBorder) return;
        int ticks = data.velocity.sinceVelocityTicks;
        boolean pending = data.velocity.hasPendingVelocity;
        boolean exempt = exempt(Exempt.SLOW_FALLING, Exempt.LEVITATION, Exempt.COLLIDING_VERTICALLY, Exempt.COLLIDING_HORIZONTALLY, Exempt.ELYTRA,
                Exempt.GLIDING, Exempt.RIPTIDE, Exempt.ENTITY_COLLISION, Exempt.SWEET_BERRIES, Exempt.LIQUID, Exempt.CLIMBABLE, Exempt.FALL_DAMAGE,
                Exempt.ICE, Exempt.SOUL_SAND, Exempt.HONEY, Exempt.CAKE, Exempt.NETHERITE_ARMOR, Exempt.FROZEN, Exempt.CHUNK, Exempt.TRAPDOOR,
                Exempt.DOOR, Exempt.ILLEGAL_BLOCK, Exempt.VEHICLE, Exempt.DOLPHINS_GRACE, Exempt.MYTHIC_MOB, Exempt.PLACED_WEB, Exempt.VOID,
                Exempt.CAULDRON, Exempt.FENCE, Exempt.WEB, Exempt.WATERLOGGED, Exempt.ENDER_PEARL, Exempt.FISHING_ROD, Exempt.RESPAWN,
                Exempt.DRAGON_DAMAGE, Exempt.TELEPORT, Exempt.DEATH, Exempt.WORLD_CHANGE, Exempt.FLIGHT);
        boolean tinyVelocity = data.velocity.velocityX < 0.01 && data.velocity.velocityY < 0.01 && data.velocity.velocityZ < 0.01;
        if (ticks <= 6 && !exempt && data.position.sinceBlockAboveTicks >= 3 && !tinyVelocity) predict(pending, ticks);
        lastMoved = moved;
        moved = p.flying().hasPositionChanged();
        attacks = 0;
    }

    private void predict(boolean pending, int ticks) {
        boolean lastGround = data.position.lastOnGround, lastLastGround = data.position.lastLastOnGround;
        float friction = lastGround ? 0.54600006f : 0.91f;
        float lastFriction = lastLastGround ? 0.54600006f : 0.91f;
        boolean newer = data.clientVersion().isNewerThan(ClientVersion.V_1_9);
        boolean modern = data.clientVersion().isNewerThanOrEquals(ClientVersion.V_1_13);
        double best = Double.MAX_VALUE;
        for (float[] input : INPUTS) {
            for (int sprintIdx = 0; sprintIdx < 2; sprintIdx++) {
                for (int sneakIdx = 0; sneakIdx < 2; sneakIdx++) {
                    for (int hitIdx = 0; hitIdx < 2; hitIdx++) {
                        for (int jumpIdx = 0; jumpIdx < 2; jumpIdx++) {
                            boolean sprint = sprintIdx == 0, sneak = sneakIdx == 0, hit = hitIdx == 0, jump = jumpIdx == 0;
                            float forward = input[0], strafe = input[1];
                            if (sneak) {
                                forward = (float) ((double) forward * 0.2);
                                strafe = (float) ((double) strafe * 0.2);
                            }
                            if (data.action.sneaking) {
                                forward *= 0.3f;
                                strafe *= 0.3f;
                            }
                            forward *= 0.98f;
                            strafe *= 0.98f;
                            double x = pending ? data.velocity.velocityX : data.position.lastDeltaX;
                            double y = pending ? data.velocity.velocityY : data.position.lastDeltaY;
                            double z = pending ? data.velocity.velocityZ : data.position.lastDeltaZ;
                            double speed = speedAttribute();
                            if (sprint) speed += speed * (double) 0.3f;
                            float base = modern ? 0.21600002f : 0.16277136f;
                            double epsilon = newer ? 0.003 : 0.005;
                            float slip = base / (friction * friction * friction);
                            float moveSpeed = lastGround ? (float) speed * slip : (float) (sprint ? 0.025999999418854714 : (double) 0.02f);
                            if (!newer && attacks > 0 && hit) {
                                x *= 0.6;
                                z *= 0.6;
                            }
                            if (!pending) {
                                y -= 0.08;
                                x *= (double) lastFriction;
                                y *= (double) 0.98f;
                                z *= (double) lastFriction;
                            }
                            if (Math.abs(x) < epsilon) x = 0.0;
                            if (Math.abs(y) < epsilon) y = 0.0;
                            if (Math.abs(z) < epsilon) z = 0.0;
                            if (jump && lastGround && sprint) {
                                float rad = data.rotation.yaw * ((float) Math.PI / 180);
                                x -= (double) (McMath.sin(rad) * 0.2f);
                                z += (double) (McMath.cos(rad) * 0.2f);
                            }
                            float len = strafe * strafe + forward * forward;
                            double mx = 0.0, mz = 0.0;
                            if (len > 1.0E-4f) {
                                len = McMath.sqrt(len);
                                if (len < 1.0f) len = 1.0f;
                                len = moveSpeed / len;
                                float s = strafe * len, f = forward * len;
                                float yaw = data.rotation.yaw;
                                float sin = McMath.sin(yaw * (float) Math.PI / 180.0f), cos = McMath.cos(yaw * (float) Math.PI / 180.0f);
                                mx = s * cos - f * sin;
                                mz = f * cos + s * sin;
                            }
                            Location predicted = data.position.lastLocation.clone().add(x + mx, 0.0, z + mz);
                            predicted.setY(0.0);
                            Location actual = data.position.location.clone();
                            actual.setY(0.0);
                            best = Math.min(best, actual.distanceSquared(predicted));
                        }
                    }
                }
            }
        }
        if (best > 10.0) return;
        double threshold = 5.0E-6 + (moved && lastMoved ? 0.0 : 0.001);
        if (newer) threshold += 0.001;
        if (best > threshold) {
            if (increaseBuffer() > maxBuffer()) fail("offset=" + best + " threshold=" + threshold + " tick=" + ticks);
        } else {
            decreaseBuffer();
        }
    }

    private double speedAttribute() {
        double d = data.action.movementSpeedAttribute;
        d += (double) data.action.speedAmplifier * 0.2 * d;
        return (float) d;
    }
}
