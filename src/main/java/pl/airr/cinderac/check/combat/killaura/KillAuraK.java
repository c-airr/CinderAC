package pl.airr.cinderac.check.combat.killaura;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CheckInfo(name = "Kill Aura", type = 'K', complexType = "Pattern", description = "Attack pattern.")
public final class KillAuraK extends Check {
    public KillAuraK(PlayerData data) {
        super(data);
    }

    private final List<Long> delays = new ArrayList<>();
    private long lastAttack;

    @Override
    public void handle(Packet p) {
        if (!(p.isAttack() && p.interact().getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) || exempt(Exempt.FAST)) return;
        delays.add(System.currentTimeMillis() - lastAttack);
        if (delays.size() == 10) {
            Collections.sort(delays);
            long range = delays.get(delays.size() - 1) - delays.get(0);
            if (range < 50L) {
                if (increaseBuffer() > maxBuffer()) fail("range=" + range);
            } else {
                decreaseBuffer();
            }
            delays.clear();
        }
        lastAttack = System.currentTimeMillis();
    }
}
