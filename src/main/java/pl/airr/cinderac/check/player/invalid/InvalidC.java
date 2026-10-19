package pl.airr.cinderac.check.player.invalid;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Invalid", type = 'C', complexType = "Y", description = "Too large Y-Axis movement.")
public final class InvalidC extends Check {
    public InvalidC(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick() || isAtOrigin()) return;
        double dy = data.position.deltaY;
        if (data.action.levitationAmplifier > 100) return;
        boolean teleport = data.action.sinceTeleportTicks < 80;
        boolean death = data.action.sinceDeathTicks < 40;
        boolean joined = System.currentTimeMillis() - data.joinTime < 10000L;
        boolean exempt = exempt(Exempt.TELEPORT, Exempt.RIPTIDE, Exempt.GLIDING, Exempt.FISHING_ROD, Exempt.VELOCITY, Exempt.JOINED,
                Exempt.FLIGHT, Exempt.CREATIVE, Exempt.VEHICLE, Exempt.WORLD_CHANGE, Exempt.DOLPHINS_GRACE, Exempt.SOUL_SPEED, Exempt.DEATH,
                Exempt.SLEEPING, Exempt.ENDER_PEARL, Exempt.HIGH_JUMP_BOOST, Exempt.HIGH_LEVITATION);
        double limit = 3.921f;
        int sinceExplosion = data.velocity.sinceExplosionTicks;
        if (data.velocity.hasPendingExplosion) {
            limit += 0.1 + data.velocity.explosionYSum;
        } else if (sinceExplosion < 40) {
            limit += data.velocity.lastExplosionY * Math.pow(0.98f, sinceExplosion);
        }
        if (Math.abs(dy) > limit && !exempt && !teleport && !death && !joined) {
            // also instant with dy > 4 on a server teleport tick (as in the original)
            if (increaseBuffer() > maxBuffer() || dy > 4.0 && exempt(Exempt.SERVER_POSITION)) fail("deltaY=" + dy);
        } else {
            decreaseBuffer();
        }
    }
}
