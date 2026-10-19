package pl.airr.cinderac.check.combat.criticals;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Criticals", type = 'B', complexType = "Modulo", description = "Tried to hit Critical hit on ground.")
public final class CriticalsB extends Check {
    public CriticalsB(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isAttack() || hitTicks() >= 2) return;
        double mod = data.position.y % 0.015625;
        boolean invalid = mod < 1.0E-5 && mod > 0.0;
        boolean exempt = exempt(Exempt.LIQUID, Exempt.TRAPDOOR, Exempt.COLLIDING_VERTICALLY, Exempt.BOAT, Exempt.TELEPORT, Exempt.WEB,
                Exempt.ENDER_PEARL, Exempt.CHORUS_FRUIT, Exempt.SERVER_POSITION, Exempt.VEHICLE, Exempt.FLIGHT, Exempt.SLAB, Exempt.STAIRS);
        if (invalid && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("modulo=" + mod);
        } else {
            decreaseBuffer();
        }
    }
}
