package pl.airr.cinderac.data;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import pl.airr.cinderac.util.VMath;

import java.util.LinkedList;

/**
 * Knockback tracked with transactions: a velocity is "pending" from the confirmation of the transaction sent before it
 * until the first position packet after the transaction sent right after it is confirmed.
 */
public final class VelocityTracker {
    private final PlayerData data;

    public final LinkedList<Vector3d> pendingVelocities = new LinkedList<>();
    public final LinkedList<Vector3d> pendingExplosions = new LinkedList<>();
    public boolean hasPendingVelocity, hasPendingExplosion;
    public double maxVelocityXZ, maxVelocityY, velocityX, velocityY, velocityZ, velocityXZ;
    public double explosionX, explosionY, explosionZ, explosionXZ, explosionYSum, explosionXZSum, lastExplosionY, lastExplosionXZ;
    public double appliedVelocityY;
    public int sinceVelocityTicks, sinceExplosionTicks, sinceVelocityAppliedTicks, sinceHighVelocityTicks = 1000;

    public VelocityTracker(PlayerData data) {
        this.data = data;
    }

    public void handleVelocity(Vector3d velocity, Runnable sendTransactionAfter) {
        data.transactions.onLastTransactionConfirm(() -> {
            appliedVelocityY = velocity.getY();
            sinceVelocityAppliedTicks = 0;
            pendingVelocities.add(velocity);
        });
        data.transactions.onNextTransactionConfirm(() -> data.position.runAfterPosition(pendingVelocities::poll));
        sendTransactionAfter.run();
    }

    /** Explosion: playerMotion (vector for the player) and knockback; skipped when the motion is zero. */
    public void handleExplosion(Vector3f playerMotion, Vector3d knockback, Runnable sendTransactionAfter) {
        if (playerMotion == null || (playerMotion.getX() == 0.0f && playerMotion.getY() == 0.0f && playerMotion.getZ() == 0.0f)) return;
        if (knockback == null) return;
        data.transactions.onLastTransactionConfirm(() -> pendingExplosions.add(knockback));
        data.transactions.onNextTransactionConfirm(() -> data.position.runAfterPosition(pendingExplosions::poll));
        sendTransactionAfter.run();
    }

    public void handleFlying(WrapperPlayClientPlayerFlying f) {
        hasPendingVelocity = !pendingVelocities.isEmpty();
        hasPendingExplosion = !pendingExplosions.isEmpty();
        for (Vector3d v : pendingVelocities) {
            velocityX = v.getX();
            velocityY = v.getY();
            velocityZ = v.getZ();
            velocityXZ = Math.hypot(velocityX, velocityZ);
            maxVelocityY = Math.max(maxVelocityY, v.getY());
            maxVelocityXZ = Math.max(maxVelocityXZ, VMath.hypot(v.getX(), v.getZ()));
            if (maxVelocityXZ > 0.75) sinceHighVelocityTicks = 0;
        }
        explosionYSum = 0.0;
        explosionXZSum = 0.0;
        for (Vector3d v : pendingExplosions) {
            explosionX = v.getX();
            explosionY = v.getY();
            explosionZ = v.getZ();
            explosionXZ = Math.hypot(explosionX, explosionZ);
            explosionYSum += explosionY;
            explosionXZSum += explosionXZ;
        }
        if (explosionYSum > 0.0) lastExplosionY = explosionYSum;
        if (explosionXZSum > 0.0) lastExplosionXZ = explosionXZSum;
        if (f.isOnGround()) lastExplosionY = 0.0;
        sinceVelocityTicks = hasPendingVelocity ? 0 : sinceVelocityTicks + 1;
        sinceExplosionTicks = hasPendingExplosion ? 0 : sinceExplosionTicks + 1;
        ++sinceVelocityAppliedTicks;
        ++sinceHighVelocityTicks;
    }
}
