package pl.airr.cinderac.check.combat.killaura;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Kill Aura", type = 'A', complexType = "Post", description = "Post UseEntity packets.")
public final class KillAuraA extends Check {
    public KillAuraA(PlayerData data) {
        super(data);
    }

    private long lastFlying;
    private boolean sent;

    @Override
    public void handle(Packet p) {
        if (p.isFlying()) {
            long delay = System.currentTimeMillis() - lastFlying;
            if (sent) {
                if (delay > 40L && delay < 100L && !exempt(Exempt.SPECTATOR)) {
                    if (increaseBuffer() > maxBuffer()) fail("delay=" + delay);
                } else {
                    decreaseBuffer();
                }
                sent = false;
            }
            lastFlying = System.currentTimeMillis();
        } else if (p.isAttack() && p.interact().getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK && System.currentTimeMillis() - lastFlying < 5L) {
            sent = true;
        }
    }
}
