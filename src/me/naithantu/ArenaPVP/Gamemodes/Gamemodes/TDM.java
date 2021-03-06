package me.naithantu.ArenaPVP.Gamemodes.Gamemodes;

import me.naithantu.ArenaPVP.Arena.Arena;
import me.naithantu.ArenaPVP.Arena.ArenaExtras.ArenaSpawns;
import me.naithantu.ArenaPVP.Arena.ArenaExtras.ArenaUtil;
import me.naithantu.ArenaPVP.Arena.ArenaPlayer;
import me.naithantu.ArenaPVP.Arena.ArenaTeam;
import me.naithantu.ArenaPVP.Arena.Settings.ArenaSettings;
import me.naithantu.ArenaPVP.ArenaManager;
import me.naithantu.ArenaPVP.Gamemodes.Gamemode;
import me.naithantu.ArenaPVP.Storage.YamlStorage;
import me.naithantu.ArenaPVP.TabController;
import me.naithantu.ArenaPVP.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.mcsg.double0negative.tabapi.TabAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TDM extends Gamemode {
	
	private Comparator<ArenaTeam> teamComp;
	private Comparator<ArenaPlayer> playerComp;
	
	
	public TDM(Arena arena, ArenaSettings settings, ArenaSpawns arenaSpawns, ArenaUtil arenaUtil, YamlStorage arenaStorage, TabController tabController) {
		super(arena, settings, arenaSpawns, arenaUtil, arenaStorage, tabController, Gamemodes.TDM);
	}
	
	@Override
	public String getName(){
		return "TDM";
	}
	
	@Override
	public boolean isTeamGame() {
		return true;
	}
	
	@Override
	public void onPlayerDeath(PlayerDeathEvent event, ArenaPlayer arenaPlayer){
		super.onPlayerDeath(event, arenaPlayer);
		Player killer = event.getEntity().getKiller();
		if(killer != null){
			ArenaTeam team = ArenaManager.getPlayerByName(killer.getName()).getTeam();
			team.addScore();
			if(team.getScore() >= settings.getScoreLimit()){
				arena.getArenaGameController().stopGame(team);
			} else {
				sortLists();
				updateTabs();
			}
		}
	}

	@Override
	public void updateTabs() {
		if (!tabController.hasTabAPI()) return;
		
		String status = Util.capitalizeFirstLetter(arena.getArenaState().toString());
		String arenaName = arena.getArenaName();
		String spectators = ChatColor.GRAY + "" + arena.getArenaSpectators().getSpectators().size() + " Spectators";
		
		List<ArenaPlayer> players = new ArrayList<>();
		List<ArenaTeam> teams = arena.getTeams();
		String[] teamTab = new String[teams.size() * 3];
		
		List<String> kills = new ArrayList<>();
		int rank = 1; int x = 0;
		for (ArenaTeam team : arena.getTeams()) {
			players.addAll(team.getPlayers());
			teamTab[x] = ChatColor.GRAY + "Rank " + rank + " ->"; rank++; x++;
			teamTab[x] = team.getColoredName(); x++;
			String killString = ChatColor.GREEN + "" + team.getScore() + " Kills";
			while (kills.contains(killString)) {
				killString = killString + " ";
			}
			kills.add(killString);
			teamTab[x] = killString; x++;
		}
		
		Collections.sort(players, playerComp);
		String nrOfPlayers = players.size() + " (" + teams.size() + " teams)";
		
		String[] playerTab = new String[players.size() * 3];
		
		x = 0;
		for (ArenaPlayer player : players) {
			if (x == 0) playerTab[x] = ChatColor.GRAY + "MVP  ->";
			x++;
			playerTab[x] = player.getTeam().getTeamColor() + player.getPlayerName(); x++;
			String killString = ChatColor.RED + "" + player.getPlayerScore().getKills() + " Kills";
			while (kills.contains(killString)) {
				killString = killString + " ";
			}
			kills.add(killString);
			playerTab[x] = killString; x++;
			if (rank > 5) break;
		}
		
		rank = 1;
		for (ArenaTeam team : teams) {
			String teamName = team.getColoredName() + " ";
			String ranking = ChatColor.GREEN + "Rank " + rank; rank++;
			for (ArenaPlayer aP : team.getPlayers()) {
				Player p = Bukkit.getPlayerExact(aP.getPlayerName());
				if (p != null) {
					setTabPlayer(p, status, arenaName, nrOfPlayers, spectators, false, teamName, ranking, teamTab, playerTab);
				}
			}
		}
		
		for (Player p : arena.getArenaSpectators().getSpectators().keySet()) {
			setTabPlayer(p, status, arenaName, nrOfPlayers, spectators, true, null, null, teamTab, playerTab);
		}
		
		TabAPI.updateAll();
	}
	
	private void setTabPlayer(Player p, String status, String arena, String players, String spectators, boolean spectator, String team, String rank, String[] teamTab, String[] playerTab) {
		int row = tabController.setTopTab(p, Gamemodes.TDM);
		TabAPI.setTabString(plugin, p, 2, 1, status);
		TabAPI.setTabString(plugin, p, 3, 1, arena);
		TabAPI.setTabString(plugin, p, 4, 1, players);
		TabAPI.setTabString(plugin, p, 4, 2, spectators);
		if (spectator) {
			TabAPI.setTabString(plugin, p, 5, 1, ChatColor.GRAY + "Spectators");
		} else {
			TabAPI.setTabString(plugin, p, 5, 1, team);
			TabAPI.setTabString(plugin, p, 5, 2, rank);
		}
		
		int colom = 0;
		for (String cell : teamTab) {
			if (cell != null) {
				TabAPI.setTabString(plugin, p, row, colom, cell);
			}
			colom++;
			if (colom == 3) {
				row++;
				if (row > 19) return;
				colom = 0;
			}
		}
		
		row++;
		if (row < 18) {
			TabAPI.setTabString(plugin, p, row, 1, ChatColor.GOLD + "-- Players --"); row++;
			colom = 0;
			for (String cell : playerTab) {
				if (cell != null) {
					TabAPI.setTabString(plugin, p, row, colom, cell);
				}
				colom++;
				if (colom == 3) {
					row++;
					if (row > 19) return;
					colom = 0;
				}
			}
		}
	}

	@Override
	public void sortLists() {
		Collections.sort(arena.getTeams(), teamComp);		
	}

	@Override
	protected void createComp() {
		teamComp = new Comparator<ArenaTeam>() {
			@Override
			public int compare(ArenaTeam o1, ArenaTeam o2) {
				if (o1.getScore() < o2.getScore()) return 1;
				if (o1.getScore() > o2.getScore()) return -1;
				return 0;
			}
		};
		playerComp = new Comparator<ArenaPlayer>() {
			@Override
			public int compare(ArenaPlayer o1, ArenaPlayer o2) {
				int o1Kills = o1.getPlayerScore().getKills();
				int o2Kills = o2.getPlayerScore().getKills();
				if (o1Kills < o2Kills) return 1;
				if (o1Kills > o2Kills) return -1;
				return 0;
			}
		};
	}
}
