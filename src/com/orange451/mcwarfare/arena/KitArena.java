package com.orange451.mcwarfare.arena;

import com.orange451.mcwarfare.Field;
import com.orange451.mcwarfare.FileIO;
import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.Util;
import com.orange451.mcwarfare.arena.kits.KitClass;
import com.orange451.mcwarfare.arena.kits.Perk;
import com.orange451.pvpgunplus.PVPGunPlus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.EnumClientCommand;
import net.minecraft.server.v1_7_R1.PacketPlayInClientCommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.kitteh.tag.TagAPI;

public class KitArena
{
	public boolean stopped = false;
	public boolean started = false;
	public boolean busy;
	public boolean allowTeamkill = false;
	public boolean test = true;
	public int maxPlayers = 60;
	public int minPlayers = -1;
	public int repeat = 0;
	public int timer = 60;
	public int announcemaptimer = 0;
	public int timermax;
	public int bluekills = 0;
	public int redkills = 0;
	public int redscore = 0;
	public int bluescore = 0;
	public int maxscore = 75;
	public int timeSinceStart;
	public int ticksSinceStart = 0;
	public Random r = new Random();
	public KitPvP plugin;
	public String type = "lobby";
	public String killType = "tdm";
	public String name;
	public String message;
	public Location spawnpoint;
	public Location spawnpoint2;
	public KitArena last;
	public KitArena tomap;
	public Field field;
	public GameModeModifier gameModifier = GameModeModifier.NONE;
	public ArrayList<KitSpawn> spawns = new ArrayList();
	public ArrayList<KitFlagStand> flags = new ArrayList();
	public ArrayList<KitPlayer> players = new ArrayList();
	public int mytimer = -1;

	public ArrayList<String> gunList = new ArrayList();

	public KitArena(KitPvP plugin, String arenaName) {
		this.plugin = plugin;
		this.name = arenaName;
		this.field = new Field();

		loadArena();

		this.timermax = 240;
		if (this.type.equals("lobby")) {
			this.started = true;
			plugin.currentMap = this;
			this.message = (ChatColor.GRAY + "To view your guns type" + ChatColor.YELLOW + " /war list");
			this.timermax = 70;
		}

		if (this.type.equals("ffa")) {
			this.killType = "ffa";
			this.message = (ChatColor.AQUA + "Free For All! First person to 20 kills wins!");
			if (this.gameModifier.equals(GameModeModifier.ONEINCHAMBER))
				this.message = (ChatColor.AQUA + "OneIntheChamber! one life! DONT DIE");
			if (this.gameModifier.equals(GameModeModifier.GUNGAME))
				this.message = (ChatColor.AQUA + "GunGame! each kill ranks up your gun");
		}
		if (this.type.equals("tdm")) {
			this.message = (ChatColor.AQUA + "Team Deathmatch!");
			if (this.gameModifier.equals(GameModeModifier.CTF))
				this.message = (ChatColor.AQUA + "CTF! Capture the other teams flag!");
			if (this.gameModifier.equals(GameModeModifier.INFECT)) {
				this.message = (ChatColor.AQUA + "Infection! Kill the other team!");
			}
		}
		this.gunList.clear();
		this.gunList.add("usp45");
		this.gunList.add("m9");
		this.gunList.add("magnum");
		this.gunList.add("deserteagle");
		this.gunList.add("m16");
		this.gunList.add("m4a1");
		this.gunList.add("ak47");
		this.gunList.add("l118a");
		this.gunList.add("dragunov");
		this.gunList.add("barret50c");
		this.gunList.add("m1014");
		this.gunList.add("spas12");
		this.gunList.add("aa12");
		this.gunList.add("msr");
		this.gunList.add("moddel1887");
		this.gunList.add("famas");
		this.gunList.add("tomahawk");
	}

