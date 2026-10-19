package pl.airr.cinderac.check.player.baritone;

import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Baritone", type = 'A', complexType = "Rotations", description = "Checks for Baritone like rotations.")
public final class BaritoneA extends Check {
    public BaritoneA(PlayerData data) {
        super(data);
    }

    private int rotations;

    @Override
    public void handle(Packet p) {
        if (p.isRotation() && ++rotations < 10) {
            float dy = data.rotation.deltaYaw, dp = data.rotation.deltaPitch;
            if ((double) dy < 0.005 && (double) dp < 0.005 && (double) dy > 0.0 && (double) dp > 0.0 && !data.rotation.cinematic) {
                if (increaseBuffer() > maxBuffer()) fail("deltaYaw=" + dy + " deltaPitch=" + dp);
            } else {
                decreaseBuffer();
            }
        } else if (p.isDigging() && p.digging().getAction() == DiggingAction.CANCELLED_DIGGING) {
            rotations = 0;
        }
    }
}
