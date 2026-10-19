package pl.airr.cinderac.check.player.badpackets;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'K', complexType = "Sneak", description = "Sending EntityAction packets too quickly")
public final class BadPacketsK extends Check {
    public BadPacketsK(PlayerData data) {
        super(data);
    }


    private long lastSneak;

    @Override
    public void handle(Packet p) {
        if (!p.isEntityAction() || p.entityAction().getAction() != WrapperPlayClientEntityAction.Action.START_SNEAKING) return;
        long now = System.currentTimeMillis();
        long delay = now - lastSneak;
        if (delay < 90L && !exempt(Exempt.FAST)) {
            if (increaseBuffer() > maxBuffer()) fail("delay=" + delay);
        } else {
            decreaseBuffer();
        }
        lastSneak = now;
    }
}
