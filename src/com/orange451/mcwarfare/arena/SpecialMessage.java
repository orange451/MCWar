package com.orange451.mcwarfare.arena;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SpecialMessage
{
	protected KitPlayer player;
	protected int ticks;
	protected int maxTicks;
	public String[] lines;

	public SpecialMessage()
	{
		this.lines = new String[5];
		for (int i = 0; i < this.lines.length; i++) {
			this.lines[i] = "";
		}
		this.maxTicks = 6;
	}

	public int getTicks() {
		return this.ticks;
	}

	public KitPlayer getPlayer() {
		return this.player;
	}

	public void clear() {
		this.player = null;
		this.ticks = 0;
	}

	public void setPlayer(KitPlayer player) {
		this.player = player;
	}

	public void draw() {
		this.ticks += 1;
		getPlayer().player.sendMessage(ChatColor.GRAY + "║ " + this.lines[0]);
		getPlayer().player.sendMessage(ChatColor.GRAY + "║ " + this.lines[1]);
		getPlayer().player.sendMessage(ChatColor.GRAY + "║ " + this.lines[2]);
		getPlayer().player.sendMessage(ChatColor.GRAY + "║ " + this.lines[3]);
		getPlayer().player.sendMessage(ChatColor.GRAY + "║ " + this.lines[4]);
		getPlayer().player.sendMessage(ChatColor.GRAY + "╚═══════════════════════════════════════════════════════════════");
	}
}