package pl.airr.cinderac.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientPluginMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import org.bukkit.entity.Player;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.data.PlayerData;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** packetevents packets -> PlayerData (player netty thread). */
public final class PacketListener extends PacketListenerAbstract {
    private final CinderAC plugin;

    public PacketListener(CinderAC plugin) {
        super(PacketListenerPriority.MONITOR);
        this.plugin = plugin;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        long now = System.currentTimeMillis();
        if (event.getPacketType() == PacketType.Configuration.Client.PLUGIN_MESSAGE) {
            WrapperConfigClientPluginMessage w = new WrapperConfigClientPluginMessage(event);
            brand(w.getChannelName(), w.getData(), event.getUser().getUUID());
        }
        if (!(event.getPacketType() instanceof PacketType.Play.Client)) return;
        if (event.getUser() == null || event.getUser().getUUID() == null) return;
        PlayerData data = plugin.players().getOrCreate(event.getUser());
        if (data == null) return;
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            WrapperPlayClientPluginMessage w = new WrapperPlayClientPluginMessage(event);
            brand(w.getChannelName(), w.getData(), event.getUser().getUUID());
        }
        if (data.player == null && event.getPlayer() instanceof Player) data.player = (Player) event.getPlayer();
        data.handleReceive(event, now);
    }

    private void brand(String channel, byte[] bytes, UUID id) {
        if (id == null || bytes == null || bytes.length == 0) return;
        if (!channel.equals("minecraft:brand") && !channel.equals("MC|Brand")) return;
        // the first byte is the string length (varint)
        String brand = new String(bytes, StandardCharsets.UTF_8).substring(1).replaceAll("§.", "");
        plugin.players().putBrand(id, brand);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) return;
        if (event.getUser() == null || event.getUser().getUUID() == null) return;
        if (!(event.getPacketType() instanceof PacketType.Play.Server)) return;
        PlayerData data = plugin.players().getOrCreate(event.getUser());
        if (data == null) return;
        if (data.player == null && event.getPlayer() instanceof Player) data.player = (Player) event.getPlayer();
        data.handleSend(event);
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        if (event.getUser() == null || event.getUser().getUUID() == null) return;
        plugin.players().remove(event.getUser().getUUID());
    }
}
