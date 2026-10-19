package pl.airr.cinderac.check.movement.step;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Step", type = 'A', complexType = "Vanilla", description = "Invalid Step height.")
public final class StepA extends Check {
    public StepA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick()) return;
        double dy = data.position.deltaY;
        boolean exempt = exempt(Exempt.JOINED, Exempt.TELEPORT, Exempt.SHULKER_BOX, Exempt.SPECTATOR, Exempt.WORLD_CHANGE, Exempt.RIPTIDE,
                Exempt.BOAT, Exempt.VEHICLE, Exempt.PISTON, Exempt.SHULKER, Exempt.ENDER_PEARL, Exempt.SLEEPING, Exempt.BED, Exempt.BLOCK_BREAK);
        double step = data.clientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2) ? data.action.stepHeightAttribute : 0.6f;
        if (data.position.onGround && dy > step && !exempt) fail("deltaY=" + dy);
    }
}
