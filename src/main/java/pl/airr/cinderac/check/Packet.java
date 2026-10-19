package pl.airr.cinderac.check;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerAbilities;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSpectate;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSteerVehicle;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseItem;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientVehicleMove;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;

/** A single packet passed to checks; wrappers are created lazily and shared. */
public final class Packet {
    public final ProtocolPacketEvent event;
    public final PacketTypeCommon type;
    public final boolean receive;
    public final long time;
    private Object wrapper;

    public Packet(ProtocolPacketEvent event, long time) {
        this.event = event;
        this.type = event.getPacketType();
        this.receive = event instanceof PacketReceiveEvent;
        this.time = time;
    }

    private PacketReceiveEvent in() {
        return (PacketReceiveEvent) event;
    }

    // --- type detection (same groups as the original) ---

    public boolean isFlying() {
        return receive && (type == PacketType.Play.Client.PLAYER_FLYING || type == PacketType.Play.Client.PLAYER_POSITION
                || type == PacketType.Play.Client.PLAYER_ROTATION || type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION);
    }

    public boolean isPosition() {
        return receive && (type == PacketType.Play.Client.PLAYER_POSITION || type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION);
    }

    public boolean isRotation() {
        return receive && (type == PacketType.Play.Client.PLAYER_ROTATION || type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION);
    }

    /** INTERACT_ENTITY or ATTACK (any action - the check inspects the action). */
    public boolean isAttack() {
        return receive && (type == PacketType.Play.Client.INTERACT_ENTITY || type == PacketType.Play.Client.ATTACK);
    }

    public boolean is(PacketTypeCommon t) {
        return type == t;
    }

    public boolean isBlockPlace() {
        return receive && type == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT;
    }

    public boolean isUseItem() {
        return receive && type == PacketType.Play.Client.USE_ITEM;
    }

    public boolean isAnimation() {
        return receive && type == PacketType.Play.Client.ANIMATION;
    }

    public boolean isVehicleMove() {
        return receive && type == PacketType.Play.Client.VEHICLE_MOVE;
    }

    public boolean isEntityAction() {
        return receive && type == PacketType.Play.Client.ENTITY_ACTION;
    }

    public boolean isDigging() {
        return receive && type == PacketType.Play.Client.PLAYER_DIGGING;
    }

    public boolean isAbilities() {
        return receive && type == PacketType.Play.Client.PLAYER_ABILITIES;
    }

    public boolean isSteerVehicle() {
        return receive && type == PacketType.Play.Client.STEER_VEHICLE;
    }

    public boolean isClientStatus() {
        return receive && type == PacketType.Play.Client.CLIENT_STATUS;
    }

    public boolean isHeldItemChange() {
        return receive && type == PacketType.Play.Client.HELD_ITEM_CHANGE;
    }

    public boolean isKeepAlive() {
        return receive && type == PacketType.Play.Client.KEEP_ALIVE;
    }

    public boolean isClickWindow() {
        return receive && type == PacketType.Play.Client.CLICK_WINDOW;
    }

    public boolean isSpectate() {
        return receive && type == PacketType.Play.Client.SPECTATE;
    }

    public boolean isClientTickEnd() {
        return receive && type == PacketType.Play.Client.CLIENT_TICK_END;
    }

    public boolean isServerTeleport() {
        return !receive && type == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK;
    }

    // --- wrappers ---

    public WrapperPlayClientPlayerFlying flying() {
        if (wrapper == null) wrapper = new WrapperPlayClientPlayerFlying(in());
        return (WrapperPlayClientPlayerFlying) wrapper;
    }

    /** An ATTACK packet is converted to INTERACT_ENTITY with the ATTACK action (as in the original). */
    public WrapperPlayClientInteractEntity interact() {
        if (wrapper == null) {
            if (type == PacketType.Play.Client.ATTACK) {
                WrapperPlayClientAttack a = new WrapperPlayClientAttack(in());
                wrapper = new WrapperPlayClientInteractEntity(a.getEntityId(), WrapperPlayClientInteractEntity.InteractAction.ATTACK,
                        InteractionHand.MAIN_HAND, java.util.Optional.empty(), java.util.Optional.of(false));
            } else {
                wrapper = new WrapperPlayClientInteractEntity(in());
            }
        }
        return (WrapperPlayClientInteractEntity) wrapper;
    }

    public void setInteract(WrapperPlayClientInteractEntity w) {
        wrapper = w;
    }

    public WrapperPlayClientPlayerDigging digging() {
        if (wrapper == null) wrapper = new WrapperPlayClientPlayerDigging(in());
        return (WrapperPlayClientPlayerDigging) wrapper;
    }

    public WrapperPlayClientPlayerBlockPlacement blockPlace() {
        if (wrapper == null) wrapper = new WrapperPlayClientPlayerBlockPlacement(in());
        return (WrapperPlayClientPlayerBlockPlacement) wrapper;
    }

    public WrapperPlayClientUseItem useItem() {
        if (wrapper == null) wrapper = new WrapperPlayClientUseItem(in());
        return (WrapperPlayClientUseItem) wrapper;
    }

    public WrapperPlayClientEntityAction entityAction() {
        if (wrapper == null) wrapper = new WrapperPlayClientEntityAction(in());
        return (WrapperPlayClientEntityAction) wrapper;
    }

    public WrapperPlayClientHeldItemChange heldItemChange() {
        if (wrapper == null) wrapper = new WrapperPlayClientHeldItemChange(in());
        return (WrapperPlayClientHeldItemChange) wrapper;
    }

    public WrapperPlayClientSteerVehicle steerVehicle() {
        if (wrapper == null) wrapper = new WrapperPlayClientSteerVehicle(in());
        return (WrapperPlayClientSteerVehicle) wrapper;
    }

    public WrapperPlayClientPlayerAbilities abilities() {
        if (wrapper == null) wrapper = new WrapperPlayClientPlayerAbilities(in());
        return (WrapperPlayClientPlayerAbilities) wrapper;
    }

    public WrapperPlayClientKeepAlive keepAlive() {
        if (wrapper == null) wrapper = new WrapperPlayClientKeepAlive(in());
        return (WrapperPlayClientKeepAlive) wrapper;
    }

    public WrapperPlayClientClickWindow clickWindow() {
        if (wrapper == null) wrapper = new WrapperPlayClientClickWindow(in());
        return (WrapperPlayClientClickWindow) wrapper;
    }

    public WrapperPlayClientSpectate spectate() {
        if (wrapper == null) wrapper = new WrapperPlayClientSpectate(in());
        return (WrapperPlayClientSpectate) wrapper;
    }

    public WrapperPlayClientVehicleMove vehicleMove() {
        if (wrapper == null) wrapper = new WrapperPlayClientVehicleMove(in());
        return (WrapperPlayClientVehicleMove) wrapper;
    }

    public WrapperPlayClientAnimation animation() {
        if (wrapper == null) wrapper = new WrapperPlayClientAnimation(in());
        return (WrapperPlayClientAnimation) wrapper;
    }

    public WrapperPlayServerPlayerPositionAndLook serverPosition() {
        if (wrapper == null) wrapper = new WrapperPlayServerPlayerPositionAndLook((PacketSendEvent) event);
        return (WrapperPlayServerPlayerPositionAndLook) wrapper;
    }
}
