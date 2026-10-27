package dev.chunky.plugin;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChunkyPlugin extends JavaPlugin {
    private final Set<UUID> god = ConcurrentHashMap.newKeySet();
    private final Set<UUID> vanished = ConcurrentHashMap.newKeySet();
    private Access access;
    private InvseeManager invsee;
    private NamespacedKey stickKey;
    private NamespacedKey missileKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        stickKey = new NamespacedKey(this, "lightning_stick");
        missileKey = new NamespacedKey(this, "missile");

        access = new Access(this);
        access.reload();

        invsee = new InvseeManager(this);
        invsee.start();
        Bukkit.getPluginManager().registerEvents(invsee, this);
        Bukkit.getPluginManager().registerEvents(new Listeners(this), this);

        new Commands(this).registerAll();

        if (!Bukkit.getOnlineMode()) {
            getLogger().warning("Server läuft mit online-mode=false: Spieler-UUIDs sind Offline-UUIDs (abgeleitet vom Namen)."
                    + " Die Owner-UUID in config.yml muss die Offline-UUID sein – /chunky uuid zeigt sie an.");
        }
        removeLeftoverMissiles();
        getLogger().info("Chunky aktiviert.");
    }

    @Override
    public void onDisable() {
        if (invsee != null) {
            invsee.closeAll();
        }
        for (UUID id : Set.copyOf(vanished)) {
            Player v = Bukkit.getPlayer(id);
            if (v != null) {
                for (Player o : Bukkit.getOnlinePlayers()) {
                    o.showPlayer(this, v);
                }
            }
        }
        vanished.clear();
        removeLeftoverMissiles();
    }

    private void removeLeftoverMissiles() {
        for (World w : Bukkit.getWorlds()) {
            for (Creeper c : w.getEntitiesByClass(Creeper.class)) {
                if (c.getPersistentDataContainer().has(missileKey, PersistentDataType.BYTE)) {
                    c.remove();
                }
            }
        }
    }

    Access access() {
        return access;
    }

    InvseeManager invsee() {
        return invsee;
    }

    Set<UUID> god() {
        return god;
    }

    Set<UUID> vanished() {
        return vanished;
    }

    NamespacedKey stickKey() {
        return stickKey;
    }

    NamespacedKey missileKey() {
        return missileKey;
    }

    void reloadAll() {
        reloadConfig();
        access.reload();
    }
}
