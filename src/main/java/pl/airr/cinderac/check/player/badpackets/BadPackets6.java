package pl.airr.cinderac.check.player.badpackets;

import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = '6', complexType = "Nuker", description = "Invalid Block Dig packets.")
public final class BadPackets6 extends Check {
    public BadPackets6(PlayerData data) {
        super(data);
    }


    // the counter is only reset (never grows) - as in the original the check never flags in practice
    private int digs;

    @Override
    public void handle(Packet p) {
        if (p.isDigging()) {
            if (p.digging().getAction() == DiggingAction.START_DIGGING) {
                if (digs > 3) {
                    if (increaseBuffer() > maxBuffer()) fail("digs=" + digs);
                } else {
                    decreaseBuffer();
                }
            }
        } else if (p.isAnimation()) {
            digs = 0;
        }
    }
}
