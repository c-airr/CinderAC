package pl.airr.cinderac.check.player.badpackets;

import pl.airr.cinderac.platform.ServerVersion;
import pl.airr.cinderac.check.CheckInfo;
import pl.airr.cinderac.check.Packet;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

@CheckInfo(name = "Bad Packets", type = 'M', complexType = "Post BlockPlace", description = "Post BlockPlace packets.")
public final class BadPacketsM extends PostPacketCheck {
    public BadPacketsM(PlayerData data) {
        super(data);
    }


    @Override
    boolean isTarget(Packet p) {
        return p.isBlockPlace();
    }

    @Override
    boolean exempt() {
        if (exempt(Exempt.CREATIVE, Exempt.SPECTATOR, Exempt.WEB, Exempt.LIQUID, Exempt.EMPTIED_BUCKET) || data.player == null) return true;
        boolean bucket = data.action.sinceBucketUseTicks < 50 || data.player.getItemInHand().getType().name().contains("BUCKET")
                || ServerVersion.atLeast(9) && data.player.getInventory().getItemInOffHand().getType().name().contains("BUCKET");
        return bucket;
    }
}
