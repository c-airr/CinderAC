package pl.airr.cinderac.check.movement.noslow;

import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "No Slow", type = 'A', complexType = "Packet", description = "Placing while digging")
public final class NoSlowA extends Check {
    public NoSlowA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isDigging()) return;
        boolean release = p.digging().getAction() == DiggingAction.RELEASE_USE_ITEM;
        boolean exempt = exempt(Exempt.CLIENT_VERSION, Exempt.SERVER_VERSION);
        if (data.action.placedThisTick && release && !exempt) {
            if (increaseBuffer() > maxBuffer()) failNoInfo();
        } else {
            decreaseBuffer();
        }
    }
}
