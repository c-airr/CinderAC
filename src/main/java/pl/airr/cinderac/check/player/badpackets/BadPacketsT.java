package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'T', complexType = "Keep Alive", description = "Invalid KeepAlive packets.")
public final class BadPacketsT extends Check {
    public BadPacketsT(PlayerData data) {
        super(data);
    }


    private long lastId = -1L;

    @Override
    public void handle(Packet p) {
        if (!p.isKeepAlive()) return;
        long id = p.keepAlive().getId();
        if (id == lastId && id != 0L && !exempt(Exempt.JOINED)) fail("id=" + id + " lastId=" + lastId);
        lastId = id;
    }
}
