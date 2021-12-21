package com.orange451.mcwarfare;

import com.orange451.mcwarfare.arena.Clan;
import com.orange451.mcwarfare.arena.KitArena;
import com.orange451.mcwarfare.arena.KitGun;
import com.orange451.mcwarfare.arena.KitPlayer;
import com.orange451.mcwarfare.arena.KitProfile;
import com.orange451.mcwarfare.arena.create.KitArenaCreator;
import com.orange451.mcwarfare.arena.kits.KitClass;
import com.orange451.mcwarfare.arena.kits.Perk;
import com.orange451.mcwarfare.arena.kits.PerkFlakjacket;
import com.orange451.mcwarfare.arena.kits.PerkHardline;
import com.orange451.mcwarfare.arena.kits.PerkJuggernaut;
import com.orange451.mcwarfare.arena.kits.PerkMarathon;
import com.orange451.mcwarfare.arena.kits.PerkMartyrdom;
import com.orange451.mcwarfare.arena.kits.PerkScavenger;
import com.orange451.mcwarfare.arena.kits.PerkSleightofhand;
import com.orange451.mcwarfare.arena.kits.PerkSpeed;
import com.orange451.mcwarfare.arena.kits.PerkStoppingPower;
import com.orange451.mcwarfare.arena.mapitem.MapItem;
import com.orange451.mcwarfare.commands.PBaseCommand;
import com.orange451.mcwarfare.commands.PCommandAddSpawn;
import com.orange451.mcwarfare.commands.PCommandBuy;
import com.orange451.mcwarfare.commands.PCommandChat;
import com.orange451.mcwarfare.commands.PCommandCreate;
import com.orange451.mcwarfare.commands.PCommandGui;
import com.orange451.mcwarfare.commands.PCommandGun;
import com.orange451.mcwarfare.commands.PCommandHelp;
import com.orange451.mcwarfare.commands.PCommandJoin;
import com.orange451.mcwarfare.commands.PCommandLeave;
import com.orange451.mcwarfare.commands.PCommandList;
import com.orange451.mcwarfare.commands.PCommandReload;
import com.orange451.mcwarfare.commands.PCommandSetPoint;
import com.orange451.mcwarfare.commands.PCommandStop;
import com.orange451.mcwarfare.listeners.PluginBlockListener;
import com.orange451.mcwarfare.listeners.PluginEntityListener;
import com.orange451.mcwarfare.listeners.PluginPlayerListener;
import com.orange451.opex.Opexmain;
import com.orange451.opex.permissions.PermissionInterface;
import com.orange451.pvpgunplus.PVPGunPlus;
import com.orange451.pvpgunplus.gun.Gun;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitScheduler;

public class KitPvP extends JavaPlugin
{
	public List<Location> glassThinReplace = new ArrayList();
	public List<MapItem> mapItems = new ArrayList<MapItem>();
	private PluginBlockListener blockListener = new PluginBlockListener(this);
	private PluginPlayerListener playerListener = new PluginPlayerListener(this);
	private PluginEntityListener entityListener = new PluginEntityListener(this);
	public List<KitArena> activeArena = new ArrayList<KitArena>();
	public List<KitClass> classes = new ArrayList<KitClass>();
	public List<KitArenaCreator> creatingArena = new ArrayList<KitArenaCreator>();
	public ArrayList<KitGun> loadedGuns = new ArrayList<KitGun>();
	public List<String> guns_gungame = new ArrayList<String>();
	private List<PBaseCommand> commands = new ArrayList<PBaseCommand>();
	private List<String> messages = new ArrayList<String>();
	private List<Clan> clans = new ArrayList<Clan>();
	private List<String> codclients = new ArrayList<String>();
	private int timer;
	private int playerTimer;
	private int announceTimer = 60;
	private int lastAnnouncement;
	private boolean loaded = false;
	public KitArena currentMap = null;
	public boolean stopped = false;
	public Random r = new Random();
	//public MCWarPlayerSave lbsave;
	public int timerunning = 0;
	public int secondsAlive;
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> parameters = new ArrayList<String>(Arrays.asList(args));
		Player player = null;
		boolean fromConsole = false;
		boolean candocommand = false;
		boolean ismod = false;
		String fullCMD = commandLabel;
		for (int i = 0; i < args.length; i++) {
			fullCMD += " " + args[i];
		}
		if (sender instanceof org.bukkit.command.ConsoleCommandSender) {
			candocommand = true;
			ismod = true;
			fromConsole = true;
		}
		if (sender instanceof Player) {
			player = (Player)sender;
			if (PermissionInterface.hasPermission((Player)sender, "mcwar.admin")) {
				candocommand = true;
				ismod = true;
			}
			if (PermissionInterface.hasPermission((Player)sender, "mcwar.mod")) {
				ismod = true;
			}
		}
		
		if (commandLabel.equals("spawn")) {
			if (player != null) {
				player.teleport(this.getServer().getWorlds().get(0).getSpawnLocation().clone().add(0,1,0));
			}
			return false;
		}
		
