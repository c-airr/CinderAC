package pl.airr.cinderac.check.player.groundspoof;

import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Ground Spoof", type = 'A', complexType = "Spoof", description = "Spoofed onGround value.")
public final class GroundSpoofA extends Check {
    public GroundSpoofA(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isFlying() || isServerPositionTick() || isAtOrigin()) return;
        boolean ground = data.position.onGround;
        boolean air = data.position.allAir;
        double dy = data.position.deltaY;
        if (Math.abs(dy) - 0.3116 <= 0.005) return;
        boolean teleport = data.action.sinceServerPositionTicks < 20;
        int ticks = data.position.groundTicks;
        boolean exempt = exempt(Exempt.JOINED, Exempt.STAIRS, Exempt.CHUNK, Exempt.SHULKER, Exempt.RIPTIDE, Exempt.LIQUID, Exempt.TELEPORT,
                Exempt.VOID, Exempt.WORLD_CHANGE, Exempt.SCAFFOLDING, Exempt.SWIMMING, Exempt.BOAT, Exempt.GLIDING, Exempt.WATERLOGGED,
                Exempt.HONEY, Exempt.SLOW_FALLING, Exempt.BLOCK_PLACE, Exempt.SNOW, Exempt.WEB, Exempt.FLIGHT, Exempt.SLIME,
                Exempt.PLACED_SLIME, Exempt.HAPPY_GHAST, Exempt.SHULKER_BOX, Exempt.NEAR_GROUND, Exempt.CANCELLED_PLACE, Exempt.LEVITATION);
        if (air && ground && dy < -0.20000000298023224 && !exempt && !teleport) {
            if (increaseBuffer() > maxBuffer()) fail("deltaY=" + dy + " ticks=" + ticks);
        } else {
            decreaseBuffer();
        }
    }
}
