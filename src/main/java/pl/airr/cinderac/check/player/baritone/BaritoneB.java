package pl.airr.cinderac.check.player.baritone;

import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Baritone", type = 'B', complexType = "Large Rotation", description = "Checks for Baritone like rotations.")
public final class BaritoneB extends Check {
    public BaritoneB(PlayerData data) {
        super(data);
    }

    private int rotations = 100;

    @Override
    public void handle(Packet p) {
        if (p.isRotation() && ++rotations < 5) {
            float dy = data.rotation.deltaYaw, dp = data.rotation.deltaPitch, pitch = data.rotation.pitch;
            if (dy > 95.0f && (double) dp < 0.15 && Math.abs(pitch) < 70.0f && !exempt(Exempt.PISTON)) {
                if (increaseBuffer() > maxBuffer()) fail("deltaPitch=" + dp + " deltaYaw=" + dy);
            } else {
                decreaseBuffer();
            }
        } else if (p.isDigging() && p.digging().getAction() == DiggingAction.CANCELLED_DIGGING) {
            rotations = 0;
        }
    }
}
