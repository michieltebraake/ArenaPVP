package me.naithantu.ArenaPVP.Arena.ArenaExtras;

import org.bukkit.Bukkit;

import me.naithantu.ArenaPVP.Arena.Arena;
import me.naithantu.ArenaPVP.Arena.ArenaPlayer;
import me.naithantu.ArenaPVP.Arena.ArenaTeam;
import me.naithantu.ArenaPVP.Util.Util;

public class ArenaUtil {
	Arena arena;

	public ArenaUtil(Arena arena) {
		this.arena = arena;
	}

	public void sendMessageAll(String message) {
		for (ArenaTeam team : arena.getTeams())
			sendMessageTeam(message, team);
	}

	public void sendMessageTeam(String message, ArenaTeam team) {
		for (ArenaPlayer arenaPlayer : team.getPlayers())
			Util.msg(Bukkit.getPlayer(arenaPlayer.getPlayerName()), message);
	}

	public void sendNoPrefixMessageAll(String message) {
		for (ArenaTeam team : arena.getTeams())
			sendNoPrefixMessageTeam(message, team);
	}

	public void sendNoPrefixMessageTeam(String message, ArenaTeam team) {
		for (ArenaPlayer arenaPlayer : team.getPlayers())
			Bukkit.getPlayer(arenaPlayer.getPlayerName()).sendMessage(message);
	}
}