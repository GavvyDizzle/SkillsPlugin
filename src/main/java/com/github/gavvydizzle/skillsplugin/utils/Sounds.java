package com.github.gavvydizzle.skillsplugin.utils;

import com.github.gavvydizzle.skillsplugin.configs.SoundsConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Sounds {

    public static Sounds regenerateSound, spinCycleExplosionSound;
    public static Sounds skillLevelUpSound;

    static {
        regenerateSound = new Sounds(Sound.BLOCK_ANVIL_FALL, 1, 2);
        spinCycleExplosionSound = new Sounds(Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1f);

        skillLevelUpSound = new Sounds(Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    public static void reload() {
        FileConfiguration config = SoundsConfig.get();
        config.options().copyDefaults(true);
        addDefault(config, "mining.regenerate", regenerateSound);
        addDefault(config, "mining.spinCycleExplode", spinCycleExplosionSound);

        addDefault(config, "general.skillLevelUp", skillLevelUpSound);

        SoundsConfig.save();

        regenerateSound = getSound(config, "mining.regenerate");
        spinCycleExplosionSound = getSound(config, "mining.spinCycleExplode");

        skillLevelUpSound = getSound(config, "general.skillLevelUp");
    }

    private static void addDefault(FileConfiguration config, String root, Sounds sound) {
        config.addDefault(root + ".sound", sound.sound.toString().toUpperCase());
        config.addDefault(root + ".volume", sound.volume);
        config.addDefault(root + ".pitch", sound.pitch);
    }

    private static Sounds getSound(FileConfiguration config, String root) {
        try {
            return new Sounds(Sound.valueOf(Objects.requireNonNull(config.getString(root + ".sound")).toUpperCase()),
                    (float) config.getDouble(root + ".volume"),
                    (float) config.getDouble(root + ".pitch"));
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to load the sound: " + root + ". It has been set to an muted sound so it does not cause errors.");
            return new Sounds(Sound.BLOCK_NOTE_BLOCK_CHIME, 0, 1);
        }
    }


    private final Sound sound;
    private final float volume;
    private final float pitch;

    public Sounds(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    /**
     * Plays the sound at the location for all players to hear.
     * @param loc The location to play the sound.
     */
    public void playSound(Location loc) {
        if (loc.getWorld() != null) loc.getWorld().playSound(loc, sound, volume, pitch);
    }

    /**
     * Plays the sound for only the player to hear.
     * @param p The player to play the sound for.
     */
    public void playSound(Player p) {
        p.playSound(p.getLocation(), sound, volume, pitch);
    }
}

