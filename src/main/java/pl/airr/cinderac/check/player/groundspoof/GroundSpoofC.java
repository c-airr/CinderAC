package pl.airr.cinderac.check.player.groundspoof;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Ground Spoof", type = 'C', complexType = "Spoof", description = "Spoofed OnGround value.")
public final class GroundSpoofC extends Check {
    public GroundSpoofC(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isAtOrigin()) return;
        int air = data.position.airTicks;
        double dy = data.position.deltaY;
        boolean teleport = data.action.sinceServerPositionTicks < 40;
        boolean exempt = exempt(Exempt.TELEPORT, Exempt.ANVIL, Exempt.LIQUID, Exempt.WATERLOGGED, Exempt.RIPTIDE, Exempt.ELYTRA, Exempt.GLIDING,
                Exempt.JOINED, Exempt.CLIMBABLE, Exempt.LILY_PAD, Exempt.SNOW, Exempt.WORLD_CHANGE, Exempt.VELOCITY, Exempt.WEB,
                Exempt.NEAR_GROUND, Exempt.SLOW_FALLING, Exempt.LEVITATION, Exempt.SHULKER, Exempt.SHULKER_BOX, Exempt.HONEY, Exempt.SLIME,
                Exempt.FLIGHT, Exempt.VOID, Exempt.CANCELLED_PLACE, Exempt.PLACED_SLIME, Exempt.BLOCK_PLACE);
        if (dy < -0.5 && air < 5 && !exempt && !teleport) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy + " airTicks=" + air);
        } else {
            decreaseBuffer();
        }
    }
}
