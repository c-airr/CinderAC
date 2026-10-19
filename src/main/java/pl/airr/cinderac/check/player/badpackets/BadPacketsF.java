package pl.airr.cinderac.check.player.badpackets;


import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'F', complexType = "Steer Vehicle", description = "Invalid Steer Vehicle packet.")
public final class BadPacketsF extends Check {
    public BadPacketsF(PlayerData data) {
        super(data);
    }


    @Override
    public void handle(Packet p) {
        if (!p.isSteerVehicle()) return;
        float forward = p.steerVehicle().getForward(), side = p.steerVehicle().getSideways();
        if (Math.abs(forward) > 0.98f || Math.abs(side) > 0.98f) fail("forward=" + forward + " side=" + side);
    }
}
