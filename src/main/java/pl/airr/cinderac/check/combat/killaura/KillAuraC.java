package pl.airr.cinderac.check.combat.killaura;

import org.bukkit.entity.Player;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Kill Aura", type = 'C', complexType = "Head Snap", description = "Invalid acceleration.")
public final class KillAuraC extends Check {
    public KillAuraC(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || hitTicks() >= 3) return;
        double accel = data.position.accelXZ(), dxz = data.position.deltaXZ;
        float dy = data.rotation.deltaYaw, dp = data.rotation.deltaPitch;
        boolean invalid = accel < 0.001 && dy > 10.0f && dp > 26.5 && data.combat.lastAttacked instanceof Player && dxz > 0.0;
        if (invalid) {
            if (increaseBuffer() > maxBuffer()) fail("accel=" + accel + " deltaYaw=" + dy);
        } else {
            decreaseBuffer();
        }
    }
}
