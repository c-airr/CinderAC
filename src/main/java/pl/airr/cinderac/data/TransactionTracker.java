package pl.airr.cinderac.data;

import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.config.Settings;
import pl.airr.cinderac.platform.ServerVersion;
import pl.airr.cinderac.util.VMath;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Transactions (Ping/Pong on 1.17+, WindowConfirmation on older): one is sent every tick, the client replies in order.
 * Tasks attached to a transaction run once the client confirms it received everything sent before it.
 */
public final class TransactionTracker {
    private final PlayerData data;

    public final Deque<Pending> pending = new ConcurrentLinkedDeque<>();
    private final Deque<Runnable> nextTasks = new ConcurrentLinkedDeque<>();
    private short idCounter = (short) -32348;
    public short lastSentId = -69;
    /** Id of the transaction sent right after JOIN_GAME - its confirmation enables tracking. */
    public short joinId = -69;
    public long transactionPing, lastFlyingTime = -1L, flyingDelay = -1L, lastPongTime = System.currentTimeMillis();
    public long lastFastFlyingTime = Long.MAX_VALUE;
    public int lastPongId = -42069;

    public TransactionTracker(PlayerData data) {
        this.data = data;
    }

    /** Task run when the last already-sent transaction is confirmed (or immediately when nothing is pending). */
    public void onLastTransactionConfirm(Runnable r) {
        Pending last = pending.peekLast();
        if (last == null) {
            r.run();
            return;
        }
        last.tasks.add(r);
    }

    /** Task run when the next sent transaction is confirmed. */
    public void onNextTransactionConfirm(Runnable r) {
        nextTasks.add(r);
    }

    public void onSent(int id) {
        if (!data.joinConfirmed) return;
        Pending p = new Pending(id, System.currentTimeMillis());
        if (!nextTasks.isEmpty()) {
            p.tasks.addAll(nextTasks);
            nextTasks.clear();
        }
        pending.add(p);
    }

    public void onPong(int id) {
        if (data.kicked || !data.joinConfirmed) return;
        Settings s = Settings.get();
        Pending p = pending.poll();
        if (p == null) {
            String id0 = Integer.toString(id);
            CinderAC.get().alerts().staff(s.invalidStaffMessage.replace("%id%", id0).replace("%player%", data.name));
            CinderAC.get().getLogger().info(pl.airr.cinderac.util.Text.strip(s.invalidConsoleMessage.replace("%player%", data.name).replace("%id%", id0)));
            data.kick(s.invalidKickMessage);
            return;
        }
        if (id != p.id && (!s.otherAnticheatSupport || System.currentTimeMillis() - data.joinTime >= 30000L)) {
            String a = Integer.toString(id), b = Integer.toString(p.id);
            CinderAC.get().alerts().staff(s.outOfOrderStaffMessage.replace("%id%", a).replace("%last-id%", b).replace("%player%", data.name));
            CinderAC.get().getLogger().info(pl.airr.cinderac.util.Text.strip(s.outOfOrderConsoleMessage.replace("%id%", a).replace("%last-id%", b).replace("%player%", data.name)));
            data.kick(s.outOfOrderKickMessage);
            return;
        }
        lastPongId = id;
        transactionPing = System.currentTimeMillis() - p.time;
        lastPongTime = System.currentTimeMillis();
        for (Runnable r : p.tasks) r.run();
    }

    public void handleFlying() {
        Settings s = Settings.get();
        long now = System.currentTimeMillis();
        long sincePong = now - lastPongTime;
        if (sincePong > s.noResponseDelaySeconds * 1000L && now - data.joinTime > 30000L && !data.kicked) {
            PositionTracker pos = data.position;
            boolean moved = VMath.magnitude(pos.x - pos.joinX, pos.y - pos.joinY, pos.z - pos.joinZ) >= 5.0;
            if (!s.codeOfConductFix || moved) {
                String d = Long.toString(sincePong);
                CinderAC.get().alerts().staff(s.noResponseStaffMessage.replace("%delay%", d).replace("%player%", data.name));
                CinderAC.get().getLogger().info(pl.airr.cinderac.util.Text.strip(s.noResponseConsoleMessage.replace("%delay%", d).replace("%player%", data.name)));
                data.kick(s.noResponseKickMessage);
                return;
            }
        }
        if (pending.size() > s.maxPendingTransactions && !data.kicked && now - data.joinTime > 30000L && pending.size() > 2000) {
            String size = Integer.toString(pending.size());
            CinderAC.get().alerts().staff(s.maxSizeStaffMessage.replace("%size%", size).replace("%player%", data.name));
            CinderAC.get().getLogger().info(pl.airr.cinderac.util.Text.strip(s.maxSizeConsoleMessage.replace("%size%", size).replace("%player%", data.name)));
            data.kick(s.maxSizeKickMessage);
        }
        if (lastFlyingTime > 0L) {
            flyingDelay = now - lastFlyingTime;
            if (flyingDelay < 5L) lastFastFlyingTime = now;
        }
        lastFlyingTime = now;
    }

    /** Sends a transaction; inEventLoop = from another thread via the channel event loop (keeps packet order). */
    public void sendConfirmation(boolean inEventLoop) {
        if (!data.joinConfirmed || data.user.getEncoderState() != ConnectionState.PLAY) return;
        Runnable send = () -> {
            short id = nextId();
            sendPing(id);
            lastSentId = id;
        };
        if (inEventLoop) ChannelHelper.runInEventLoop(data.user.getChannel(), send);
        else send.run();
    }

    public void sendPing(int id) {
        PacketWrapper<?> w = ServerVersion.atLeast(17) ? new WrapperPlayServerPing(id) : new WrapperPlayServerWindowConfirmation(0, (short) id, false);
        data.user.sendPacket(w);
    }

    private short nextId() {
        idCounter = (short) (idCounter + 1);
        if (idCounter > -420) idCounter = (short) -32348;
        return idCounter;
    }

    public static final class Pending {
        public final int id;
        public final long time;
        final List<Runnable> tasks = new ArrayList<>();

        Pending(int id, long time) {
            this.id = id;
            this.time = time;
        }
    }
}