	public void tick() {
		try {
			if (this.timer > this.timermax) {
				this.timer = this.timermax;
			}
			if (this.type.equals("lobby"))
			{
				this.announcemaptimer -= 1;
				if (this.announcemaptimer <= 0) {
					this.announcemaptimer = 10;
					announceMap();
				}

				if (this.timer % 10 == 0) {
					removeMultiplePlayers();
				}
			}
			onTick();
			if (!this.stopped) {
				for (int i = this.players.size() - 1; i >= 0; i--)
					try {
						if (((KitPlayer)this.players.get(i)).player != null)
						{
							if (!this.field.isInside(((KitPlayer)this.players.get(i)).player.getLocation())) {
								((KitPlayer)this.players.get(i)).player.teleport(getSpawnLocation((KitPlayer)this.players.get(i)));
							}
						}
						((KitPlayer)this.players.get(i)).tick();

						String name = ((KitPlayer)this.players.get(i)).player.getName();
						if (name.length() + 2 >= 16)
							name = name.substring(0, 14);
						try {
							((KitPlayer)this.players.get(i)).player.setPlayerListName(((KitPlayer)this.players.get(i)).getTeamColor() + name);
						}
						catch (Exception localException1)
						{
						}
				}
				catch (Exception localException2)
				{
				}
				if (((this.players.size() > 0) || (this.started)) && (this.plugin.currentMap.equals(this)))
				{
					if (!this.busy) {
						this.timer -= 1;
						this.timeSinceStart += 1;
					}

					checkScores();
					checkTime();

					for (int i = 0; i < this.spawns.size(); i++) {
						((KitSpawn)this.spawns.get(i)).tick();
					}
				}
				if ((this.players.size() == 0) && (this.started))
					endGame("no players!");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void playerTick() {
		if (this.gameModifier.equals(GameModeModifier.INFECT))
			for (int i = 0; i < this.players.size(); i++) {
				KitPlayer kp = (KitPlayer)this.players.get(i);
				if ((kp.player != null) && (kp.player.isOnline()))
					kp.checkInventory();
			}
	}

	public void checkTime()
	{
		if (((this.timer <= 10) || (this.timer == 30) || (this.timer == 20) || (this.timer == 45) || (this.timer == 60) || (this.timer == 120) || (this.timer == 300)) && (this.timer > -1)) {
			this.plugin.broadcastMessage(null, Integer.toString(this.timer) + ChatColor.GRAY + " seconds!");
			if (this.timer <= 10) {
				for (int i = 0; i < this.players.size(); i++) {
					if (this.players.get(i) != null)
						try {
							Util.playEffect(Effect.CLICK1, ((KitPlayer)this.players.get(i)).player.getLocation(), 0);
						}
					catch (Exception localException1)
					{
					}
				}
			}
		}
		if (this.timer < 0)
			if (this.type.equals("lobby")) {
				if (this.players.size() > 0) {
					if (this.tomap != null)
						try {
							this.last = this.tomap;
							this.tomap.started = true;
							mergePlayers(this.tomap, "" + ChatColor.BOLD + ChatColor.RED + "Start!  map: " + this.tomap.name, false);
							this.busy = true;
							this.started = false;
							this.plugin.currentMap = this.tomap;
							this.tomap.onStart(null);
							this.tomap.killPlayers();
						} catch (Exception e) {
							broadcastMessage("Error Starting Arena! Restarting Lobby!");
							this.timer = this.timermax;
							System.out.println("ERROR STARTING ARENA");
						}
				}
				else {
					broadcastMessage("NOT ENOUGH PEOPLE! RESTARTING!");
					this.timer = this.timermax;
				}
			}
			else
				endGame("");
	}

	public void onStart(ArrayList<KitArenaStats> stats)
	{
		this.redkills = 0;
		this.bluekills = 0;
		this.redscore = 0;
		this.bluescore = 0;
		this.timeSinceStart = 0;
		try {
			this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "weather clear");
			this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "time set 6000");
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.busy = false;
		this.ticksSinceStart = 0;
		if (this.type.equals("lobby")) {
			if (stats != null) {
				for (int i = 0; i < this.players.size(); i++) {
					if (this.players.get(i) != null) {
						KitPlayer kpto = (KitPlayer)this.players.get(i);
						for (int ii = 0; ii < stats.size(); ii++) {
							KitArenaStats mystats = (KitArenaStats)stats.get(ii);
							if (mystats.getPlayer().player.equals(kpto.player)) {
								kpto.setLastArenaStats(mystats);
							}
						}
					}
				}
			}
			this.tomap = this.plugin.getRandomKitArena(this, this.last, getAmountPlayers());
			if (this.tomap != null) {
				try {
					Plugin p = Bukkit.getPluginManager().getPlugin("PVPGunPlus");
					if (p != null) {
						PVPGunPlus pv = (PVPGunPlus)p;
						if (pv != null)
							pv.reload();
					}
				}
				catch (Exception localException1)
				{
				}
				/*try {
					String path = this.plugin.getRoot().getAbsolutePath() + "/lastVote";
					Date date = new Date();
					Calendar calendar = GregorianCalendar.getInstance();
					calendar.setTimeZone(TimeZone.getTimeZone("EST"));
					calendar.setTime(date);
					BufferedReader br = FileIO.file_text_open_read(path);
					int i = calendar.get(5);
					int old = Integer.parseInt(FileIO.file_text_read_line(br));
					FileIO.file_text_close(br);
					if (i != old) {
						this.plugin.newDay();
						BufferedWriter wr = FileIO.file_text_open_write(path);
						FileIO.file_text_write_line(wr, Integer.toString(i));
						FileIO.file_text_close(wr);
					}
				}
				catch (Exception localException2) {
				}*/
				announceMap();
			}
		}

		if (this.mytimer != -1)
			this.plugin.getServer().getScheduler().cancelTask(this.mytimer);
		this.mytimer = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new ArenaUpdater(), 2L, 2L);

		this.maxscore = 75;
		this.timermax = 180;
		if (this.type.equals("tdm")) {
			if (getAmountPlayers() > 20) {
				this.maxscore = (75 + (getAmountPlayers() - 18) * 2);
				this.timermax = (180 + (40) * 5);
				if (this.timermax > 420)
					this.timermax = 420;
			}
			if (this.gameModifier.equals(GameModeModifier.CTF)) {
				this.maxscore = 5;
				for (int i = 0; i < this.flags.size(); i++) {
					((KitFlagStand)this.flags.get(i)).flag.removeFlag();
					((KitFlagStand)this.flags.get(i)).setup();
				}
			}
			if (this.gameModifier.equals(GameModeModifier.INFECT)) {
				this.timermax = ((int)(this.timermax * 0.75D));
				boolean loop = true;
				int ctr = 0;
				while (loop) {
					if (ctr > 64) {
						loop = false;
						endGame("Error!");
					}
					KitPlayer r = getRandomKitPlayer();
					if (r != null) {
						r.team = Teams.RED;
						r.boughtItems.add(new KitItem().setTag("rootZombie"));
						loop = false;
					}
				}
			}
			if (!this.gameModifier.equals(GameModeModifier.INFECT)) {
				balanceTeams();
			}
		}
		if (this.type.equals("ffa")) {
			this.timermax = 180;
			if (this.gameModifier.equals(GameModeModifier.GUNGAME)) {
				this.timermax = (180 + (40) * 5);
				if (this.timermax > 420)
					this.timermax = 420;
			}
			if (this.gameModifier.equals(GameModeModifier.ONEINCHAMBER)) {
				ArrayList players = getPlayers();
				for (int i = 0; i < players.size(); i++) {
					((KitPlayer)players.get(i)).lives = 3;
				}
			}
		}

		if (this.type.equals("lobby")) {
			this.timermax = 70;
		}
		this.timer = this.timermax;

		removeMultiplePlayers();
	}

	private void removeMultiplePlayers() {
		for (int i = this.players.size() - 1; i >= 0; i--) {
			int amt = getAmountOfPlayer(((KitPlayer)this.players.get(i)).player.getName());
			if (amt > 1)
				this.players.remove(i);
		}
	}

	private int getAmountOfPlayer(String string)
	{
		int ret = 0;
		for (int i = this.players.size() - 1; i >= 0; i--) {
			if (((KitPlayer)this.players.get(i)).player.getName().equals(string))
				ret++;
		}
		return ret;
	}

	private void balanceTeams() {
		int amtred = getAmountPlayers(Teams.RED);
		int amtblue = getAmountPlayers(Teams.BLUE);

		if (amtred >= amtblue + 2)
			balance(Teams.RED, Teams.BLUE, amtred - amtblue);
		if (amtblue >= amtred + 2)
			balance(Teams.BLUE, Teams.RED, amtblue - amtred);
	}

	private void balance(Teams from, Teams to, int amtPlayers) {
		amtPlayers -= 2;
		amtPlayers /= 2;
		int switched = 0;
		for (int i = 0; i < this.players.size(); i++) {
			if ((((KitPlayer)this.players.get(i)).team.equals(from)) && (((KitPlayer)this.players.get(i)).getClan() == null) && (switched < amtPlayers)) {
				((KitPlayer)this.players.get(i)).team = to;
				switched++;
			}
		}
		if (switched < amtPlayers)
			for (int i = 0; i < this.players.size(); i++)
				if ((((KitPlayer)this.players.get(i)).team.equals(from)) && (switched < amtPlayers)) {
					((KitPlayer)this.players.get(i)).team = to;
					switched++;
				}
	}

	public void endGame(String message)
	{
		if (this.mytimer != -1)
			this.plugin.getServer().getScheduler().cancelTask(this.mytimer);
		this.mytimer = -1;
		try {
			KitArena to = this.plugin.getFirstKitArena("lobby");
			if (to != null) {
				if (this.gameModifier.equals(GameModeModifier.CTF)) {
					for (int i = 0; i < this.flags.size(); i++) {
						((KitFlagStand)this.flags.get(i)).flag.removeFlag();
					}
				}

				ArrayList stats = generateGameStats();

				if ((this.timer < 0) && (message.equals(""))) {
					message = onOutOfTime();
				}
				this.bluescore = 0;
				this.redscore = 0;
				this.bluekills = 0;
				this.redkills = 0;

				to.busy = false;
				to.started = true;
				this.started = false;
				this.plugin.currentMap = to;
				if (this.timermax < 10)
					this.timermax = 10;
				this.timer = this.timermax;
				to.timer = to.timermax;
				if (!message.equals("FORCE END")) {
					this.plugin.onStartLobby();
					mergePlayers(to, "" + ChatColor.BOLD + ChatColor.RED + message, true);
					to.onStart(stats);
				}
			} else {
				List p = Util.Who();
				for (int i = 0; i < p.size(); i++)
					((Player)p.get(i)).kickPlayer("SORRY THERES AN ERROR IN THE SERVER");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			broadcastMessage("ERROR ENDING GAME!");
			List p = Util.Who();
			for (int i = 0; i < p.size(); i++)
				((Player)p.get(i)).chat("/spawn");
		}
	}

	private ArrayList<KitArenaStats> generateGameStats()
	{
		ArrayList ret = new ArrayList();
		for (int i = 0; i < this.players.size(); i++) {
			KitPlayer pl = (KitPlayer)this.players.get(i);
			if (pl != null) {
				ret.add(new KitArenaStats(pl, this));
			}
		}
		return ret;
	}

	public int getTeamKills(Teams team) {
		if (team.equals(Teams.BLUE))
			return this.bluekills;
		if (team.equals(Teams.RED))
			return this.redkills;
		return 0;
	}

	public String getLeader() {
		String ret = ChatColor.GRAY + "none  ";
		if (this.type.equals("tdm")) {
			if (this.gameModifier.equals(GameModeModifier.CTF))
			{
				if (this.bluescore > this.redscore)
					return ChatColor.BLUE + "blue(" + Integer.toString(this.bluescore - this.redscore) + ")  " + ChatColor.WHITE;
				if (this.bluescore < this.redscore)
					return ChatColor.RED + "red(" + Integer.toString(this.redscore - this.bluescore) + ")   " + ChatColor.WHITE;
			}
			else
			{
				if (this.bluekills > this.redkills)
					return ChatColor.BLUE + "blue(" + Integer.toString(this.bluekills - this.redkills) + ")  " + ChatColor.WHITE;
				if (this.bluekills < this.redkills) {
					return ChatColor.RED + "red(" + Integer.toString(this.redkills - this.bluekills) + ")   " + ChatColor.WHITE;
				}
			}

			return ChatColor.YELLOW + "tie(0)  " + ChatColor.WHITE;
		}if (this.type.equals("ffa")) {
			if (this.gameModifier.equals(GameModeModifier.GUNGAME)) {
				KitPlayer mostkills = getPlayerWithHighestGunGameRank();
				if (mostkills != null)
					return ChatColor.RED + mostkills.player.getName() + "(" + mostkills.gungameLevel + ")  ";
			}
			else {
				KitPlayer mostkills = getPlayerWithMostKills();
				if (mostkills != null) {
					return ChatColor.RED + mostkills.player.getName() + "(" + mostkills.kills + ")  ";
				}
			}
		}
		return ret;
	}

	public void checkScores()
	{
		if (this.type.equals("tdm"))
		{
			if (this.gameModifier.equals(GameModeModifier.CTF)) {
				if (this.bluescore > this.maxscore) {
					multiplyXP(Teams.BLUE, 1.5D);
					endGame(ChatColor.BLUE + "blue " + ChatColor.WHITE + " team has won!");
				} else if (this.redscore > this.maxscore) {
					multiplyXP(Teams.RED, 1.5D);
					endGame(ChatColor.RED + "red " + ChatColor.WHITE + " team has won!");
				}
			} else if (this.gameModifier.equals(GameModeModifier.INFECT)) {
				int amtonBlueTeam = getAmountPlayers(Teams.BLUE);
				if (amtonBlueTeam < 1) {
					multiplyXP(Teams.RED, 2.0D);
					endGame(ChatColor.RED + "red " + ChatColor.WHITE + " team has won!");
				} else {
					for (int i = 0; i < this.players.size(); i++) {
						KitPlayer kp = (KitPlayer)this.players.get(i);
						if ((kp.team.equals(Teams.BLUE)) && 
								(kp.player != null) && (kp.player.isOnline())) {
							kp.giveXp(1);
						}
					}
				}

			}
			else if (this.bluekills > this.maxscore) {
				multiplyXP(Teams.BLUE, 1.5D);
				endGame(ChatColor.BLUE + "blue " + ChatColor.WHITE + " team has won!");
			} else if (this.redkills > this.maxscore) {
				multiplyXP(Teams.RED, 1.5D);
				endGame(ChatColor.RED + "red " + ChatColor.WHITE + " team has won!");
			}
		}
		else if (this.type.equals("ffa"))
		{
			if (this.gameModifier.equals(GameModeModifier.ONEINCHAMBER)) {
				int size = getActivePlayers();
				if (size == 1) {
					KitPlayer kp = getLastPlayer();
					multiplyXP(Teams.ALL, 2.0D);
					endGame(kp.player.getName() + " has won!");
				}
			} else if (this.gameModifier.equals(GameModeModifier.GUNGAME)) {
				KitPlayer kp = getPlayerWithHighestGunGameRank();
				if ((kp != null) && 
						(kp.gungameLevel >= this.plugin.guns_gungame.size() - 1)) {
					multiplyXP(kp, 2.0D);
					endGame(kp.player.getName() + " has won!");
				}
			}
			else {
				KitPlayer kp = getPlayerWithMostKills();
				if ((kp != null) && 
						(kp.kills >= 20)) {
					multiplyXP(kp, 2.0D);
					endGame(kp.player.getName() + " has won!");
				}
			}
		}
	}

	public String onOutOfTime()
	{
		try
		{
			if (this.type.equals("tdm")) {
				if (this.gameModifier.equals(GameModeModifier.CTF)) {
					if (this.bluescore > this.redscore) {
						multiplyXP(Teams.BLUE, 1.5D);
						return ChatColor.BLUE + "blue " + ChatColor.WHITE + " team has won!";
					}if (this.redscore > this.bluescore) {
						multiplyXP(Teams.RED, 1.5D);
						return ChatColor.RED + "red " + ChatColor.WHITE + " team has won!";
					}
					multiplyXP(Teams.ALL, 1.25D);
					return ChatColor.RED + "STALEMATE!";
				}
				if (this.gameModifier.equals(GameModeModifier.INFECT)) {
					multiplyXP(Teams.BLUE, 2.0D);
					return ChatColor.BLUE + "blue " + ChatColor.WHITE + " team has won!";
				}
				if (this.bluekills > this.redkills) {
					multiplyXP(Teams.BLUE, 1.5D);
					return ChatColor.BLUE + "blue " + ChatColor.WHITE + " team has won!";
				}if (this.redkills > this.bluekills) {
					multiplyXP(Teams.RED, 1.5D);
					return ChatColor.RED + "red " + ChatColor.WHITE + " team has won!";
				}
				multiplyXP(Teams.ALL, 1.25D);
				return ChatColor.RED + "STALEMATE!";
			}

			if (this.type.equals("ffa")) {
				KitPlayer kp = getPlayerWithMostKills();
				if (kp != null) {
					multiplyXP(kp, 2.0D);
					String ret = "blank";
					try {
						ret = kp.player.getName();
					}
					catch (Exception localException) {
					}
					return ret + " has won!";
				}
				return "No winner!";
			}
		}
		catch (Exception localException1)
		{
		}
		return "Game Over!";
	}

	public void onTick() {
		if (!this.started)
		{
			KitArena running = this.tomap;
			if (running != null)
				for (int i = this.players.size() - 1; i >= 0; i--) {
					KitPlayer kp = (KitPlayer)this.players.get(i);
					if (kp.player != null)
						kp.player.setLevel(this.tomap.timer);
				}
		}
		else
		{
			for (int i = this.players.size() - 1; i >= 0; i--) {
				KitPlayer kp = (KitPlayer)this.players.get(i);
				if (kp.player != null)
					kp.player.setLevel(this.timer);
			}
		}
		
		if (this.ticksSinceStart == 2) {
			for (int i = 0; i < this.players.size(); i++) {
				players.get(i).player.playSound(players.get(i).player.getLocation(), Sound.BLAZE_DEATH, 6, 1);
			}
		}
	}

	public boolean onDeath(KitPlayer killed)
	{
		if (this.timeSinceStart < 10)
			return false;
		killed.onDeath();
		if ((this.gameModifier.equals(GameModeModifier.INFECT)) && 
				(!killed.team.equals(Teams.RED))) {
			killed.team = Teams.RED;
			broadcastMessage(ChatColor.DARK_RED + killed.player.getName() + ChatColor.RED + " is now infected!");
		}

		return true;
	}

	public void onDamage(EntityDamageByEntityEvent event, KitPlayer damager, KitPlayer attacked) {
		int originalDamage = (int) event.getDamage();
		if ((attacked.dead) || (attacked.alive < 1))
			return;
		if (attacked.player.getHealth() <= 0)
			return;
		//System.out.println(attacked.player.getHealth());
		if ((damager.killTicks > 0) || (attacked.killTicks > 0)) {
			event.setCancelled(true);
			return;
		}
		attacked.onDamagedByEvent(event);
		damager.onAttack(event);
		double dist = attacked.player.getLocation().distance(damager.player.getLocation());

		if ((this.gameModifier.equals(GameModeModifier.INFECT)) &&  (this.plugin.isInArena(damager.player)) && (this.plugin.isInArena(attacked.player))) {
			if (damager.team.equals(Teams.RED)) {
				if (event.getDamage() > 0) {
					if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
						if ((damager.player.getHealth() > 0) && (!damager.player.isDead())) {
							attacked.lastDamager = damager;
							attacked.player.damage(9999);
							attacked.player.setHealth(0);
							damager.kills += 1;
							if (damager.hasTag("rootZombie")) {
								damager.giveXp(100);
								damager.sayMessage(null, ChatColor.DARK_RED + "Root Zombie Xp Bonus: " + ChatColor.YELLOW + " +100");
							} else {
								damager.giveXp(25);
								damager.sayMessage(null, ChatColor.DARK_RED + "Normal Zombie Xp Bonus: " + ChatColor.YELLOW + " +25");
							}
						}
					} else if (event.getCause().equals(EntityDamageEvent.DamageCause.CUSTOM)) {
						event.setCancelled(true);
					}
				}
				else {
					event.setCancelled(true);
				}
			} else if (!attacked.hasTag("rootZombie"))
				event.setDamage((int)(event.getDamage() * 2.5D));
			else {
				event.setDamage((int)(event.getDamage() / 1.5D));
			}

		}
		ItemStack iteminhand = damager.player.getItemInHand();
		if (iteminhand != null) {
			String holding = iteminhand.getType().toString().toLowerCase();
			if (holding.contains("sword")) {
				if (holding.contains("diamond")) {
					if (dist <= 3.0D)
						event.setDamage(event.getDamage() * 3);
					else
						event.setCancelled(true);
				}
				else {
					event.setDamage((int)(event.getDamage() * 1.75D));
				}
			}
		}
		if ((this.gameModifier.equals(GameModeModifier.ONEINCHAMBER)) && (originalDamage > 0 || event.getDamager() instanceof Projectile)) {
			if (this.timeSinceStart < 4)
				return;
			System.out.println("G");
			if ((this.plugin.isInArena(damager.player)) && (this.plugin.isInArena(attacked.player)) && (damager.arena.equals(attacked.arena))) {
				String amt = "1";
				if (damager.hasPerk("scavenger"))
					amt = "2";
				event.setCancelled(true);
				System.out.println("H");
				this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "give " + damager.player.getName() + " " + Material.getMaterial(this.plugin.getGunAmmo(damager.kclass.secondary)).getId() + " " + amt);
				damager.sayMessage(null, ChatColor.DARK_AQUA + "killed " + ChatColor.AQUA + attacked.player.getName());
				damager.sayMessage(null, ChatColor.YELLOW + "+1" + ChatColor.WHITE + " ammo" + ChatColor.YELLOW + "    +30" + ChatColor.WHITE + " xp");
				damager.giveXp(30);
				damager.kills += 1;
				damager.player.updateInventory();
				attacked.deaths += 1;
				attacked.onDeath();
				System.out.println("I");
				if (attacked.lives <= 0) {
					attacked.lastDamager = damager;
					Player rejoin = attacked.player;
					this.plugin.leaveArena(attacked);
					this.plugin.joinArena(rejoin, null);

					this.plugin.getKitPlayer(rejoin).sayMessage(null, ChatColor.DARK_AQUA + "YOU'RE OUT!");
					this.plugin.getKitPlayer(rejoin).sayMessage(null, "killed by " + ChatColor.RED + damager.player.getName());
				} else {
					attacked.lastSpawn = 0;
					attacked.spawn();
					attacked.sayMessage(null, ChatColor.RED + "Lives left: " + ChatColor.DARK_RED + Integer.toString(attacked.lives));
				}
			}

		}

		if (!event.isCancelled()) {
			attacked.lastDamager = damager;
			Util.playEffect(Effect.STEP_SOUND, attacked.player.getLocation(), Material.REDSTONE_BLOCK.getId());
			Util.playEffect(Effect.STEP_SOUND, attacked.player.getLocation().add(0.0D, 1.0D, 0.0D), Material.REDSTONE_BLOCK.getId());
		}
	}

