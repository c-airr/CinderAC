package pl.airr.cinderac.check.combat.criticals;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Criticals", type = 'A', complexType = "Ground", description = "Tried to hit Critical hit on ground.")
public final class CriticalsA extends Check {
    public CriticalsA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isAttack() || hitTicks() >= 2) return;
        double dy = Math.abs(data.position.deltaY);
        double mod = data.position.y % 0.015625;
        boolean exempt = exempt(Exempt.LIQUID, Exempt.TRAPDOOR, Exempt.COLLIDING_VERTICALLY, Exempt.TELEPORT, Exempt.BOAT, Exempt.WEB,
                Exempt.ENDER_PEARL, Exempt.CHORUS_FRUIT, Exempt.ELYTRA, Exempt.SERVER_POSITION, Exempt.CARPET, Exempt.VEHICLE,
                Exempt.FARMLAND, Exempt.GLIDING, Exempt.SLAB)
                || data.position.nearLantern || data.position.nearFlowerPot || data.position.nearPath || data.position.nearCactus;
        if (Math.abs(dy - 7.504558659832128E-4) < 0.001) return;
        boolean invalid = dy < 0.001 && dy > 0.0 && mod < 1.0E-5 || dy == 0.0625;
        if (invalid && !exempt) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy + " modulo=" + mod);
        } else {
            decreaseBuffer();
        }
    }
}
