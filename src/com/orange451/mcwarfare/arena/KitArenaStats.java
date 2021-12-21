package com.orange451.mcwarfare.arena;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KitArenaStats extends SpecialMessage
{
	private int kills;
	private int deaths;
	private int teamkills;
	private String winner;
	private String tk;
	private String mapname;
	private String KDR;
	private String creditsearned;

	public KitArenaStats(KitPlayer pl, KitArena last)
	{
		this.kills = pl.kills;
		this.deaths = pl.deaths;
		this.player = pl;
		this.mapname = last.name;
		this.KDR = Double.toString(pl.getKDR());
		this.winner = last.getLeader();
		this.creditsearned = Integer.toString(this.player.getCreditsEarned());
		if (last.type.equals("tdm")) {
			this.teamkills = last.getTeamKills(pl.team);
			this.tk = ("teamkills:" + ChatColor.YELLOW + Integer.toString(this.teamkills));
		} else {
			this.tk = "";
		}

		this.maxTicks = 6;
	}

	public void draw()
	{
		this.ticks += 1;

		this.player.player.sendMessage(ChatColor.RESET + "║ " + ChatColor.GOLD + ChatColor.BOLD + "MCWAR STATS: " + ChatColor.RESET + ChatColor.AQUA + this.mapname);
		this.player.player.sendMessage(ChatColor.RESET + "║ ");
		this.player.player.sendMessage(ChatColor.RESET + "║ " + ChatColor.WHITE + "WINNER: " + this.winner + ChatColor.WHITE + "      " + this.tk);
		this.player.player.sendMessage(ChatColor.RESET + "║ " + ChatColor.WHITE + "kills:     " + ChatColor.GREEN + this.kills + "   " + ChatColor.WHITE + "Credits Earned: " + ChatColor.GREEN + this.creditsearned);
		this.player.player.sendMessage(ChatColor.RESET + "║ " + ChatColor.WHITE + "deaths: " + ChatColor.RED + this.deaths + "   " + ChatColor.WHITE + "KDR: " + ChatColor.YELLOW + this.KDR);
		this.player.player.sendMessage(ChatColor.RESET + "╚═══════════════════════════════════════════════════════════════");
	}

	public int getTicks() {
		return this.ticks;
	}

	public KitPlayer getPlayer() {
		return this.player;
	}

	public void clear() {
		super.clear();
		this.kills = 0;
		this.deaths = 0;
		this.teamkills = 0;
		this.winner = null;
		this.tk = null;
		this.mapname = null;
	}
}