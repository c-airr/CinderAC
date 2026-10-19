package pl.airr.cinderac.check.combat.killaura;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import org.bukkit.entity.Player;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Kill Aura", type = 'L', complexType = "Strafe", description = "Impossible strafe.", experimental = true)
public final class KillAuraL extends Check {
    public KillAuraL(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || hitTicks() >= 3 || data.player == null) return;
        double dxz = data.position.deltaXZ, accel = data.position.accelXZ();
        // speed limit: always a constant in the original (walk speed is constant)
        float limit = 0.275f;
        long clickDelay = data.clicks.delay;
        boolean targetPlayer = data.combat.lastAttacked instanceof Player;
        boolean oldClient = data.clientVersion().isOlderThan(ClientVersion.V_1_9);
        boolean soulSand = exempt(Exempt.SOUL_SAND);
        boolean combo = data.player.getMaximumNoDamageTicks() < 18
                || targetPlayer && ((Player) data.combat.lastAttacked).getMaximumNoDamageTicks() < 18;
        boolean invalid = accel < 0.0025 && !data.action.sprinting && dxz > limit && clickDelay < 500L && targetPlayer && oldClient;
        if (invalid && !soulSand && !combo) {
            if (increaseBuffer() > maxBuffer()) fail("speed=" + dxz + " accel=" + accel + " delay=" + clickDelay);
        } else {
            decreaseBuffer();
        }
    }
}
