package pl.airr.cinderac.check;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerHeldItemChange;
import org.bukkit.event.Event;
import pl.airr.cinderac.CinderAC;
import pl.airr.cinderac.config.CheckConfig;
import pl.airr.cinderac.data.Exempt;
import pl.airr.cinderac.data.PlayerData;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Base check. Buffer: increaseBuffer() > maxBuffer -> fail, otherwise decreaseBuffer() by decay.
 * fail(): setback (when enabled and vl > min-vl-to-setback), optional hotbar shuffle / random rotation,
 * buffer *= multiple, then VL/alert/log/punishment.
 */
public abstract class Check {
    protected final PlayerData data;
    public final CheckInfo info;
    /** Class name, e.g. "AimA" - key for permissions and lookups. */
    public final String key;
    public final String category;
    /** checks.<category>.<name>.<type> */
    public final String configPath;
    public CheckConfig cfg;
    private int vl;
    private double buffer;

    protected Check(PlayerData data) {
        this.data = data;
        this.info = getClass().getAnnotation(CheckInfo.class);
        this.key = getClass().getSimpleName();
        String pkg = getClass().getName();
        this.category = pkg.contains(".combat.") ? "combat" : pkg.contains(".movement.") ? "movement" : "player";
        this.configPath = "checks." + category + "." + info.name().replace(" ", "").toLowerCase() + "." + Character.toLowerCase(info.type());
        this.cfg = CinderAC.get().checkConfig(configPath);
    }

    public abstract void handle(Packet packet);

    /** Bukkit events: BlockPlace, BlockBreak, PlayerInteract (left click on block), ProjectileLaunch (arrow), PlayerMove (Folia). */
    public void handleBukkit(Event event) {
    }

    // --- buffer ---

    protected double increaseBuffer() {
        buffer = Math.min(10000.0, buffer + 1.0);
        return buffer;
    }

    protected double increaseBufferBy(double amount) {
        buffer = Math.min(10000.0, buffer + amount);
        return buffer;
    }

    protected double decreaseBufferBy(double amount) {
        buffer = Math.max(0.0, buffer - amount);
        return buffer;
    }

    protected void decreaseBuffer() {
        decreaseBufferBy(cfg.bufferDecay);
    }

    protected void resetBuffer() {
        buffer = 0.0;
    }

    protected void multiplyBuffer(double m) {
        buffer *= m;
    }

    public double buffer() {
        return buffer;
    }

    public void setBuffer(double b) {
        buffer = b;
    }

    protected double maxBuffer() {
        return cfg.bufferMax;
    }

    public int vl() {
        return vl;
    }

    public void setVl(int v) {
        vl = v;
    }

    // --- helpers ---

    protected boolean exempt(Exempt... types) {
        return data.exempt(types);
    }

    protected int tick() {
        return CinderAC.get().tick();
    }

    protected int hitTicks() {
        return data.combat.hitTicks;
    }

    protected boolean recentlyAttacked(int ticks) {
        return data.combat.hitTicks < ticks && data.combat.ticksSinceAttack() < ticks * 6;
    }

    protected boolean isServerPositionTick() {
        return data.action.acceptedServerPosition;
    }

    protected boolean isAtOrigin() {
        return data.position.atOrigin;
    }

    protected void debug(Object o) {
        CinderAC.get().debug(data, this, Objects.toString(o));
    }

    // --- flagging ---

    protected void fail(Object info) {
        fail(Objects.toString(info));
    }

    protected void failNoInfo() {
        fail("");
    }

    private void fail(String info) {
        if (data.player == null) return;
        if (cfg.setback && vl > cfg.minVlToSetback
                && !data.player.hasPermission("cinderac.bypass.setback." + key.toLowerCase())) {
            data.position.requestSetback();
        }
        if (cfg.hotbarShuffle && vl >= cfg.shuffleMinVl && vl % cfg.shuffleEvery == 0) {
            data.user.sendPacket(new WrapperPlayServerHeldItemChange(ThreadLocalRandom.current().nextInt(8)));
        }
        if (cfg.randomRotation && vl >= cfg.rotateMinVl && vl % cfg.rotateEvery == 0) data.rotateRandomly();
        multiplyBuffer(cfg.bufferMultiple);
        CinderAC.get().alerts().handleFail(this, data, info);
    }

    public String displayName() {
        return info.name();
    }

    @Override
    public String toString() {
        return info.name() + " " + info.type();
    }
}
