package pl.airr.cinderac.check.combat.killaura;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import org.bukkit.entity.Player;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Kill Aura", type = 'B', complexType = "Acceleration", description = "Invalid acceleration.")
public final class KillAuraB extends Check {
    public KillAuraB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || hitTicks() >= 3 || data.player == null) return;
        double dxz = data.position.deltaXZ, accel = data.position.accelXZ();
        // speed limit: always a constant in the original (walk speed is constant)
        float limit = 0.21f;
        long clickDelay = data.clicks.delay;
        boolean targetPlayer = data.combat.lastAttacked instanceof Player;
        boolean oldClient = data.clientVersion().isOlderThan(ClientVersion.V_1_9);
        boolean soulSand = exempt(Exempt.SOUL_SAND);

        boolean invalid = accel < 0.0025 && data.action.sprinting && dxz > limit && clickDelay < 500L && targetPlayer && oldClient;
        if (invalid && !soulSand) {
            if (increaseBuffer() > maxBuffer()) fail("speed=" + dxz + " accel=" + accel + " delay=" + clickDelay);
        } else {
            decreaseBuffer();
        }
    }
}
