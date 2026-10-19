package pl.airr.cinderac.check.combat.killaura;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Kill Aura", type = 'J', complexType = "Frequency", description = "Attack frequency.")
public final class KillAuraJ extends Check {
    public KillAuraJ(PlayerData data) {
        super(data);
    }

    private int ticks, lastTicks, attacks, same;

    @Override
    public void handle(Packet p) {
        if (p.isAttack()) {
            if (p.interact().getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;
            boolean invalid = data.clicks.cps > 7.2 && ticks < 4 && lastTicks < 4;
            if (invalid) {
                if (ticks == lastTicks) ++same;
                if (++attacks == 30) {
                    if (same > 28) {
                        if (increaseBuffer() > maxBuffer()) fail("invalid=" + same);
                    } else {
                        decreaseBuffer();
                    }
                    same = 0;
                    attacks = 0;
                }
            }
            lastTicks = ticks;
            ticks = 0;
        } else if (p.isFlying()) {
            ++ticks;
        }
    }
}