	public void mergePlayers(KitArena to, String send, boolean kill)
	{
		try {
			if (kill) {
				ArrayList sendto = new ArrayList();
				for (int i = this.players.size() - 1; i >= 0; i--)
					try {
						if (((KitPlayer)this.players.get(i)).player != null) {
							sendto.add(((KitPlayer)this.players.get(i)).player);
							((KitPlayer)this.players.get(i)).profile.gainxp = 0;
							((KitPlayer)this.players.get(i)).profile.creditsGain = 0;
							((KitPlayer)this.players.get(i)).profile.save((KitPlayer)this.players.get(i));
							leaveArena((KitPlayer)this.players.get(i));
						}
					}
				catch (Exception localException1)
				{
				}
				this.players.clear();
				for (int i = 0; i < sendto.size(); i++)
					to.join((Player)sendto.get(i));
			}
			else {
				for (int i = this.players.size() - 1; i >= 0; i--) {
					try {
						if ((((KitPlayer)this.players.get(i)).player != null) && 
								(((KitPlayer)this.players.get(i)).player.isOnline())) {
							((KitPlayer)this.players.get(i)).profile.save((KitPlayer)this.players.get(i));
							to.join((KitPlayer)this.players.get(i), ((KitPlayer)this.players.get(i)).player);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				this.players.clear();
			}

			to.broadcastMessage(send);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getActivePlayers()
	{
		int ret = 0;
		for (int i = this.players.size() - 1; i >= 0; i--) {
			if ((((KitPlayer)this.players.get(i)).player != null) && 
					(((KitPlayer)this.players.get(i)).player.isOnline())) {
				ret++;
			}
		}

		return ret;
	}

	public KitPlayer getLastPlayer()
	{
		for (int i = this.players.size() - 1; i >= 0; i--) {
			if ((((KitPlayer)this.players.get(i)).player != null) && 
					(((KitPlayer)this.players.get(i)).player.isOnline())) {
				return (KitPlayer)this.players.get(i);
			}
		}

		return null;
	}

	public void getKill(KitPlayer kp) {
		if (this.type.equals("tdm")) {
			if (kp.team.equals(Teams.BLUE))
				this.bluekills += 1;
			else if (kp.team.equals(Teams.RED))
				this.redkills += 1;
		}
		else this.type.equals("ffa");
	}

	public int getAmountPlayers()
	{
		int ret = 0;
		for (int i = this.players.size() - 1; i >= 0; i--) {
			if ((((KitPlayer)this.players.get(i)).player != null) && 
					(((KitPlayer)this.players.get(i)).player.isOnline())) {
				ret++;
			}
		}

		return ret;
	}

	public int getAmountPlayers(Teams team) {
		int ret = 0;
		for (int i = this.players.size() - 1; i >= 0; i--) {
			if ((((KitPlayer)this.players.get(i)).player != null) && 
					(((KitPlayer)this.players.get(i)).player.isOnline()) && (
							(((KitPlayer)this.players.get(i)).team == team) || (team.equals(Teams.ALL)))) {
				ret++;
			}

		}

		return ret;
	}

	private Teams getTeam(KitPlayer player) {
		if (this.type.equals("ffa")) {
			return Teams.FFA;
		}
		if (this.type.equals("lobby")) {
			return Teams.NEUTRAL;
		}

		if (this.gameModifier.equals(GameModeModifier.INFECT)) {
			return Teams.BLUE;
		}

		int amtred = getAmountPlayers(Teams.RED);
		int amtblue = getAmountPlayers(Teams.BLUE);

		if ((this.type.equals("tdm")) && (!this.gameModifier.equals(GameModeModifier.INFECT))) {
			Clan c = player.getClan();
			if (c != null) {
				Teams preference = getTeamWithClanMajority(c);
				if (preference != null) {
					return preference;
				}
			}
		}

		if (amtred > amtblue) {
			return Teams.BLUE;
		}
		if (amtred < amtblue) {
			return Teams.RED;
		}

		int rand = Util.random(2);
		if (rand == 1) {
			return Teams.BLUE;
		}
		return Teams.RED;
	}

	private Teams getTeamWithClanMajority(Clan c)
	{
		Teams ret = null;
		int clanMajorityRed = 0;
		int clanMajorityBlue = 0;

		for (int i = 0; i < this.players.size(); i++) {
			if ((((KitPlayer)this.players.get(i)).getClan() != null) && 
					(((KitPlayer)this.players.get(i)).getClan().getName().equals(c.getName()))) {
				if (((KitPlayer)this.players.get(i)).team.equals(Teams.RED)) {
					clanMajorityRed++;
				}
				if (((KitPlayer)this.players.get(i)).team.equals(Teams.BLUE)) {
					clanMajorityBlue++;
				}
			}

		}

		if (clanMajorityRed > clanMajorityBlue)
			return Teams.RED;
		if (clanMajorityBlue > clanMajorityRed)
			return Teams.BLUE;
		return ret;
	}

	public void announceMap() {
		try {
			if (this.started)
				broadcastMessage(ChatColor.DARK_RED + "NEXT MAP: " + ChatColor.GOLD + this.tomap.name + ChatColor.DARK_RED + "     GAMEMODE: " + ChatColor.GOLD + this.tomap.getArenaType());
			else
				broadcastMessage(ChatColor.DARK_RED + "CURRENT MAP: " + ChatColor.GOLD + this.tomap.name + ChatColor.DARK_RED + "     GAMEMODE: " + ChatColor.GOLD + this.tomap.getArenaType());
		}
		catch (Exception localException) {
		}
	}

	public String getArenaType() {
		String ret = this.type;
		if (!this.gameModifier.equals(GameModeModifier.NONE)) {
			ret = this.gameModifier.toString().toLowerCase();
		}
		return ret;
	}

	public void broadcastMessage(String send) {
		for (int i = this.players.size() - 1; i >= 0; i--)
			((KitPlayer)this.players.get(i)).sayMessage(null, send);
	}

	private void killPlayers() {
		for (int i = this.players.size() - 1; i >= 0; i--) {
			final Player player = players.get(i).player;
			
			((KitPlayer)this.players.get(i)).player.setHealth(0);
			((KitPlayer)this.players.get(i)).player.damage(99, ((KitPlayer)this.players.get(i)).player);

	        
			try{
				this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						PacketPlayInClientCommand in = new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN);
				        EntityPlayer cPlayer = ((CraftPlayer)player).getHandle();
				        cPlayer.playerConnection.a(in);
					}
				}, 2L);
			}catch(Exception e) {
				//
			}
		}
	}

	private void multiplyXP(Teams e, double d) {
		for (int i = 0; i < this.players.size(); i++)
			if ((((KitPlayer)this.players.get(i)).team == e) || (e.equals(Teams.ALL)))
				multiplyXP((KitPlayer)this.players.get(i), d);
	}

	private void multiplyXP(KitPlayer kp, double d)
	{
		d -= 1.0D;
		if (d < 0.0D)
			d = 0.0D;
		int newxp = (int)(kp.profile.gainxp * d);
		kp.profile.xp += newxp;
	}

	private KitPlayer getRandomKitPlayer() {
		return (KitPlayer)this.players.get(this.r.nextInt(getActivePlayers()));
	}

	public Location getSpawnLocation(KitPlayer kp) {
		Location addto = new Location((World)Bukkit.getServer().getWorlds().get(0), 0.5D, 0.5D, 0.5D);
		if (this.type.equals("tdm")) {
			if (kp.team.equals(Teams.BLUE)) {
				return this.spawnpoint.clone().add(addto);
			}
			return this.spawnpoint2.clone().add(addto);
		}
		if (this.type.equals("ffa")) {
			KitSpawn ks = getKitSpawn(kp);
			if (ks != null) {
				ks.spawn(kp);
				return ks.location.clone().add(addto);
			}
		}
		return this.spawnpoint.clone().add(addto);
	}

	public KitSpawn getKitSpawn(KitPlayer check) {
		KitSpawn ret = null;
		int farthest = 0;
		for (int i = 0; i < this.spawns.size(); i++) {
			KitSpawn spawn = (KitSpawn)this.spawns.get(i);
			int nearest = 9999999;
			for (int ii = this.players.size() - 1; ii >= 0; ii--) {
				KitPlayer player = (KitPlayer)this.players.get(ii);
				if (!player.equals(check)) {
					double distance = Util.point_distance(spawn.getLocation(), player.player.getLocation());
					if (distance < nearest) {
						nearest = (int)distance;
					}
				}
			}

			if (nearest > farthest) {
				farthest = nearest;
				ret = spawn;
			}
		}
		return ret;
	}

	public void stop() {
		this.stopped = true;
		for (int i = this.players.size() - 1; i >= 0; i--) {
			this.plugin.leaveArena((KitPlayer)this.players.get(i));
		}
		this.players.clear();
		endGame("FORCE END");
	}

	public boolean isFull() {
		return getAmountPlayers() >= this.maxPlayers;
	}

	public boolean checkSpawn(KitPlayer player) {
		player.sayMessage(null, this.message);
		if (this.type.equals("lobby")) {
			return true;
		}
		if ((this.gameModifier.equals(GameModeModifier.INFECT)) && 
				(player.team.equals(Teams.RED))) {
			new Perk(player.player).addPotion("SPEED", 0).giveToPlayer(player.player).clear();
			return true;
		}

		if (this.type.equals("ffa")) {
			if (this.gameModifier.equals(GameModeModifier.ONEINCHAMBER))
			{
				player.giveItem(player.player, player.kclass.weapon1, null, (byte)0, 1, 0);
				if (player.profile.knife == 1)
					player.giveItem(player.player, Material.IRON_SWORD.getId(), new KitEnchantment(), (byte)0, 1, 0);
				if (player.profile.knife == 2) {
					player.giveItem(player.player, Material.DIAMOND_SWORD.getId(), new KitEnchantment(), (byte)0, 1, 0);
				}
				player.giveItem(player.player, player.kclass.weapon3, null, (byte)0, 1, 1);
				player.giveItem(player.player, player.kclass.weapon9, null, (byte)0, 1, 8);
				return true;
			}
			if (this.gameModifier.equals(GameModeModifier.GUNGAME)) {
				player.spawn_gunGame();
				return true;
			}
		}
		return false;
	}

	public KitPlayer join(Player p) {
		if (p == null)
			return null;
		if (!p.isOnline())
			return null;
		KitPlayer kap = new KitPlayer();
		join(kap, p);
		return kap;
	}

	public KitPlayer join(KitPlayer kap, Player p) {
		if (p == null)
			return null;
		if (!p.isOnline())
			return null;
		kap.start(this.plugin, this, p, getTeam(kap));
		kap.spawn();
		this.players.add(kap);
		try
		{
			TagAPI.refreshPlayer(kap.player);
		}
		catch (Exception localException) {
		}
		if (this.busy) {
			kap.sayMessage(null, ChatColor.WHITE + "An arena is already in progress!");
			kap.sayMessage(null, "" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "  It will be done shortly! Please wait");
		}
		return kap;
	}

	public KitPlayer getPlayer(Player player) {
		for (int i = this.players.size() - 1; i >= 0; i--) {
			if ((player != null) && (((KitPlayer)this.players.get(i)).player != null) && 
					(player.getName().equals(((KitPlayer)this.players.get(i)).player.getName()))) {
				return (KitPlayer)this.players.get(i);
			}
		}

		return null;
	}

	public KitPlayer getPlayer(String player) {
		for (int i = this.players.size() - 1; i >= 0; i--) {
			if ((player != null) && (((KitPlayer)this.players.get(i)).player != null) && 
					(player.equals(((KitPlayer)this.players.get(i)).player.getName()))) {
				return (KitPlayer)this.players.get(i);
			}
		}

		return null;
	}

	public void leaveArena(KitPlayer kp) {
		try {
			kp.player.setPlayerListName(kp.player.getName());
		}
		catch (Exception localException) {
		}
		for (int i = this.players.size() - 1; i >= 0; i--) {
			try {
				if (kp.player.getName().equals(((KitPlayer)this.players.get(i)).player.getName()))
					this.players.remove(i);
			}
			catch (Exception localException1)
			{
			}
		}
		this.plugin.clearPlayer(kp.player);
		kp.player.teleport(kp.returnto);
	}

	public void loadArena() {
		String path = this.plugin.getRoot().getAbsolutePath() + "/arenas/" + this.name;
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		try {
			fstream = new FileInputStream(path);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			Location loc1 = getLocationFromString(br.readLine());
			Location loc2 = getLocationFromString(br.readLine());
			this.spawnpoint = getLocationFromString(br.readLine());
			this.spawnpoint2 = getLocationFromString(br.readLine());

			this.field.setParam(loc1.getX(), loc1.getZ(), loc2.getX(), loc2.getZ());
		} catch (Exception e) {
			System.err.print("ERROR READING KITPVP ARENA");
		}
		loadConfig(br);
		try { br.close(); } catch (Exception localException1) {
		}try { in.close(); } catch (Exception localException2) {
		}try { fstream.close(); } catch (Exception localException3) {
		}
	}

	private void loadConfig(BufferedReader br) { ArrayList file = new ArrayList();
	try {
		String str = br.readLine();
		if ((str != null) &&  (str.equals("--config--"))) {
			String strLine;
			while ((strLine = br.readLine()) != null) {
				file.add(strLine);
			}
			for (int i = 0; i < file.size(); i++) {
				computeConfigData((String)file.get(i));
			}
		}
	}
	catch (IOException localIOException) {
	}
	if (this.minPlayers == -1) {
		this.minPlayers = (this.maxPlayers - 40);
	}
	if (this.maxPlayers >= Util.server.getMaxPlayers())
		this.maxPlayers = (Util.server.getMaxPlayers() * 2);
	}

	private void computeConfigData(String str) {
		if (str.indexOf("=") > 0) {
			String str2 = str.substring(0, str.indexOf("="));
			if (str2.equalsIgnoreCase("type"))
				this.type = str.substring(str.indexOf("=") + 1);
			if (str2.equalsIgnoreCase("maxPlayers"))
				this.maxPlayers = Integer.parseInt(str.substring(str.indexOf("=") + 1));
			if (str2.equalsIgnoreCase("minPlayers"))
				this.minPlayers = Integer.parseInt(str.substring(str.indexOf("=") + 1));
			if (str2.equalsIgnoreCase("modifier")) {
				String check = str.substring(str.indexOf("=") + 1).toUpperCase();
				if (check.length() > 1) {
					this.gameModifier = GameModeModifier.valueOf(check);
				}
			}
			if (str2.equalsIgnoreCase("addspawn")) {
				Location spawnloc = getLocationFromString(str.substring(str.indexOf("=") + 1));
				if (spawnloc != null)
					if (this.gameModifier.equals(GameModeModifier.CTF)) {
						int color = 11;
						if (this.flags.size() == 1)
							color = 14;
						this.flags.add(new KitFlagStand(this, color, spawnloc));
					} else {
						KitSpawn ks = new KitSpawn(spawnloc);
						ks.lastSpawn = (1000 + Util.random(200));
						this.spawns.add(ks);
					}
			}
		}
	}

	public Location getLocationFromString(String str)
	{
		String[] arr = str.split(",");
		if (arr.length == 2)
			return new Location((World)this.plugin.getServer().getWorlds().get(0), Integer.parseInt(arr[0]), 0.0D, Integer.parseInt(arr[1]));
		if (arr.length == 3) {
			return new Location((World)this.plugin.getServer().getWorlds().get(0), Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
		}
		return null;
	}

	public String getMode() {
		return this.type;
	}

	public ArrayList<KitPlayer> getPlayersOnTeam(Teams team) {
		ArrayList ret = new ArrayList();
		for (int i = 0; i < this.players.size(); i++) {
			if ((((KitPlayer)this.players.get(i)).team == team) || (team.equals(Teams.ALL))) {
				ret.add((KitPlayer)this.players.get(i));
			}
		}
		return ret;
	}

	public ArrayList<KitPlayer> getPlayers() {
		ArrayList ret = new ArrayList();
		for (int i = 0; i < this.players.size(); i++) {
			if ((this.players.get(i) != null) && 
					(((KitPlayer)this.players.get(i)).player.isOnline())) {
				ret.add((KitPlayer)this.players.get(i));
			}
		}

		return ret;
	}

	public KitPlayer getPlayerWithMostKills() {
		KitPlayer most = null;
		int kills = 0;
		for (int i = 0; i < this.players.size(); i++) {
			if (((KitPlayer)this.players.get(i)).kills > kills) {
				kills = ((KitPlayer)this.players.get(i)).kills;
				most = (KitPlayer)this.players.get(i);
			}
		}
		return most;
	}

	public KitPlayer getPlayerWithHighestGunGameRank() {
		KitPlayer most = null;
		int kills = 0;
		for (int i = 0; i < this.players.size(); i++) {
			if (((KitPlayer)this.players.get(i)).gungameLevel > kills) {
				kills = ((KitPlayer)this.players.get(i)).gungameLevel;
				most = (KitPlayer)this.players.get(i);
			}
		}
		return most;
	}

	public KitFlag getFlag(Item item) {
		if (this.gameModifier.equals(GameModeModifier.CTF)) {
			for (int i = 0; i < this.flags.size(); i++) {
				if (((KitFlagStand)this.flags.get(i)).flag.itemInWorld.equals(item)) {
					return ((KitFlagStand)this.flags.get(i)).flag;
				}
			}
		}
		return null;
	}

	public int getWoolColor(KitPlayer kitPlayer) {
		if ((this.gameModifier.equals(GameModeModifier.INFECT)) && 
				(kitPlayer.team.equals(Teams.RED))) {
			MaterialData data = new MaterialData(397);
			if (kitPlayer.hasTag("rootzombie"))
				data.setData((byte)0);
			else {
				data.setData((byte)2);
			}
			ItemStack itm = data.toItemStack(1);
			kitPlayer.player.getInventory().setHelmet(itm);
			return 999;
		}

		return -1;
	}

	public class ArenaUpdater
	implements Runnable
	{
		public ArenaUpdater()
		{
		}

		public void run()
		{
			for (int i = 0; i < KitArena.this.flags.size(); i++)
				((KitFlagStand)KitArena.this.flags.get(i)).tick();
		}
	}

	public static enum GameModeModifier
	{
		NONE, ONEINCHAMBER, CTF, INFECT, GUNGAME;
	}

	public static enum Teams {
		RED, BLUE, FFA, NEUTRAL, ALL;
	}
}