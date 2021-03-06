package me.naithantu.ArenaPVP.Arena.PlayerExtras;

import me.naithantu.ArenaPVP.Arena.Arena;
import me.naithantu.ArenaPVP.Arena.ArenaExtras.ArenaPlayerState;
import me.naithantu.ArenaPVP.Arena.ArenaExtras.ArenaSpawns;
import me.naithantu.ArenaPVP.Arena.ArenaExtras.ArenaSpawns.SpawnType;
import me.naithantu.ArenaPVP.Arena.ArenaPlayer;
import me.naithantu.ArenaPVP.Arena.Runnables.OutOfBoundsTimer;
import me.naithantu.ArenaPVP.Arena.Settings.ArenaSettings;
import me.naithantu.ArenaPVP.ArenaPVP;
import me.naithantu.ArenaPVP.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerTimers {
	ArenaPVP plugin = ArenaPVP.getInstance();
	Arena arena;
	ArenaSettings settings;
	ArenaPlayer arenaPlayer;

	ArenaSpawns arenaSpawns;

	OutOfBoundsTimer outOfBoundsTimer;
	boolean outOfBounds = false;

	boolean spawnProtection;
	int spawnProtectionID;

	//Don't need boolean for respawning, is done via playerstate.
	int respawnTimerID;

    //Contains all other timers that run on a player, should never contain
    List<PlayerTimer> playerTimers = new ArrayList<PlayerTimer>();

	public PlayerTimers(Arena arena, ArenaPlayer arenaPlayer, Player player) {
		this.arena = arena;
		this.arenaPlayer = arenaPlayer;
		this.settings = arena.getSettings();
		arenaSpawns = arena.getArenaSpawns();
		outOfBoundsTimer = new OutOfBoundsTimer(this, player, settings.getOutOfBoundsTime());
	}

	public void cancelAllTimers() {
		if (outOfBounds)
			outOfBoundsTimer.cancel();

		Bukkit.getScheduler().cancelTask(spawnProtectionID);
		spawnProtection = false;

		Bukkit.getScheduler().cancelTask(respawnTimerID);
		if (arenaPlayer.getPlayerState() == ArenaPlayerState.RESPAWNING) {
			arenaPlayer.setPlayerState(ArenaPlayerState.PLAYING);
		}

        for(PlayerTimer playerTimer: playerTimers){
            playerTimer.cancel();
        }
	}

	public boolean isOutOfBounds() {
		return outOfBounds;
	}

	public void killOutOfBounds(Player player) {
		player.setHealth(0);
		outOfBoundsTimer.cancel();
		outOfBounds = false;
		Util.msg(player, "You left the combat area, you have been killed!");
	}

	public void setOutOfBounds(Player player, boolean outOfBounds) {
		this.outOfBounds = outOfBounds;
		if (!outOfBounds) {
			Util.msg(player, "You have returned to the combat area!");
			outOfBoundsTimer.cancel();
		} else {
			outOfBoundsTimer = new OutOfBoundsTimer(this, player, settings.getOutOfBoundsTime());
			outOfBoundsTimer.runTaskTimer(plugin, 0, 20);
		}
	}

	public boolean hasSpawnProtection() {
		return spawnProtection;
	}

	public void giveSpawnProtection() {
		if (settings.getSpawnProtection() > 0) {
			spawnProtection = true;
			spawnProtectionID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					spawnProtection = false;
				}
			}, settings.getSpawnProtection() * 20);
		}
	}

	public void startRespawnTimer(final Player player, final SpawnType spawnType) {
		if (arenaPlayer.getPlayerState() == ArenaPlayerState.RESPAWNING) {
			Bukkit.getScheduler().cancelTask(respawnTimerID);
		}

		arenaPlayer.setPlayerState(ArenaPlayerState.RESPAWNING);
		respawnTimerID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				arenaPlayer.setPlayerState(ArenaPlayerState.PLAYING);
				Location location;
				if ((location = arenaSpawns.getRespawnLocation(player, arenaPlayer, spawnType)) != null) {
					player.teleport(location);
				}
			}
		}, settings.getRespawnTime() * 20);
	}

    public void addTimer(PlayerTimer playerTimer){
        playerTimers.add(playerTimer);
    }

    public void removeTimer(PlayerTimer playerTimer) {
        playerTimers.remove(playerTimer);
    }
}