		if (candocommand || ismod) {
			if (commandLabel.equals("profile") && candocommand) {
				try{
					String name = args[0];
					KitProfile kp = this.readProfile(name);
					kp.dumpStats();
					kp.CLEAR();
				}catch(Exception e) {
					e.printStackTrace();
				}
				this.giveMessage(sender, "Incorrect use of command!");
				return false;
			}
			if (commandLabel.equals("dprofile") && candocommand) {
				try{
					String name = args[0];
					KitProfile kp = this.readProfile(name);
					kp.delete();
					kp.CLEAR();
				}catch(Exception e) {
					e.printStackTrace();
				}
				this.giveMessage(sender, "Incorrect use of command!");
				return false;
			}
			if (commandLabel.equals("opex") && candocommand) {
				try{
					if (args[0].equals("user")) {
						logDonation(fullCMD);
						if (args[2].equals("add")) {
							PermissionInterface.addPermission(args[1], args[3]);
							this.giveMessage(sender, "Permission added!");
							return true;
						}
						if (args[2].equals("remove")) {
							PermissionInterface.removePermission(args[1], args[3]);
							this.giveMessage(sender, "Permission removed!");
							return true;
						}
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
				this.giveMessage(sender, "Incorrect use of command!");
				return false;
			}
			if (commandLabel.equals("mod") && candocommand) {
				try{
					Player tomod = Util.MatchPlayer(args[0]);
					if (tomod != null) {
						if (PermissionInterface.hasPermission(tomod, "mcwar.mod")) {
							PermissionInterface.removePermission(args[0], "mcwar.mod");
							try{
								getKitPlayer(tomod).sayMessage(null, "NO LONGER MOD");
								this.giveMessage(sender, "NO LONGER MOD");
							}catch(Exception e) {
								//
							}
						}else{
							PermissionInterface.addPermission(args[0], "mcwar.mod");
							try{
								getKitPlayer(tomod).sayMessage(null, "You have been modded");
								this.giveMessage(sender, "modded: " + tomod.getName());
							}catch(Exception e) {
								//
							}
						}
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
				this.giveMessage(sender, "Incorrect use of command!");
				return false;
			}
			if (commandLabel.equals("kickplayer")) {
				try{
					Player tomod = getServer().getPlayerExact(args[0]);
					if (tomod != null) {
						this.giveMessage(sender, "Kicked: " + getPlayerName(tomod));
						tomod.kickPlayer("Kicked by staff member: " + getPlayerName(sender));
					}
					else
						this.giveMessage(sender, "Unnable to find: " + args[0]);

				}catch(Exception e) {
					this.giveMessage(sender, "Error processing command: " + e);
					e.printStackTrace();
				}
				return true;
			}
			/*if (commandLabel.equals("ban") && candocommand) {
				try{
					Player tomod = Util.MatchPlayer(args[0]);
					if (tomod != null) {
						this.banPlayer(tomod, "banned by admin");
						return true;
					}else{
						this.banPlayer(args[0], "banned by admin");
						return true;
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
				this.giveMessage(sender, "Incorrect use of command!");
				return false;
			}
			
			if (commandLabel.equals("unban") && candocommand) {
				try{
					logDonation(fullCMD);
					this.unbanPlayer(args[0]);
				}catch(Exception e) {
					e.printStackTrace();
				}
				this.giveMessage(sender, "Incorrect use of command!");
				return false;
			}
			
			if ((commandLabel.equals("banip") || commandLabel.equals("eban") || commandLabel.equals("ban-ip")) && candocommand) {
				this.giveMessage(sender, "NOT ALLOWED!");
				return false;
			}*/
			
			if (commandLabel.equals("givexp") && candocommand) {
				if (args.length == 2) {
					String pl = args[0];
					String amt = args[1];
					try {
						if (fromConsole) {
							KitProfile kp = this.readProfile(pl);
							if (kp != null) {
								logDonation(fullCMD);
								kp.xp += Integer.parseInt(amt);
								kp.save(pl);
								kp.CLEAR();
							}else{
								logDonation(fullCMD + "[ERROR] NO PROFILE");
							}
						}else{
							Player p = Util.MatchPlayer(pl);
							if (p.isOnline()) {
								int xp = Integer.parseInt(amt);
								KitPlayer kp = this.getKitPlayer(p);
								if (kp == null) {
									kp = this.joinArena(p, null);
								}
								if (kp != null) {
									kp.giveXp(xp);
									kp.sayMessage(null, ChatColor.DARK_RED + "You have been given the xp amount: " + ChatColor.BOLD + ChatColor.RED + amt);
								}else{
									
								}
							}
						}
					}catch(Exception e) {
						
					}
				}
				return false;
			}
			
			if (commandLabel.equals("setkills") && candocommand) {
				if (args.length == 2) {
					String pl = args[0];
					String amt = args[1];
					try {
						if (fromConsole) {
							KitProfile kp = this.readProfile(pl);
							if (kp != null) {
								kp.kills = Integer.parseInt(amt);
								kp.save(pl);
								kp.CLEAR();
							}
						}else{
							Player p = Util.MatchPlayer(pl);
							if (p.isOnline()) {
								int xp = Integer.parseInt(amt);
								KitPlayer kp = this.getKitPlayer(p);
								if (kp != null) {
									kp.profile.kills = xp;
									kp.profile.save(kp);
									kp.sayMessage(null, ChatColor.DARK_RED + "Your kills are now: " + ChatColor.BOLD + ChatColor.RED + amt);
								}
							}
						}
					}catch(Exception e) { }
				}
				return false;
			}
			
			if (commandLabel.equals("setdeaths") && candocommand) {
				if (args.length == 2) {
					String pl = args[0];
					String amt = args[1];
					try {
						if (fromConsole) {
							KitProfile kp = this.readProfile(pl);
							if (kp != null) {
								kp.deaths = Integer.parseInt(amt);
								kp.save(pl);
								kp.CLEAR();
							}
						}else{
							Player p = Util.MatchPlayer(pl);
							if (p.isOnline()) {
								int xp = Integer.parseInt(amt);
								KitPlayer kp = this.getKitPlayer(p);
								if (kp != null) {
									kp.profile.deaths = xp;
									kp.profile.save(kp);
									kp.sayMessage(null, ChatColor.DARK_RED + "Your deaths are now: " + ChatColor.BOLD + ChatColor.RED + amt);
								}
							}
						}
					}catch(Exception e) { }
				}
				return false;
			}
			
			if (commandLabel.equals("day") && candocommand) {
				this.getServer().getWorlds().get(0).setTime(0);
				return false;
			}
			
			if (commandLabel.equals("weather") && candocommand) {
				this.getServer().getWorlds().get(0).setStorm(false);
				return false;
			}
			
			if (commandLabel.equals("setspawn") && candocommand) {
				if (player != null) {
					this.getServer().getWorlds().get(0).setSpawnLocation(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
				}
				return false;
			}
			
			if (commandLabel.equals("givegun") && candocommand) {
				if (args.length == 2) {
					String pl = args[0];
					String gun = args[1];
					try {
						if (fromConsole) {
							KitProfile kp = this.readProfile(pl);
							if (kp != null) {
								//logDonation(fullCMD);
								kp.boughtGuns.add(gun);
								kp.save(pl);
								kp.CLEAR();
							}else{
								logDonation(fullCMD + "[ERROR] NO PROFILE");
							}
						}else{
							Player p = Util.MatchPlayer(pl);
							if (p != null && p.isOnline()) {
								KitPlayer kp = this.getKitPlayer(p);
								if (kp == null) {
									kp = this.joinArena(p, null);
								}
								if (kp != null) {
									kp.profile.boughtGuns.add(gun);
									kp.sayMessage(null, ChatColor.DARK_RED + "You have been given the gun: " + ChatColor.BOLD + ChatColor.RED + gun);
								}
							}else{
								if (player != null) {
									KitProfile kp = this.loadProfile(pl);
									kp.boughtGuns.add(gun);
									kp.save(pl);
									kp.CLEAR();
								}
							}
						}
					}catch(Exception e) {
						//e.printStackTrace();
					}
				}
				return false;
			}
			
			if (commandLabel.equals("giveperk") && candocommand) {
				if (args.length == 2) {
					String pl = args[0];
					String gun = args[1];
					try {
						if (fromConsole) {
							KitProfile kp = this.readProfile(pl);
							if (kp != null) {
								logDonation(fullCMD);
								kp.perks.add(gun);
								kp.save(pl);
								kp.CLEAR();
							}else{
								logDonation(fullCMD + "[ERROR] NO PROFILE");
							}
						}else{
							Player p = Util.MatchPlayer(pl);
							if (p.isOnline()) {
								KitPlayer kp = this.getKitPlayer(p);
								if (kp == null) {
									kp = this.joinArena(p, null);
								}
								if (kp != null) {
									kp.profile.perks.add(gun);
									kp.sayMessage(null, ChatColor.DARK_RED + "You have been given the perk: " + ChatColor.BOLD + ChatColor.RED + gun);
									kp.profile.save(kp);
								}
							}
						}
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
				return false;
			}
		}else{
		}
		
		this.handleCommand(sender, parameters);
		return true;
	}
	
	private void logDonation(String string) {
		ArrayList<String> g = new ArrayList<String>();
		String path = KitPvP.getMcWar() + "/donation_log.txt";
		BufferedReader in = FileIO.file_text_open_read(path);
		try{
			String strLine = null;
			while((strLine = FileIO.file_text_read_line(in)) != null ) {
				g.add(strLine);
			}
			FileIO.file_text_close(in);
		}catch(Exception e) {
			//
		}
		
		BufferedWriter out = FileIO.file_text_open_write(path);
		try{
			for (int i = 0; i < g.size(); i++) {
				FileIO.file_text_write_line(out, g.get(i));
			}
			FileIO.file_text_write_line(out, string);
		}catch(Exception e) {
			//
		}
		FileIO.file_text_close(out);
	}

	public void handleCommand(CommandSender sender, List<String> parameters) {
		if (parameters.size() == 0) {
			this.commands.get(0).execute(sender, parameters);
			return;
		}
		
		String commandName = parameters.get(0).toLowerCase();
		
		for (PBaseCommand fcommand : this.commands) {
			if (fcommand.getAliases().contains(commandName)) {
				fcommand.execute(sender, parameters);
				return;
			}
		}
		
		sender.sendMessage(ChatColor.YELLOW + "Unknown MCWarfare command \""+commandName+"\". Try /war help");
	}
	
	public List<PBaseCommand> getCommands() {
		return commands;
	}
	
	public File getRoot() {
		return getDataFolder();
	}
	
	public static String getMcWar() {
		return getFTP();
	}
	
	public String getUsers() {
		return getFTP() + "/accounts";
	}
	
	public static String getFTP() {
		return "/Shared/mcwar";
	}

	public void makeDirectories() {
		new File(getMcWar()).mkdirs();
		new File(getUsers()).mkdir();
	}

	public void reloadMessages() {
		this.messages.clear();
		BufferedReader br = FileIO.file_text_open_read(getFTP() + "/announcements.txt");
		String strLine = null;
		while ((strLine = FileIO.file_text_read_line(br)) != null) {
			this.messages.add(strLine);
		}
		FileIO.file_text_close(br);
	}

	public void onEnable()
	{
		System.out.println("KitPvP Enabled");

		File dir = getDataFolder();
		if (!dir.exists()) {
			dir.mkdir();
		}

		File dir2 = new File(getDataFolder().getAbsolutePath() + "/arenas");
		if (!dir2.exists()) {
			dir2.mkdir();
		}

		Util.Initialize(this);
		PermissionInterface.Initialize();

		this.commands.add(new PCommandHelp(this));

		this.commands.add(new PCommandList(this));
		this.commands.add(new PCommandGun(this));
		this.commands.add(new PCommandBuy(this));
		this.commands.add(new PCommandJoin(this));
		this.commands.add(new PCommandGui(this));
		this.commands.add(new PCommandChat(this));
		this.commands.add(new PCommandLeave(this));

		this.commands.add(new PCommandCreate(this));
		this.commands.add(new PCommandReload(this));
		this.commands.add(new PCommandSetPoint(this));
		this.commands.add(new PCommandStop(this));
		this.commands.add(new PCommandAddSpawn(this));

		this.guns_gungame.add("usp45");
		this.guns_gungame.add("m9");
		this.guns_gungame.add("m16");
		this.guns_gungame.add("m4a1");
		this.guns_gungame.add("m1014");
		this.guns_gungame.add("spas12");
		this.guns_gungame.add("l118a");
		this.guns_gungame.add("dragunov");
		this.guns_gungame.add("magnum");
		this.guns_gungame.add("deserteagle");
		this.guns_gungame.add("ak47");
		this.guns_gungame.add("famas");
		this.guns_gungame.add("aa12");
		this.guns_gungame.add("moddel1887");
		this.guns_gungame.add("barret50c");
		this.guns_gungame.add("msr");
		this.guns_gungame.add("executioner");
		this.guns_gungame.add("python");
		this.guns_gungame.add("spas24");
		this.guns_gungame.add("typhoid");
		this.guns_gungame.add("lemantation");
		this.guns_gungame.add("l120_isolator");
		this.guns_gungame.add("skullcrusher");
		this.guns_gungame.add("disassembler");
		this.guns_gungame.add("law");
		this.guns_gungame.add("tomahawk");

		if (!this.loaded)
		{
			PluginManager pm = getServer().getPluginManager();
			pm.registerEvents(this.entityListener, this);
			pm.registerEvents(this.blockListener, this);
			pm.registerEvents(this.playerListener, this);
		}

		loadArenas();
		loadClasses();
		loadWeapons();
		loadClans();

		reloadMessages();

		this.timer = getServer().getScheduler().scheduleSyncRepeatingTask(this, new ArenaUpdater(), 20L, 20L);
		this.playerTimer = getServer().getScheduler().scheduleSyncRepeatingTask(this, new ArenaUpdaterPlayer(), 20L, 1L);

		this.loaded = true;
		this.stopped = false;

		makeDirectories();

		//this.lbsave = new MCWarPlayerSave(this);

		getServer().getScheduler().scheduleSyncDelayedTask(this, new PlayerJoiner(), 20L);

		/*String path = getRoot().getAbsolutePath() + "/lastVote";
		File f = new File(path);
		if (!f.exists()) {
			Date date = new Date();
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("EST"));
			calendar.setTime(date);
			int i = calendar.get(5);
			BufferedWriter wr = FileIO.file_text_open_write(path);
			FileIO.file_text_write_line(wr, Integer.toString(i));
			FileIO.file_text_close(wr);
		}*/
	}

	public void onDisable()
	{
		System.out.println("KitPvP Disabled");
		for (int i = this.activeArena.size() - 1; i >= 0; i--) {
			try {
				((KitArena)this.activeArena.get(i)).stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		clearMemory();
	}

	public void clearMemory() {
		for (int i = 0; i < this.glassThinReplace.size(); i++)
			((Location)this.glassThinReplace.get(i)).getWorld().getBlockAt((Location)this.glassThinReplace.get(i)).setType(Material.THIN_GLASS);
		for (int i = 0; i < this.mapItems.size(); i++) {
			this.mapItems.get(i).getLocation().getWorld().getBlockAt(this.mapItems.get(i).getLocation()).setType(Material.AIR);
		}
		this.glassThinReplace.clear();
		this.mapItems.clear();
		this.guns_gungame.clear();
		this.commands.clear();
		this.codclients.clear();
		this.creatingArena.clear();
		this.classes.clear();
		this.activeArena.clear();
		this.loadedGuns.clear();
		//this.lbsave = null;
		this.messages.clear();
		this.timerunning = 0;
		getServer().getScheduler().cancelTask(this.playerTimer);
		getServer().getScheduler().cancelTask(this.timer);
	}

	public void loadArena(String arenaName) {
		String path = getRoot().getAbsolutePath() + "/arenas";
		File f = new File(path + "/" + arenaName);
		if (f.exists()) {
			KitArena ka = new KitArena(this, arenaName);
			this.activeArena.add(ka);
		}
	}

	public void loadArenas() {
		String path = getRoot().getAbsolutePath() + "/arenas";
		File dir = new File(path);
		String[] children = dir.list();
		if (children != null)
			for (int i = 0; i < children.length; i++) {
				String filename = children[i];
				KitArena ka = new KitArena(this, filename);
				this.activeArena.add(ka);
			}
	}

	public void loadClasses()
	{
		String path = getRoot().getAbsolutePath() + "/classes";
		File dir = new File(path);
		String[] children = dir.list();
		if (children != null)
			for (int i = 0; i < children.length; i++) {
				String filename = children[i];
				KitClass kc = new KitClass(this, new File(path + "/" + filename));
				kc.name = filename;
				this.classes.add(kc);
				System.out.println("LOADED CLASS: " + filename);
			}
	}

	public void loadClans()
	{
		this.clans.clear();
		String path = getMcWar() + "/clans";
		File dir = new File(path);
		String[] children = dir.list();
		if (children != null)
			for (int i = 0; i < children.length; i++) {
				String filename = children[i];
				Clan clan = new Clan(new File(path + "/" + filename));
				clan.load();
				this.clans.add(clan);
			}
	}

	public void loadWeapons() {
		String path = getMcWar() + "/buyable/weapons";
		BufferedReader out = FileIO.file_text_open_read(path);
		boolean reading = true;
		boolean isReadingGun = false;
		KitGun gun = null;
		int line = 0;
		while ((reading) && (line < 512)) {
			line++;
			String read = FileIO.file_text_read_line(out);
			if (read == null) {
				reading = false;
				return;
			}
			if (read.equalsIgnoreCase("::defgun")) {
				isReadingGun = true;
				gun = new KitGun();
			}
			if (read.equalsIgnoreCase("::endgun")) {
				isReadingGun = false;
				this.loadedGuns.add(gun);
			}

			if (isReadingGun) {
				if (read.contains("name")) {
					gun.name = read.substring(read.indexOf("=") + 1);
				}
				if (read.contains("desc"))
					gun.desc = read.substring(read.indexOf("=") + 1);
				if (read.contains("cost"))
					gun.cost = Integer.parseInt(read.substring(read.indexOf("=") + 1));
				if (read.contains("level"))
					gun.level = Integer.parseInt(read.substring(read.indexOf("=") + 1));
				if (read.contains("type"))
					gun.type = Integer.parseInt(read.substring(read.indexOf("=") + 1));
				if (read.contains("slot")) {
					gun.slot = read.substring(read.indexOf("=") + 1);
				}
			}
		}
		FileIO.file_text_close(out);
	}

	public KitArenaCreator getKitArenaCreator(Player p) {
		for (int i = this.creatingArena.size() - 1; i >= 0; i--) {
			if (((KitArenaCreator)this.creatingArena.get(i)).player.getName().equals(p.getName())) {
				return (KitArenaCreator)this.creatingArena.get(i);
			}
		}
		return null;
	}

	public boolean isInArena(Location location) {
		boolean ret = false;
		for (int i = 0; i < this.activeArena.size(); i++) {
			if (((KitArena)this.activeArena.get(i)).field.isInside(location)) {
				ret = true;
			}
		}
		return ret;
	}

	public boolean isInArena(LivingEntity attacker) {
		return getKitPlayer((Player)attacker) != null;
	}

	public boolean isInArena(Block block) {
		return isInArena(block.getLocation());
	}

	public void onQuit(Player pl) {
		for (int i = this.codclients.size() - 1; i >= 0; i--) {
			if (((String)this.codclients.get(i)).equals(pl.getName())) {
				this.codclients.remove(i);
			}
		}
		KitPlayer kp = getKitPlayer(pl);
		if (kp != null)
			leaveArena(kp);
	}

	public void clearPlayer(Player pl)
	{
		if (pl == null)
			return;
		InventoryHelper.clearInventory(pl.getInventory());
		for (PotionEffect effect : pl.getActivePotionEffects())
			pl.removePotionEffect(effect.getType());
	}

	public void onJoin(Player pl) {
		if (isInArena(pl.getLocation()))
			clearPlayer(pl);
	}

	public KitPlayer getKitPlayer(Player player)
	{
		for (int i = this.activeArena.size() - 1; i >= 0; i--) {
			KitPlayer find = ((KitArena)this.activeArena.get(i)).getPlayer(player);
			if (find != null) {
				return find;
			}
		}
		return null;
	}

	public KitPlayer getKitPlayer(String string) {
		for (int i = this.activeArena.size() - 1; i >= 0; i--) {
			KitPlayer find = ((KitArena)this.activeArena.get(i)).getPlayer(string);
			if (find != null) {
				return find;
			}
		}
		return null;
	}

	public ArrayList<KitPlayer> getKitPlayers() {
		ArrayList players = new ArrayList();
		for (int i = this.activeArena.size() - 1; i >= 0; i--) {
			ArrayList myplayers = ((KitArena)this.activeArena.get(i)).getPlayers();
			for (int ii = 0; ii < myplayers.size(); ii++) {
				players.add((KitPlayer)myplayers.get(ii));
			}
		}
		return players;
	}

	public KitClass getArenaClass(String line1) {
		for (int i = 0; i < this.classes.size(); i++) {
			if (((KitClass)this.classes.get(i)).name.equals(line1)) {
				return (KitClass)this.classes.get(i);
			}
		}
		return null;
	}

	public KitGun getGun(String name)
	{
		for (int i = 0; i < this.loadedGuns.size(); i++) {
			if (((KitGun)this.loadedGuns.get(i)).name.equals(name)) {
				return (KitGun)this.loadedGuns.get(i);
			}
		}
		return null;
	}

	public KitGun getGunNameFromItem(int id) {
		for (int i = 0; i < this.loadedGuns.size(); i++) {
			if (((KitGun)this.loadedGuns.get(i)).type == id) {
				return (KitGun)this.loadedGuns.get(i);
			}
		}
		return null;
	}

	public KitArena getKitArena(String arenaName) {
		for (int i = this.activeArena.size() - 1; i >= 0; i--) {
			if (((KitArena)this.activeArena.get(i)).name.toLowerCase().equals(arenaName.toLowerCase())) {
				return (KitArena)this.activeArena.get(i);
			}
		}
		return null;
	}

	public KitClass getKitClass(String cls) {
		for (int i = 0; i < this.classes.size(); i++) {
			if (((KitClass)this.classes.get(i)).name.equals(cls)) {
				return (KitClass)this.classes.get(i);
			}
		}
		return null;
	}

	public void leaveArena(KitPlayer kp) {
		kp.profile.save(kp);
		for (int i = 0; i < this.activeArena.size(); i++) {
			KitArena arena = (KitArena)this.activeArena.get(i);
			arena.leaveArena(kp);
		}
		kp.CLEARPLAYER();
	}

	public KitPlayer joinArena(Player player, KitClass kclass) {
		if (this.stopped) {
			return null;
		}
		InventoryHelper.clearInventory(player.getInventory());
		KitArena k = getFirstKitArena("lobby");
		if (k != null) {
			if (!k.isFull()) {
				return k.join(player);
			}
			player.sendMessage("FULL " + k.players.size());
			return null;
		}

		player.sendMessage("Could not join a kit arena, perhaps there is no free space?");
		return null;
	}

	public ChatColor getPlayerTagColor(Player player) {
		if (PermissionInterface.hasPermission(player, "mcwar.admin"))
			return ChatColor.GOLD;
		if (PermissionInterface.hasPermission(player, "mps.supermod"))
			return ChatColor.DARK_PURPLE;
		if (PermissionInterface.hasPermission(player, "mcwar.mod"))
			return ChatColor.LIGHT_PURPLE;
		if (PermissionInterface.hasPermission(player, "mcwar.mvp"))
			return ChatColor.AQUA;
		if (PermissionInterface.hasPermission(player, "mcwar.vip"))
			return ChatColor.GREEN;
		KitPlayer kp = getKitPlayer(player);
		if ((kp != null) && 
				(kp.profile.level > 30)) {
			return ChatColor.YELLOW;
		}

		return ChatColor.WHITE;
	}

	public void sayMessage(Player player, String say) {
		try {
			String msg = "<" + getKitPlayerName(player) + "> " + say;
			broadcastMessage(player, msg);
		} catch (Exception localException) {
		}
	}

	public void sayNonWarMessage(Player player, String message) {
		try {
			String msg = "<" + player.getDisplayName() + "> " + message;
			List players = Util.Who();
			for (int i = players.size() - 1; i >= 0; i--) {
				KitPlayer kp = getKitPlayer((Player)players.get(i));
				if (kp == null)
					((Player)players.get(i)).sendMessage(msg);
			}
		}
		catch (Exception localException) {
		}
	}

	public String getKitPlayerName(Player player) {
		KitPlayer kp = getKitPlayer(player);
		if (kp != null) {
			String prefix = "";
			Clan clan = kp.getClan();
			if ((clan != null) && (clan.getOwner().equalsIgnoreCase(player.getName()))) {
				prefix = "" +  ChatColor.UNDERLINE;
			}

			return getPlayerTagColor(player) + prefix + kp.getTag() + ChatColor.RESET + ChatColor.WHITE;
		}
		return player.getName();
	}

	public void broadcastMessage(Player from, String msg) {
		try {
			for (int i = 0; i < this.activeArena.size(); i++)
				for (int ii = ((KitArena)this.activeArena.get(i)).players.size() - 1; ii >= 0; ii--)
					((KitPlayer)((KitArena)this.activeArena.get(i)).players.get(ii)).sayMessage(from, msg);
		}
		catch (Exception localException)
		{
		}
	}

	public void giveMessage(Player player, String msg)
	{
		KitPlayer kp = getKitPlayer(player);
		if (kp != null)
			kp.sayMessage(player, msg);
		else
			player.sendMessage(msg);
	}

	public void giveMessage(CommandSender sender, String msg)
	{
		if ((sender instanceof Player))
			giveMessage((Player)sender, msg);
	}

	private String getPlayerName(CommandSender sender)
	{
		if ((sender instanceof Player)) {
			return ((Player)sender).getName();
		}
		return "Console";
	}

	public KitProfile getProfile(KitPlayer kitPlayer) {
		String path = getUsers() + "/" + kitPlayer.name.toLowerCase() + ".mcw";
		File f = new File(path);
		KitProfile k = null;
		if (f.exists()) {
			k = readProfile(kitPlayer);
		}else{
			blankProfile(kitPlayer.name);
			k = readProfile(kitPlayer);
		}
		if (k != null) {
			return k;
		}
		return null;
	}
	
	public KitProfile loadProfile(String str) {
		String path = getUsers() + "/" + str.toLowerCase() + ".mcw";
		File f = new File(path);
		KitProfile k = null;
		if (f.exists()) {
			k = readProfile(str);
		}
		if (k != null) {
			return k;
		}
		return null;
	}

	private KitProfile readProfile0(String player)
	{
		String path = getUsers() + "/" + player.toLowerCase() + ".mcw";
		File f = new File(path);
		if (f.exists()) {
			BufferedReader in = FileIO.file_text_open_read(path);
			ArrayList<String> compute = new ArrayList<String>();
			try{
				boolean can = true;
				int line = 0;
				while (can && line < 512) {
					line++;
					String str = FileIO.file_text_read_line(in);
					if (str == null) {
						can = false;
					}else{
						compute.add(str);
					}
				}
			}catch(Exception e) {
				//
			}
			FileIO.file_text_close(in);
			KitProfile kp = new KitProfile(this, player, compute);
			kp.execute(this);
			return kp;
		}
		return null;
	}
	
	private KitProfile readProfile(String player) {
		KitProfile profile = readProfile0(player);
		if (profile == null)
		{
			System.out.println("Tried to read profile of nonexistant player, attempting to create profile...");
			this.blankProfile(player);
			profile = readProfile0(player);
		}
		return profile;
	}
	
	private KitProfile readProfile(KitPlayer p) {
		String path = getUsers() + "/" + p.name.toLowerCase() + ".mcw";
		File f = new File(path);
		if (f.exists()) {
			BufferedReader in = FileIO.file_text_open_read(path);
			ArrayList<String> compute = new ArrayList<String>();
			boolean can = true;
			int line = 0;
			try{
				while (can && line < 512) {
					line++;
					String str = FileIO.file_text_read_line(in);
					if (str == null) {
						can = false;
					}else{
						compute.add(str);
					}
				}
			}catch(Exception e) {
				//
			}
			FileIO.file_text_close(in);
			KitProfile kp = new KitProfile(this, p.player, compute);
			kp.execute(this);
			return kp;
		}else{
			p.player.kickPlayer("Couldn't create user profile, please contact server Admins!");
		}
		return null;
	}
	
	private void blankProfile(String p) {
		String path = getUsers() + "/" + p.toLowerCase() + ".mcw";
	    BufferedWriter out = FileIO.file_text_open_write(path);
	    try{
		    FileIO.file_text_write_line(out, "xp=0");
		    FileIO.file_text_write_line(out, "level=1");
		    FileIO.file_text_write_line(out, "credits=0");
		    FileIO.file_text_write_line(out, "--classes");
		    try{
			    ArrayList<String> dump = classes.get(0).dumpClass();
			    for (int i = 0; i < dump.size(); i++) {
			    	FileIO.file_text_write_line(out, dump.get(i));
			    }
		    }catch(Exception e) {
		    	e.printStackTrace();
		    }
		    FileIO.file_text_write_line(out, "::defguns");
		    FileIO.file_text_write_line(out, "m16");
		    FileIO.file_text_write_line(out, "usp45");
		    FileIO.file_text_write_line(out, "m1014");
		    FileIO.file_text_write_line(out, "l118a");
		    FileIO.file_text_write_line(out, "::endguns");
		}catch(Exception e) {
			//
		}
	    FileIO.file_text_close(out);
	}
	
	public int getGunAmmo(String str) {
		Plugin p = Bukkit.getPluginManager().getPlugin("PVPGunPlus");
		if (p != null) {
			PVPGunPlus pv = (PVPGunPlus)p;
			if (pv != null) {
				Gun g = pv.getGun(str);
				return g.getAmmoMaterial().getId();
			}
		}
		return 0;
	}

	public KitArena getRandomKitArena(KitArena excluding, KitArena last, int amtPlayers)
	{
		ArrayList list = new ArrayList();
		for (int i = 0; i < this.activeArena.size(); i++) {
			KitArena ret = (KitArena)this.activeArena.get(i);
			try {
				if ((!ret.equals(excluding)) && 
						(!ret.equals(last)) && 
						(amtPlayers <= ret.maxPlayers) && (amtPlayers >= ret.minPlayers)) {
					list.add((KitArena)this.activeArena.get(i));
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (list.size() > 0) {
			int pick = new Random().nextInt(list.size());
			KitArena ret = (KitArena)list.get(pick);
			list.clear();
			return ret;
		}
		return getRandomKitArena(excluding);
	}

	public KitArena getRandomKitArena(KitArena excluding)
	{
		KitArena ret = excluding;
		int rep = 0;
		int max = 128;
		boolean loop = true;
		while ((ret.equals(excluding)) && (loop)) {
			if (rep >= max)
				loop = false;
			int i = this.activeArena.size();
			ret = (KitArena)this.activeArena.get(this.r.nextInt(i));
			rep++;
		}
		if (!loop) {
			ret = getRandomKitArena();
		}
		return ret;
	}

	public KitArena getRandomKitArena() {
		int i = this.activeArena.size();
		return (KitArena)this.activeArena.get(this.r.nextInt(i));
	}

	public KitArena getFirstKitArena(String string) {
		for (int i = 0; i < this.activeArena.size(); i++) {
			if (((KitArena)this.activeArena.get(i)).type.equals(string)) {
				return (KitArena)this.activeArena.get(i);
			}
		}
		return null;
	}

	public int getAmmo(Player player, String string) {
		int amt = 64;
		if (PermissionInterface.hasPermission(player, "mcwar.vip")) {
			amt = 96;
		}
		if (PermissionInterface.hasPermission(player, "mcwar.mvp")) {
			amt = 128;
		}
		if (string.equals("primary"))
			return amt;
		return amt / 2;
	}

	public int getKillXP(Player player)
	{
		int ret = 25;
		if (PermissionInterface.hasPermission(player, "mcwar.doublexp")) {
			ret += 25;
		}
		if (PermissionInterface.hasPermission(player, "mcwar.vip")) {
			ret += 10;
		}
		if (PermissionInterface.hasPermission(player, "mcwar.mvp")) {
			ret += 20;
		}
		return ret;
	}

	public int getKillCredits(Player player) {
		if (PermissionInterface.hasPermission(player, "mcwar.mvp")) {
			return 3;
		}
		if (PermissionInterface.hasPermission(player, "mcwar.vip")) {
			return 2;
		}
		return 1;
	}

	public void stopMakingArena(Player player) {
		for (int i = this.creatingArena.size() - 1; i >= 0; i--)
			if (((KitArenaCreator)this.creatingArena.get(i)).player.getName().equals(player.getName()))
				this.creatingArena.remove(i);
	}

	public void setPerk(Player player, String line2)
	{
		KitPlayer kp = getKitPlayer(player);
		if (kp != null) {
			boolean has = false;
			for (int i = 0; i < kp.profile.perks.size(); i++) {
				if (((String)kp.profile.perks.get(i)).equals(line2)) {
					kp.profile.perk = line2;
					has = true;
					giveMessage(player, "Perk set to: " + ChatColor.YELLOW + line2);
				}
			}
			if (!has)
				giveMessage(player, "You do not have this class!");
		}
	}

	public ArrayList<Perk> getPerks(KitPlayer k)
	{
		ArrayList perks = new ArrayList();
		for (int i = 0; i < k.profile.perks.size(); i++) {
			Perk p = getPerk(k.player, (String)k.profile.perks.get(i));
			if (p != null) {
				perks.add(p);
			}
		}
		return perks;
	}

	public ArrayList<Perk> getActivePerks(KitPlayer k) {
		ArrayList ret = new ArrayList();
		ArrayList perks = getPerks(k);
		for (int i = 0; i < perks.size(); i++) {
			if (((Perk)perks.get(i)).name.toLowerCase().equals(k.profile.perk.toLowerCase())) {
				ret.add((Perk)perks.get(i));
			}
		}
		return ret;
	}

	public Perk getPerk(Player p, String s) {
		if (s.toLowerCase().equals("marathon"))
			return new PerkMarathon(p);
		if (s.toLowerCase().equals("scavenger"))
			return new PerkScavenger(p);
		if (s.toLowerCase().equals("speed"))
			return new PerkSpeed(p);
		if (s.toLowerCase().equals("stoppingpower"))
			return new PerkStoppingPower(p);
		if (s.toLowerCase().equals("juggernaut"))
			return new PerkJuggernaut(p);
		if (s.toLowerCase().equals("sleightofhand"))
			return new PerkSleightofhand(p);
		if (s.toLowerCase().equals("martyrdom"))
			return new PerkMartyrdom(p);
		if (s.toLowerCase().equals("hardline"))
			return new PerkHardline(p);
		if (s.toLowerCase().equals("flakjacket"))
			return new PerkFlakjacket(p);
		return null;
	}

	/*public void newDay() {
		String path = getFTP() + "/voted";
		File dir = new File(path);
		String[] children = dir.list();
		if (children != null)
			for (int i = 0; i < children.length; i++) {
				String filename = children[i];
				File f = new File(path + "/" + filename);
				if (f.exists())
					f.delete();
			}
	}*/

	public Clan getClanByTag(String clanname)
	{
		if (clanname == null)
			return null;
		for (int i = this.clans.size() - 1; i >= 0; i--) {
			if ((this.clans.get(i) != null) && (((Clan)this.clans.get(i)).getName() != null) && 
					(((Clan)this.clans.get(i)).getName().equalsIgnoreCase(clanname))) {
				return (Clan)this.clans.get(i);
			}
		}

		return null;
	}

	public Clan getClanByFile(String clanname) {
		if (clanname == null)
			return null;
		for (int i = this.clans.size() - 1; i >= 0; i--) {
			if ((this.clans.get(i) != null) && (((Clan)this.clans.get(i)).getName() != null) && 
					(((Clan)this.clans.get(i)).getFilename().equalsIgnoreCase(clanname))) {
				return (Clan)this.clans.get(i);
			}
		}

		return null;
	}

	public void joinClan(KitPlayer kp, Clan clan) {
		clan.join(kp.player.getName());
		kp.setClan(clan);
		for (int i = 0; i < this.activeArena.size(); i++) {
			KitArena arena = (KitArena)this.activeArena.get(i);
			List players = arena.getPlayers();
			for (int ii = players.size() - 1; ii >= 0; ii--) {
				KitPlayer player = (KitPlayer)players.get(ii);
				if ((!player.name.equals(kp.name)) && 
						(player.getClan().equals(kp.getClan()))) {
					player.sayMessage(null, kp.name + ChatColor.GREEN + " has joined your clan!");
				}
			}
		}

		kp.sayMessage(null, "JOINED CLAN: " + clan.getName());
	}

	public Clan createClan(String name) {
		Clan c = new Clan(name);
		this.clans.add(c);
		return c;
	}

	public Clan getPlayersClan(String playername) {
		for (int i = this.clans.size() - 1; i >= 0; i--) {
			if (((Clan)this.clans.get(i)).hasMember(playername)) {
				return (Clan)this.clans.get(i);
			}
		}
		return null;
	}

	public boolean hasVoted(Player player) {
		File file = new File(getFTP() + "/votes/" + player.getName().toLowerCase() + ".txt");
		return file.isFile() && System.currentTimeMillis() - file.lastModified() < 24L * 60L * 60L * 1000L;
	}

	public boolean canDamagePlayer(Player attacker, Player defender) {
		if ((!isInArena(attacker)) && (isInArena(defender))) {
			return false;
		}

		if ((isInArena(attacker)) && (isInArena(defender))) {
			boolean isOnTeam = false;
			KitPlayer shootKP = getKitPlayer(attacker);
			KitPlayer defendKP = getKitPlayer(defender);
			KitArena ka = shootKP.arena;

			if (ka.killType.equals("ffa")) {
				isOnTeam = false;
			}
			else if (shootKP.team == defendKP.team) {
				isOnTeam = true;
			}

			if (ka.type.equals("lobby")) {
				isOnTeam = true;
			}

			return !isOnTeam;
		}
		return false;
	}

	public void onStartLobby()
	{
		for (int i = 0; i < this.glassThinReplace.size(); i++) {
			((Location)this.glassThinReplace.get(i)).getWorld().getBlockAt((Location)this.glassThinReplace.get(i)).setType(Material.THIN_GLASS);
		}
		
		for (int i = 0; i < this.mapItems.size(); i++) {
			this.mapItems.get(i).getLocation().getWorld().getBlockAt(this.mapItems.get(i).getLocation()).setType(Material.AIR);
		}
		
		this.glassThinReplace.clear();
		this.mapItems.clear();
		reloadMessages();
		updateClans();
	}

	public int getServerNumber() {
		return getServer().getPort() - 25565 + 1;
	}

	public void addCodClient(String name) {
		this.codclients.add(name);
	}

	public boolean getCodClient(String name) {
		for (int i = this.codclients.size() - 1; i >= 0; i--) {
			if (((String)this.codclients.get(i)).equals(name)) {
				return true;
			}
		}
		return false;
	}

	public void updateClans()
	{
		for (int i = this.clans.size() - 1; i >= 0; i--) {
			((Clan)this.clans.get(i)).update(false);
		}
		loadClans();
	}

	public List<Clan> getClans() {
		return this.clans;
	}

	public class ArenaUpdater
	implements Runnable
	{
		public ArenaUpdater()
		{
		}

		public void run()
		{
			Opexmain.getPlugin().setSharedPluginFolder(getFTP() + "/permissions");
			secondsAlive++;
			if (secondsAlive % 10 == 0) {
				Plugin buyc = Bukkit.getPluginManager().getPlugin("Buycraft");
				if (buyc != null) {
					Bukkit.dispatchCommand(getServer().getConsoleSender(), "buycraft forcecheck");
				}
			}
			if (secondsAlive > (60 * 60) * 16) {
				Player[] players = Bukkit.getOnlinePlayers();
				for (int i = players.length - 1; i >= 0; i--) {
					players[i].kickPlayer(ChatColor.GREEN + "The server is restarting, please rejoin");
				}
				Bukkit.shutdown();
			}
			if (KitPvP.this.currentMap == null)
				return;
			KitPvP.this.announceTimer -= 1;
			KitPvP.this.timerunning += 1;
			for (int i = KitPvP.this.activeArena.size() - 1; i >= 0; i--) {
				((KitArena)KitPvP.this.activeArena.get(i)).tick();
			}
			if (KitPvP.this.announceTimer < 0) {
				if (KitPvP.this.messages.size() == 0)
					return;
				if ((KitPvP.this.messages.get(KitPvP.this.lastAnnouncement) != null) && 
						(((String)KitPvP.this.messages.get(KitPvP.this.lastAnnouncement)).length() > 4)) {
					KitPvP.this.broadcastMessage(null, ChatColor.AQUA + "[BROADCAST]");
					KitPvP.this.broadcastMessage(null, ChatColor.GREEN + " " + (String)KitPvP.this.messages.get(KitPvP.this.lastAnnouncement));
				}

				KitPvP.this.announceTimer = 8;
				KitPvP.this.lastAnnouncement += 1;
				if (KitPvP.this.lastAnnouncement + 1 > KitPvP.this.messages.size())
					KitPvP.this.lastAnnouncement = 0;
			}
			Player[] g = Bukkit.getServer().getOnlinePlayers();
			for (int i = 0; i < g.length; i++) {
				if (g[i] != null) {
					KitPlayer kp = KitPvP.this.getKitPlayer(g[i]);
					if ((kp == null) && (!g[i].isOp())) {
						InventoryHelper.clearInventory(g[i].getInventory());
					}
				}
			}
		}
	}
	public class ArenaUpdaterPlayer implements Runnable {
		public ArenaUpdaterPlayer() {
		}
		public void run() { for (int i = KitPvP.this.activeArena.size() - 1; i >= 0; i--)
			((KitArena)KitPvP.this.activeArena.get(i)).playerTick();  } 
	}

	public class PlayerJoiner implements Runnable {
		public PlayerJoiner() {
		}

		public void run() {
			for (int i = 0; i < KitPvP.this.activeArena.size(); i++) {
				if (((KitArena)KitPvP.this.activeArena.get(i)).type.equals("lobby")) {
					((KitArena)KitPvP.this.activeArena.get(i)).onStart(null);
					((KitArena)KitPvP.this.activeArena.get(i)).started = true;
				}
			}
			Player[] g = Bukkit.getServer().getOnlinePlayers();
			for (int i = 0; i < g.length; i++)
				KitPvP.this.joinArena(g[i], null);
		}
	}
	
	public void damagePlayer(Player player, int damage, DamageType dmgType, Player damager) {
		KitPlayer kp = this.getKitPlayer(player);
		KitPlayer kp2 = this.getKitPlayer(damager);
		if (kp != null && kp2 != null) {
			kp.onDamage(damage, dmgType, kp2);
		}else{
			player.damage(damage, damager);
		}
	}
	
	public List<MapItem> getMapItems() {
		return this.mapItems;
	}

	public void removeMapEntity(MapItem mapItem) {
		for (int i = this.mapItems.size() - 1; i >= 0; i--) {
			if (mapItems.get(i).equals(mapItem)) {
				mapItems.get(i).getLocation().getWorld().getBlockAt(this.mapItems.get(i).getLocation()).setType(Material.AIR);
				mapItems.remove(i);
			}
		}
	}
}