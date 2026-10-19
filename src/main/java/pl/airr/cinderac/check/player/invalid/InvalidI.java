package pl.airr.cinderac.check.player.invalid;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import pl.airr.cinderac.check.Check;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Invalid", type = 'I', complexType = "Invalid Y movement", description = "Impossible Y change.")
public final class InvalidI extends Check {
    public InvalidI(PlayerData data) {
        super(data);
    }

    @Override
    public void handle(Packet p) {
        if (!p.isPosition() || isServerPositionTick()) return;
        if (data.clientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) return;
        double dy = data.position.deltaY;
        int air = data.position.airTicks;
        boolean damage = data.action.sinceNonFallDamageTicks < 20;
        boolean bed = data.position.sinceBedTicks < 40;
        boolean velocity = data.velocity.sinceVelocityTicks < 100;
        boolean golem = data.action.sinceIronGolemDamageTicks < 30;
        boolean dragon = data.action.sinceDragonDamageTicks < 200;
        boolean fireball = exempt(Exempt.FIREBALL);
        boolean vehicle = exempt(Exempt.VEHICLE);
        boolean exempt = exempt(Exempt.JOINED, Exempt.TELEPORT, Exempt.RIPTIDE, Exempt.HONEY, Exempt.GLIDING, Exempt.LEVITATION, Exempt.SLAB,
                Exempt.STAIRS, Exempt.FLIGHT, Exempt.JUMP_BOOST_RAN_OUT, Exempt.ELYTRA, Exempt.BUBBLE_COLUMN, Exempt.WALL, Exempt.CREATIVE,
                Exempt.SHULKER_BOX, Exempt.ANVIL, Exempt.SLIME, Exempt.PISTON, Exempt.TURTLE_EGG, Exempt.DOOR, Exempt.DEATH,
                Exempt.ENDER_PEARL, Exempt.PLACED_SLIME, Exempt.CHORUS_FRUIT, Exempt.MYTHIC_MOB, Exempt.BLOCK_BREAK, Exempt.FISHING_ROD,
                Exempt.SLEEPING, Exempt.SHULKER);
        double limit = 1.25;
        if (golem) limit += (double) 0.4f;
        if (fireball) limit += 0.5;
        if (exempt(Exempt.BOAT)) limit += 0.125;
        if (exempt(Exempt.SKULL)) limit += 0.1;
        if (data.position.nearPowderSnow) limit += 0.3;
        if (data.action.sinceHoglinDamageTicks < 20) limit += (double) 0.15f;
        if (data.action.hasJumpBoost) limit += (double) ((float) data.action.jumpBoostAmplifier * 0.125f);
        if (exempt(Exempt.FENCE)) limit += 0.25;
        // the original requires the player to have recently been in a vehicle (VEHICLE exempt true)
        if (air > 0 && dy > limit && !damage && !exempt && !bed && !velocity && !dragon && vehicle) {
            if (increaseBuffer() > maxBuffer() || dy > 2.5) fail("deltaY=" + dy + " limit=" + limit);
        } else {
            decreaseBuffer();
        }
    }
}
