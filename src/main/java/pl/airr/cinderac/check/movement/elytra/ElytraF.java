package pl.airr.cinderac.check.movement.elytra;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;
import pl.airr.cinderac.platform.ServerVersion;

@CheckInfo(name = "Elytra", type = 'F', complexType = "Packet", description = "Sending Elytra packets too quickly.")
public final class ElytraF extends Check {
    public ElytraF(PlayerData data) {
        super(data);
    }

    private long lastStart;

    @Override
    public void handle(Packet p) {
        if (!p.isEntityAction() || isServerPositionTick() || !ServerVersion.atLeast(9)) return;
        if (p.entityAction().getAction() != WrapperPlayClientEntityAction.Action.START_FLYING_WITH_ELYTRA) return;
        long delay = System.currentTimeMillis() - lastStart;
        if (delay < 110L && !exempt(Exempt.FAST)) {
            if (increaseBuffer() > maxBuffer()) fail("delay=" + delay);
        } else {
            decreaseBuffer();
        }
        lastStart = System.currentTimeMillis();
    }
}
