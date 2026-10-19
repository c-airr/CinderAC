package pl.airr.cinderac.check.player.badpackets;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerAbilities;
import pl.airr.cinderac.platform.ServerVersion;

import java.util.Optional;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'A', complexType = "Abilities", description = "Spoofed abilities packets.")
public final class BadPacketsA extends Check {
    public BadPacketsA(PlayerData data) {
        super(data);
    }


    @Override
    public void handle(Packet p) {
        if (!p.isAbilities() || data.position.atOrigin) return;
        WrapperPlayClientPlayerAbilities w = p.abilities();
        boolean dead = exempt(Exempt.DEATH);
        if (data.player == null || data.player.getAllowFlight() || dead || data.position.serverFlightAllowed) return;
        Optional<Boolean> allowed = w.isFlightAllowed();
        if (ServerVersion.atLeast(9)) {
            if (increaseBuffer() > maxBuffer()) fail("flying=" + w.isFlying());
            else decreaseBuffer();
        } else if (allowed.isPresent() && allowed.get()) {
            if (increaseBuffer() > maxBuffer()) fail("flightAllowed=" + allowed);
            else decreaseBuffer();
        }
    }
}
