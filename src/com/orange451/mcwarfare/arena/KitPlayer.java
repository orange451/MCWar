package com.orange451.mcwarfare.arena;

import com.orange451.mcwarfare.DamageType;
import com.orange451.mcwarfare.InventoryHelper;
import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.ParticleEffects;
import com.orange451.mcwarfare.Util;
import com.orange451.mcwarfare.arena.KitArena.GameModeModifier;
import com.orange451.mcwarfare.arena.KitArena.Teams;
import com.orange451.mcwarfare.arena.kits.KitClass;
import com.orange451.mcwarfare.arena.kits.Perk;
import com.orange451.opex.permissions.PermissionInterface;
import com.orange451.pvpgunplus.PVPGunPlus;
import com.orange451.pvpgunplus.gun.GunPlayer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.EnumClientCommand;
import net.minecraft.server.v1_7_R1.PacketPlayInClientCommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.kitteh.tag.TagAPI;
import org.mcsg.double0negative.tabapi.TabAPI;

public class KitPlayer {
	public int myclass = 0;
	public KitArena.Teams team;
	public int kills;
	public int killStreak = 0;
	public int deaths;
	public int timeOffline = 0;
	public int timedead = 0;
	public int alive;
	public int lives;
	public int killTicks;
	public int gungameLevel;
	public boolean dead = false;
	public boolean displayGUI = true;
	public boolean receiveChat = true;
	public boolean isSpawning = false;
	public boolean hasCodClient = false;
	public KitProfile profile;
	public KitClass kclass;
	public KitArena arena;
	public Player player;
	public String name;
	public String[] chat = new String[6];
	public Location returnto;
	public Location lastLoc;
	public KitPvP plugin;
	public ArrayList<KitItem> boughtItems = new ArrayList();
	public ArrayList<Perk> perks = new ArrayList();
	private int afkTicks;
	private SpecialMessage specialMessage;
	private Clan clan;
	private int clanInviteTimer;
	public String clanInviteTo = "";
	public int lastSpawn = 0;
	private Scoreboard board;
	private Objective scoreboard;
	public KitPlayer lastDamager;
	public Location spawnLoc;

	public void start(KitPvP plugin, KitArena arena, Player p, KitArena.Teams team)
	{
		this.plugin = plugin;
		this.player = p;
		this.name = p.getName();
		this.team = team;
		this.arena = arena;
		this.returnto = p.getLocation().clone();

		for (int i = 0; i < this.chat.length; i++) {
			this.chat[i] = "";
		}
		this.profile = arena.plugin.getProfile(this);
		if (this.profile == null) {
			p.kickPlayer("COULD NOT LOCATE PLAYER PROFILE!, PLEASE CONTACT ADMINS");
			return;
		}

		if (this.profile.classes.size() > 0) {
			this.kclass = ((KitClass)this.profile.classes.get(this.profile.myclass));
		} else {
			System.out.println(this.profile.classes);
			this.player.kickPlayer("CORRUPTED PROFILE " + ChatColor.GRAY + "visit \n" + ChatColor.BLUE + "www.mcbrawl.com");
			System.out.println("[!!!] " + this.player.getName() + " has a corrupted profile!");
		}
		this.profile.xpn = getXpto(this.profile.level);

		this.perks = plugin.getActivePerks(this);
		this.clan = plugin.getPlayersClan(this.name);

		TabAPI.setPriority(plugin, this.player, 2);

		this.player.getInventory().clear();

		ScoreboardManager manager = Bukkit.getScoreboardManager();
		this.board = manager.getNewScoreboard();

		this.scoreboard = this.board.registerNewObjective("scoreboard", "dummy");
		this.scoreboard.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.scoreboard.setDisplayName("Scoreboard");

		this.player.setScoreboard(this.board);
	}

	public void updateTabList(Player p){ //update the tab for a player
		int hei = TabAPI.getVertSize()-1;
		
		TabAPI.clearTab(player);
		
		int k = kills;
		int d = deaths;
		double kdr = getKDR();
		if (arena.type.equals("lobby")) {
			k = profile.kills;
			d = profile.deaths;
			kdr = (Math.round( ((double)k) / ((double)d) * 100.0))/100d;
		}
		TabAPI.setTabString(plugin, p, 0, 0, "" + ChatColor.DARK_RED + "KILLS " + ChatColor.AQUA + Integer.toString(k));
		TabAPI.setTabString(plugin, p, 0, 1, "" + ChatColor.DARK_RED +"DEATH " + ChatColor.AQUA + Integer.toString(d));
		TabAPI.setTabString(plugin, p, 0, 2, "" + ChatColor.DARK_RED + "KDR " + ChatColor.AQUA + Double.toString(kdr));
		
		TabAPI.setTabString(plugin, p, 1, 0, "" + ChatColor.GOLD + "LEVEL " + ChatColor.YELLOW + Integer.toString(profile.level));
		TabAPI.setTabString(plugin, p, 1, 1, drawXp());
		TabAPI.setTabString(plugin, p, 1, 2, "" + ChatColor.DARK_GREEN + "$$ " + ChatColor.GREEN + Integer.toString(profile.credits));
		
		
		TabAPI.setTabString(plugin, p, 2, 0, ChatColor.UNDERLINE + "TEAM " + getTeamColor() + getTeamName());
		TabAPI.setTabString(plugin, p, 2, 1, ChatColor.GREEN + "killstreak " + Integer.toString(this.killStreak));
		TabAPI.setTabString(plugin, p, 2, 2, arena.getLeader());
		
		int bottom = hei;
		
		if (arena.type.equals("tdm")) {
			displayTeam(Teams.BLUE, 0, 5, bottom, true);
			displayTeam(Teams.RED,  2, 5, bottom, true);
		}else{
			displayTeam(Teams.ALL, 0, 5, bottom, false);
		}

		TabAPI.updatePlayer(p);
	}
	
	public void displayTeam(Teams team, int startX, int startY, int maxY, boolean collumn) {
		ArrayList<KitPlayer> players1 = arena.getPlayersOnTeam(team);
		TabAPI.setTabString(plugin, player, startY-1, startX, "" + ChatColor.GRAY + ChatColor.UNDERLINE + "players " + ChatColor.YELLOW + Integer.toString(players1.size()));
		for (int i = 0; i < players1.size(); i++) {
			if (collumn) {
				int posy = startY + i;
				if (posy < maxY)
					TabAPI.setTabString(plugin, player, posy, startX, players1.get(i).getTeamColor() + players1.get(i).name);
			}else{
				int posy = startY + (i/3);
				int posx = (i % 3);
				if (posy < maxY)
					TabAPI.setTabString(plugin, player, posy, posx, players1.get(i).getTeamColor() + players1.get(i).name);						
			}
		}
	}

	public void onClickItem(InventoryClickEvent event)
	{
		try {
			ItemStack cursor = event.getCurrentItem();
			if (cursor == null)
				return;
			Material mat = cursor.getType();
			if (mat != null) {
				ItemMeta meta = cursor.getItemMeta();
				if (meta != null) {
					String dname = meta.getDisplayName();
					if (dname == null)
					{
						return;
					}
					if (this.player.getOpenInventory().getTopInventory().getTitle().contains("Grenade") || 
							this.player.getOpenInventory().getTopInventory().getTitle().toLowerCase().contains("menu") || 
							this.player.getOpenInventory().getTopInventory().getTitle().toLowerCase().contains("class")) {
						event.setCancelled(true);
					}
					
					if (dname.contains("Back")) {
						event.setCancelled(true);
						if (this.player.getOpenInventory().getTopInventory().getTitle().contains("Gun Store") || this.player.getOpenInventory().getTopInventory().getTitle().contains("Class Loadout"))
							openGunMenu();
						else
							openClassSelectMenu();
						return;
					}
					
					if (this.player.getOpenInventory().getTopInventory().getTitle().equals("Class Loadout")) { //inside the classloadout menu
						if (dname.contains("PRIMARY"))
							this.openGunSelectMenu("Primary");
						if (dname.contains("SECONDARY"))
							this.openGunSelectMenu("Secondary");
						if (dname.contains("LETHAL"))
							this.openGrenadeMenu();
						if (dname.contains("TACTICAL"))
							this.openGrenadeMenu();
						if (dname.contains("PERK"))
							this.openPerkMenu();
						
						return;
					}
					
					if (dname.contains("PERK")) {
						event.setCancelled(true);
						String perk = dname.replaceAll("PERK ", "").toLowerCase();
						perk = ChatColor.stripColor(perk);
						if (this.profile.hasPerk(perk)) {
							this.profile.perk = perk;
							this.player.playSound(this.player.getLocation(), Sound.ZOMBIE_METAL, 1.0F, 1.0F);
							this.player.playSound(this.player.getLocation(), Sound.SKELETON_HURT, 1.0F, 1.0F);
							this.arena.plugin.giveMessage(this.player, "Perk set to: " + ChatColor.YELLOW + perk);
						} else {
							this.player.playSound(this.player.getLocation(), Sound.BLAZE_DEATH, 1.0F, 1.0F);
							this.arena.plugin.giveMessage(this.player, "You do not have the perk: " + ChatColor.YELLOW + perk);
						}
						openClassSelectMenu();
					}
					if ((dname.contains("TACTICAL")) || (dname.contains("LETHAL"))) {
						String grenname = dname;
						grenname = grenname.replaceAll("TACTICAL", "");
						grenname = grenname.replaceAll("LETHAL", "");
						grenname = ChatColor.stripColor(grenname);
						grenname = grenname.replace(" ", "");
	
						this.player.playSound(this.player.getLocation(), Sound.ZOMBIE_METAL, 1.0F, 1.0F);
						this.player.playSound(this.player.getLocation(), Sound.SKELETON_HURT, 1.0F, 1.0F);
	
						if (dname.contains("LETHAL")) {
							this.profile.lethal = grenname;
							sayMessage(null, ChatColor.BLUE + "Lethal now set to: " + ChatColor.GRAY + grenname);
						}
						if (dname.contains("TACTICAL")) {
							this.profile.tactical = grenname;
							sayMessage(null, ChatColor.BLUE + "Tactical now set to: " + ChatColor.GRAY + grenname);
						}
						openClassSelectMenu();
					}
					if (dname.contains("GUN")) {
						ArrayList lore = (ArrayList)meta.getLore();
						String gunname = ChatColor.stripColor((String)lore.get(0));
						KitGun kg = this.plugin.getGun(gunname);
						if (this.player.getOpenInventory().getTopInventory().getTitle().contains("Selection")) {
							event.setCancelled(true);
							this.player.playSound(this.player.getLocation(), Sound.ZOMBIE_METAL, 1.0F, 1.0F);
							this.player.playSound(this.player.getLocation(), Sound.SKELETON_HURT, 1.0F, 1.0F);
							if (kg.slot.equals("primary")) {
								((KitClass)this.profile.classes.get(this.profile.myclass)).primary = gunname;
								((KitClass)this.profile.classes.get(this.profile.myclass)).update();
								sayMessage(null, "set primary to: " + ChatColor.YELLOW + gunname);
							}
	
							if (kg.slot.equals("secondary")) {
								((KitClass)this.profile.classes.get(this.profile.myclass)).secondary = gunname;
								((KitClass)this.profile.classes.get(this.profile.myclass)).update();
								sayMessage(null, "set secondary to: " + ChatColor.YELLOW + gunname);
							}
							openClassSelectMenu();
						} else {
							event.setCancelled(true);
							int cost = kg.cost;
							if (this.profile.credits >= cost) {
								this.profile.credits -= cost;
								sayMessage(null, "bought: " + ChatColor.YELLOW + gunname);
								this.profile.boughtGuns.add(gunname);
								this.profile.save(this);
								this.player.playSound(this.player.getLocation(), Sound.IRONGOLEM_HIT, 1.0F, 4.0F);
								openBuyMenu();
								return;
							}
							this.player.playSound(this.player.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);
							sayMessage(null, "This gun costs: " + ChatColor.YELLOW + cost + ChatColor.WHITE + " credits!");
							return;
						}
					}
	
					if ((mat.equals(Material.CHEST)) && (dname.contains("Class"))) {
						event.setCancelled(true);
						openClassSelectMenu();
					}
					if ((mat.equals(Material.ANVIL)) && (dname.contains("Guns"))) {
						event.setCancelled(true);
						openBuyMenu();
					}
					if ((mat.equals(Material.SLIME_BALL)) && (dname.contains("Manage"))) {
						event.setCancelled(true);
						openGrenadeMenu();
					}
					if ((mat.equals(Material.GOLDEN_APPLE)) && (dname.contains("Perks"))) {
						event.setCancelled(true);
						openPerkMenu();
					}
				}
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	private void openInventory(final Inventory retInv) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
		{
			public void run() {
				KitPlayer.this.player.openInventory(retInv);
				KitPlayer.this.player.updateInventory();
			}
		}
		, 1L);
	}
	
	public void openClassSelectMenu() {
		this.player.playSound(this.player.getLocation(), Sound.BAT_TAKEOFF, 1.0F, 1.0F);
		Inventory retInv = Bukkit.createInventory(this.player, 45, "Class Loadout");
		retInv.addItem(new ItemStack[] { namedItemStack(Material.LAVA_BUCKET, "Back", ChatColor.GRAY, "Go back") });
		
		retInv.setItem(2, namedItemStack(Material.MAP, ChatColor.RED + "Class Name", ChatColor.GRAY, null));
		retInv.setItem(3, namedItemStack(Material.WATER_BUCKET, ChatColor.RED + "Primary", ChatColor.GRAY, "Main Weapon of/this class"));		
		retInv.setItem(4, namedItemStack(Material.WATER_BUCKET, ChatColor.RED + "Secondary", ChatColor.GRAY, "Secondary Weapon of/this class"));
		retInv.setItem(5, namedItemStack(Material.WATER_BUCKET, ChatColor.RED + "Lethal", ChatColor.GRAY, "Lethal grenade/of this class"));
		retInv.setItem(6, namedItemStack(Material.WATER_BUCKET, ChatColor.RED + "Tactical", ChatColor.GRAY, "Tactical grenade/of this class"));
		retInv.setItem(7, namedItemStack(Material.WATER_BUCKET, ChatColor.RED + "Perk", ChatColor.GRAY, "Perk for/this class"));
		retInv.setItem(44, namedItemStack(Material.MAP, ChatColor.RED + "INFORMATION", ChatColor.GRAY, "Click the items under/the arrows to change/them in your class"));
		
		//CLASS 1
		ItemStack lethal = getItemStack_Lethal();
		ItemStack tactical = getItemStack_Tactical();
		
		retInv.setItem(11, namedItemStack(Material.EMPTY_MAP, "" + ChatColor.ITALIC + ChatColor.GRAY + "CLASS 1", ChatColor.GRAY, ""));
		retInv.setItem(12, namedItemStack(Material.getMaterial(profile.classes.get(0).weapon2), 
				"" + ChatColor.ITALIC + ChatColor.GRAY + "PRIMARY " + ChatColor.GREEN + profile.classes.get(0).primary, ChatColor.GRAY, ""));
		retInv.setItem(13, namedItemStack(Material.getMaterial(profile.classes.get(0).weapon3), 
				"" + ChatColor.ITALIC + ChatColor.GRAY + "SECONDARY " + ChatColor.GREEN + profile.classes.get(0).secondary, ChatColor.GRAY, ""));
		
		if (lethal != null) {
			setItemName(lethal, "" + ChatColor.ITALIC + ChatColor.GRAY + "LETHAL " + ChatColor.GREEN + ChatColor.stripColor(lethal.getItemMeta().getDisplayName()));
			retInv.setItem(14, lethal);
		}else{
			retInv.setItem(14, namedItemStack(Material.SNOW_BALL, 
					"" + ChatColor.ITALIC + ChatColor.GRAY + "LETHAL " + ChatColor.GREEN + "NONE", ChatColor.GRAY, ""));
		}
		
		if (tactical != null) {
			setItemName(tactical, "" + ChatColor.ITALIC + ChatColor.GRAY + "TACTICAL " + ChatColor.GREEN + ChatColor.stripColor(tactical.getItemMeta().getDisplayName()));
			retInv.setItem(15, tactical);
		}else{
			retInv.setItem(15, namedItemStack(Material.SNOW_BALL, "" + ChatColor.ITALIC + ChatColor.GRAY + "TACTICAL " + ChatColor.GREEN + "NONE", ChatColor.GRAY, ""));
		}
		
		if (profile.perk.length() > 1) {
			retInv.setItem(16, getItemStack_Perk());
		}else{
			retInv.setItem(16, namedItemStack(Material.SNOW_BALL, "" + ChatColor.ITALIC + ChatColor.GRAY + "PERK " + ChatColor.GREEN + "NONE", ChatColor.GRAY, ""));			
		}
		
		openInventory(retInv);
	}
	
	private void setItemName(ItemStack itm, String string) {
		ItemMeta meta = itm.getItemMeta();
		meta.setDisplayName(string);
		itm.setItemMeta(meta);
	}

	private ItemStack getItemStack_Perk() {
		if (this.profile.perk.equals("juggernaut")) {
			return namedItemStack(Material.RAW_BEEF, "" + ChatColor.ITALIC + ChatColor.GRAY + "PERK " + ChatColor.GREEN + "Juggernaut", ChatColor.GRAY, "Bullets do ⅔ less damage/to you");
		}
		if (this.profile.perk.equals("stoppingpower")) {
			return namedItemStack(Material.RAW_CHICKEN, "" + ChatColor.ITALIC + ChatColor.GRAY + "PERK " + ChatColor.GREEN + "Stopping Power", ChatColor.GRAY, "Bullets do 1 heart more damage");
		}
		if (this.profile.perk.equals("sleightofhand")) {
			return namedItemStack(Material.RAW_FISH, "" + ChatColor.ITALIC + ChatColor.GRAY + "PERK " + ChatColor.GREEN + "Sleight of Hand", ChatColor.GRAY, "Guns reload twice as fast");
		}
		if (this.profile.perk.equals("speed")) {
			return namedItemStack(Material.CARROT_ITEM, "" + ChatColor.ITALIC + ChatColor.GRAY + "PERK " + ChatColor.GREEN + "Speed", ChatColor.GRAY, "Run faster while alive");
		}
		if (this.profile.perk.equals("marathon")) {
			return namedItemStack(Material.BREAD, "" + ChatColor.ITALIC + ChatColor.GRAY + "PERK " + ChatColor.GREEN + "Marathon", ChatColor.GRAY, "Never run out of energy/while running");
		}
		if (this.profile.perk.equals("scavenger")) {
			return namedItemStack(Material.APPLE, "" + ChatColor.ITALIC + ChatColor.GRAY + "PERK " + ChatColor.GREEN + "Scavenger", ChatColor.GRAY, "Gain ¼ ammo back/when you get a kill");
		}
		
		if (this.profile.perk.equals("martyrdom")) {
			return namedItemStack(Material.BAKED_POTATO, "" + ChatColor.ITALIC + ChatColor.GRAY + "PERK " + ChatColor.GREEN + "Martyrdom", ChatColor.GRAY, "Drop a grenade upon/your death");
		}
		
		if (this.profile.perk.equals("hardline")) {
			return namedItemStack(Material.EGG, "" + ChatColor.ITALIC + ChatColor.GRAY + "PERK " + ChatColor.GREEN + "Hardline", ChatColor.GRAY, "Earn killstreaks with/one less kill");
		}
		
		if (this.profile.perk.equals("flakjacket")) {
			return namedItemStack(Material.PUMPKIN_PIE, "" + ChatColor.ITALIC + ChatColor.GRAY + "PERK " + ChatColor.GREEN + "Flakjacket", ChatColor.GRAY, "Explosives do half/their normal damage");
		}
		
		return null;
	}
	
	private ItemStack getItemStack_Lethal() {
		int amtGrenade = 0;
		if (this.plugin.hasVoted(this.player)) {
			amtGrenade++;
		}
		if (PermissionInterface.hasPermission(this.player, "mcwar.grenade")) {
			amtGrenade += 2;
		}
		if ((amtGrenade > 0) && (this.profile.lethal.equals("grenade"))) {
			ItemStack itm = namedItemStack(Material.SLIME_BALL, ChatColor.YELLOW + "Grenade", ChatColor.GRAY, "");
			itm.setAmount(amtGrenade);
			return itm;
		}

		if ((PermissionInterface.hasPermission(this.player, "mcwar.c4")) && (this.profile.lethal.equals("c4"))) {
			ItemStack itm = namedItemStack(Material.LEVER, ChatColor.YELLOW + "c4", ChatColor.GRAY, "");
			itm.setAmount(2);
			return itm;
		}

		if ((PermissionInterface.hasPermission(this.player, "mcwar.tomahawk")) && (this.profile.lethal.equals("tomahawk"))) {
			ItemStack itm = namedItemStack(Material.GOLD_AXE, ChatColor.YELLOW + "Tomahawk", ChatColor.GRAY, "");
			return itm;
		}
		
		return null;
	}
	
	private ItemStack getItemStack_Tactical() {
		if ((PermissionInterface.hasPermission(this.player, "mcwar.flashbang")) && (this.profile.tactical.equals("flashbang"))) {
			ItemStack itm = namedItemStack(Material.SULPHUR, ChatColor.YELLOW + "Flashbang", ChatColor.GRAY, "");
			itm.setAmount(2);
			return itm;
		}

		if ((PermissionInterface.hasPermission(this.player, "mcwar.molotov")) && (this.profile.tactical.equals("molotov"))) {
			ItemStack itm = namedItemStack(Material.GLOWSTONE_DUST, ChatColor.YELLOW + "Molotov", ChatColor.GRAY, "");
			itm.setAmount(2);
			return itm;
		}
		
		return null;
	}

	public void openGunSelectMenu(String gunType) {
		this.player.playSound(this.player.getLocation(), Sound.BAT_TAKEOFF, 1.0F, 1.0F);
		Inventory retInv = Bukkit.createInventory(this.player, 45, "Gun Selection Menu");
		retInv.addItem(new ItemStack[] { namedItemStack(Material.LAVA_BUCKET, "Back", ChatColor.GRAY, "Go back") });
		
		int count = 0;
		for (int i = 0; i < this.plugin.loadedGuns.size(); i++) {
			KitGun gun = (KitGun)this.plugin.loadedGuns.get(i);
			String g = (gun).name;
			boolean has = this.profile.hasGun(g);
			if (has && (gunType == null || gun.slot.equalsIgnoreCase(gunType))) {
				ItemStack itm = new ItemStack(((KitGun)this.plugin.loadedGuns.get(i)).type, 1);
				ItemMeta imeta = itm.getItemMeta();
				imeta.setDisplayName(ChatColor.GREEN + "GUN");
				ArrayList<String> lore = new ArrayList<String>();
				lore.add(ChatColor.GRAY + ((KitGun)this.plugin.loadedGuns.get(i)).name);
				imeta.setLore(lore);
				itm.setItemMeta(imeta);
				retInv.setItem(9 + count, itm);
				count++;
			}
		}

		openInventory(retInv);
	}

	public void openBuyMenu() {
		this.player.playSound(this.player.getLocation(), Sound.ANVIL_LAND, 1.0F, 1.0F);
		Inventory retInv = Bukkit.createInventory(this.player, 45, "Gun Store (money: " + this.profile.credits + ")");

		retInv.addItem(new ItemStack[] { namedItemStack(Material.LAVA_BUCKET, "Back", ChatColor.GRAY, "Go back") });

		int count = 0;
		for (int i = 0; i < this.plugin.loadedGuns.size(); i++) {
			String g = ((KitGun)this.plugin.loadedGuns.get(i)).name;
			boolean has = ((KitGun)this.plugin.loadedGuns.get(i)).isUnlocked(this);
			if ((has) && (!this.profile.hasGun(g))) {
				ItemStack itm = new ItemStack(((KitGun)this.plugin.loadedGuns.get(i)).type, 1);
				ItemMeta imeta = itm.getItemMeta();
				imeta.setDisplayName(ChatColor.GREEN + "GUN $" + Integer.toString(((KitGun)this.plugin.loadedGuns.get(i)).cost));
				ArrayList lore = new ArrayList();
				lore.add(ChatColor.GRAY + g);
				imeta.setLore(lore);
				itm.setItemMeta(imeta);
				retInv.setItem(9 + count, itm);
				count++;
			}
		}

		this.player.closeInventory();
		openInventory(retInv);
	}

	public void openPerkMenu() {
		this.player.playSound(this.player.getLocation(), Sound.CHEST_OPEN, 1.0F, 1.0F);
		Inventory retInv = Bukkit.createInventory(this.player, 27, "Perk Menu");

		retInv.addItem(new ItemStack[] { namedItemStack(Material.LAVA_BUCKET, "Back", ChatColor.GRAY, "Go back") });
		retInv.setItem(9, namedItemStack(Material.BOOK, ChatColor.AQUA + "Perks", ChatColor.GRAY, "These passively affect/gameplay, and allow for a more/customized class loadout"));

		int count = 0;
		int base = 18;
		if (this.profile.hasPerk("juggernaut")) {
			retInv.setItem(base + count, namedItemStack(Material.RAW_BEEF, ChatColor.AQUA + "PERK Juggernaut", ChatColor.GRAY, "Bullets do ⅔ less damage/to you"));
			count++;
		}
		if (this.profile.hasPerk("stoppingpower")) {
			retInv.setItem(base + count, namedItemStack(Material.RAW_CHICKEN, ChatColor.AQUA + "PERK Stoppingpower", ChatColor.GRAY, "Bullets do 1 heart more damage"));
			count++;
		}
		if (this.profile.hasPerk("sleightofhand")) {
			retInv.setItem(base + count, namedItemStack(Material.RAW_FISH, ChatColor.AQUA + "PERK SleightofHand", ChatColor.GRAY, "Guns reload twice as fast"));
			count++;
		}
		if (this.profile.hasPerk("speed")) {
			retInv.setItem(base + count, namedItemStack(Material.CARROT_ITEM, ChatColor.AQUA + "PERK Speed", ChatColor.GRAY, "Run faster while alive"));
			count++;
		}
		if (this.profile.hasPerk("marathon")) {
			retInv.setItem(base + count, namedItemStack(Material.BREAD, ChatColor.AQUA + "PERK Marathon", ChatColor.GRAY, "Never run out of energy/while running"));
			count++;
		}
		if (this.profile.hasPerk("scavenger")) {
			retInv.setItem(base + count, namedItemStack(Material.APPLE, ChatColor.AQUA + "PERK Scavenger", ChatColor.GRAY, "Gain ¼ ammo back/when you get a kill"));
			count++;
		}
		
		if (this.profile.hasPerk("martyrdom")) {
			retInv.setItem(base + count, namedItemStack(Material.BAKED_POTATO, ChatColor.AQUA + "PERK Martyrdom", ChatColor.GRAY, "Drop a grenade upon/your death"));
			count++;
		}
		
		if (this.profile.hasPerk("hardline")) {
			retInv.setItem(base + count, namedItemStack(Material.EGG, ChatColor.AQUA + "PERK Hardline", ChatColor.GRAY, "Earn killstreaks with/one less kill"));
			count++;
		}
		
		if (this.profile.hasPerk("flakjacket")) {
			retInv.setItem(base + count, namedItemStack(Material.PUMPKIN_PIE, ChatColor.AQUA + "PERK Flakjacket", ChatColor.GRAY, "Explosives do half/their normal damage"));
			count++;
		}

		openInventory(retInv);
	}

	public void openGrenadeMenu() {
		this.player.playSound(this.player.getLocation(), Sound.CHEST_OPEN, 1.0F, 1.0F);
		Inventory retInv = Bukkit.createInventory(this.player, 36, "Grenade Menu");

		retInv.addItem(new ItemStack[] { namedItemStack(Material.LAVA_BUCKET, "Back", ChatColor.GRAY, "Go back") });

		retInv.setItem(18, namedItemStack(Material.BOOK, ChatColor.AQUA + "Lethals", ChatColor.GRAY, "Your grenades that/do lethal damage"));
		retInv.setItem(27, namedItemStack(Material.BOOK, ChatColor.RED + "Tacticals", ChatColor.GRAY, "Your grenades that/are used for/tactical situations"));

		int start = 18;
		int count = 2;
		if ((PermissionInterface.hasPermission(this.player, "mcwar.grenade")) || (this.plugin.hasVoted(this.player))) {
			retInv.setItem(start + count, namedItemStack(Material.SLIME_BALL, ChatColor.GREEN + "LETHAL " + ChatColor.AQUA + "grenade", ChatColor.GRAY, "High explosive/radius grenade"));
			count++;
		}

		if (PermissionInterface.hasPermission(this.player, "mcwar.tomahawk")) {
			retInv.setItem(start + count, namedItemStack(Material.GOLD_AXE, ChatColor.GREEN + "LETHAL " + ChatColor.AQUA + "tomahawk", ChatColor.GRAY, "High range/lethal axe"));
			count++;
		}
		
		if ((PermissionInterface.hasPermission(this.player, "mcwar.c4"))) {
			retInv.setItem(start + count, namedItemStack(Material.LEVER, ChatColor.GREEN + "LETHAL " + ChatColor.AQUA + "c4", ChatColor.GRAY, "Detonation controlled/explosive"));
			count++;
		}

		start = 27;
		count = 2;
		if (PermissionInterface.hasPermission(this.player, "mcwar.flashbang")) {
			retInv.setItem(start + count, namedItemStack(Material.SULPHUR, ChatColor.GREEN + "TACTICAL " + ChatColor.AQUA + "flashbang", ChatColor.GRAY, "Blinding grenade"));
			count++;
		}
		if (PermissionInterface.hasPermission(this.player, "mcwar.molotov")) {
			retInv.setItem(start + count, namedItemStack(Material.GLOWSTONE_DUST, ChatColor.GREEN + "TACTICAL " + ChatColor.AQUA + "molotov", ChatColor.GRAY, "High incendary/radius grenade"));
			count++;
		}

		openInventory(retInv);
	}

	public void openGunMenu() {
		this.player.playSound(this.player.getLocation(), Sound.CHEST_OPEN, 1.0F, 1.0F);
		Inventory retInv = Bukkit.createInventory(this.player, 18, "Gun Menu");

		retInv.addItem(namedItemStack(Material.CHEST, ChatColor.AQUA + "Class Selection", ChatColor.GRAY, "Manage your /class loadout"));
		retInv.addItem(namedItemStack(Material.ANVIL, ChatColor.RED + "Buy New Guns", ChatColor.GRAY, "Open up a list/of buyable guns"));
		//retInv.addItem(namedItemStack(Material.SLIME_BALL, ChatColor.BLUE + "Manage Grenades", ChatColor.GRAY, "Choose which/grenades you want/as lethals or/as tacticals"));
		//retInv.addItem(namedItemStack(Material.GOLDEN_APPLE, ChatColor.BLUE + "Manage Perks", ChatColor.GRAY, "Choose which/perks you want/active"));

		openInventory(retInv);
	}
	
	public ItemStack namedItemStack(Material chest, String string, ChatColor gray, String string2, MaterialData data) {
		String[] lores = string2.split("/");
		ArrayList lore = new ArrayList();
		if (lores != null) {
			for (int i = 0; i < lores.length; i++) {
				lore.add(gray + lores[i]);
			}
		}
		ItemStack ret = new ItemStack(chest, 1, data.getData());
		ItemMeta meta = ret.getItemMeta();
		meta.setDisplayName(string);
		meta.setLore(lore);
		ret.setItemMeta(meta);
		return ret;
	}

	public ItemStack namedItemStack(Material chest, String string, ChatColor gray, String string2) {
		ArrayList lore = new ArrayList();
		if (string2 != null) {
			String[] lores = string2.split("/");
			for (int i = 0; i < lores.length; i++) {
				lore.add(gray + lores[i]);
			}
		}
		ItemStack ret = new ItemStack(chest);
		ItemMeta meta = ret.getItemMeta();
		meta.setDisplayName(string);
		meta.setLore(lore);
		ret.setItemMeta(meta);
		return ret;
	}

	public void tick() {
		this.kclass = ((KitClass)this.profile.classes.get(this.profile.myclass));
		this.player = Util.MatchPlayer(this.name);
		if (this.player == null)
			return;
		if (this.player.isDead())
			this.timedead += 1;
		else {
			this.timedead = 0;
		}
		this.killTicks -= 1;

		this.lastSpawn += 1;

		updateTabList(this.player);

		int k = this.kills;
		int d = this.deaths;
		if (this.arena.type.equals("lobby")) {
			k = this.profile.kills;
			d = this.profile.deaths;
		}

		double percent = this.profile.xp / (double)this.profile.xpn;
		this.player.setExp((float)percent);

		Score score4 = this.scoreboard.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Credits"));
		score4.setScore(this.profile.credits);

		Score score = this.scoreboard.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Kills"));
		score.setScore(k);

		Score score2 = this.scoreboard.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Deaths"));
		score2.setScore(d);

		Score score5 = this.scoreboard.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Level"));
		score5.setScore(this.profile.level);

		Score score3 = this.scoreboard.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Killstreak"));
		score3.setScore(this.killStreak);

		if ((!this.player.getGameMode().equals(GameMode.CREATIVE)) && (!this.player.isOp())) {
			this.player.setGameMode(GameMode.ADVENTURE);
		}
		if (this.profile.level <= 0) {
			this.profile.level = 1;
		}
		if ((this.timedead > 45) && (this.player.isDead())) {
			this.player.kickPlayer("KICKED FOR AFK");
		}

		if (!this.player.isDead()) {
			if (this.player.isSprinting()) {
				this.player.setFoodLevel(this.player.getFoodLevel() - 1);
			}
			else if (this.player.getFoodLevel() < 19) {
				this.player.setFoodLevel(this.player.getFoodLevel() + 2);
			}

			if (this.player.getHealth() < 20) {
				this.player.setHealth(this.player.getHealth() + 1);
			}
			
		}else{
			PacketPlayInClientCommand in = new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN);
	        EntityPlayer cPlayer = ((CraftPlayer)player).getHandle();
	        cPlayer.playerConnection.a(in);
		}

		for (int i = 0; i < this.perks.size(); i++) {
			((Perk)this.perks.get(i)).step();
		}

		this.hasCodClient = false;
		if (this.arena.plugin.getCodClient(this.player.getName())) {
			this.hasCodClient = true;
		}

		decideHat();
		setChat();
		calculate();
		if (this.timedead <= 0) {
			this.alive += 1;
		}

		if (this.isSpawning) {
			this.player.teleport(this.spawnLoc);
			this.alive = 0;
			this.isSpawning = false;
			spawn();
			
			player.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 8, 1);
		}

		this.clanInviteTimer -= 1;
		if (this.clanInviteTimer < 0) {
			this.clanInviteTo = "";
		}
		this.lastLoc = this.player.getLocation();
	}

	private int sendStats() {
		if (this.player == null)
			return 0;
		if (!this.player.isOnline())
			return 0;
		if (!this.hasCodClient)
			return 0;
		String myteam = getTeamName();
		if (myteam.contains("ffa"))
			myteam = "white";
		if (myteam.contains("blue"))
			myteam = "blue";
		if (myteam.contains("red"))
			myteam = "red";
		String teamName = getTeamColor() + myteam;
		if (this.arena.gameModifier.equals(KitArena.GameModeModifier.INFECT)) {
			if (this.team.equals(KitArena.Teams.RED))
				teamName = ChatColor.RED + "Infected";
			else {
				teamName = ChatColor.BLUE + "Survivor";
			}
		}
		if (myteam.contains("ffa"))
			teamName = "FFA";
		String toSend = "Â©Â©";
		toSend = toSend + "h" + getCodClientSeperator();
		toSend = toSend + myteam + getCodClientSeperator();
		toSend = toSend + teamName + getCodClientSeperator();
		if (this.arena.type.equals("tdm")) {
			toSend = toSend + ChatColor.RED + "RED TEAM    " + Integer.toString(this.arena.redkills) + getCodClientEscapeCharacter();
			toSend = toSend + ChatColor.BLUE + "BLUE TEAM  " + Integer.toString(this.arena.bluekills) + getCodClientEscapeCharacter();
		} else if (this.arena.type.equals("ffa")) {
			KitPlayer mostkills = this.arena.getPlayerWithMostKills();
			if (mostkills != null) {
				toSend = toSend + ChatColor.LIGHT_PURPLE + "LEADER:  " + this.arena.getLeader() + getCodClientEscapeCharacter();
				toSend = toSend + " " + getCodClientEscapeCharacter();
			} else {
				toSend = toSend + " " + getCodClientEscapeCharacter();
				toSend = toSend + " " + getCodClientEscapeCharacter();
			}
		} else {
			toSend = toSend + "0" + getCodClientEscapeCharacter();
			toSend = toSend + "0" + getCodClientEscapeCharacter();
		}
		toSend = toSend + Integer.toString(getAmtAmmo("primary")) + getCodClientEscapeCharacter();
		toSend = toSend + Integer.toString(getAmtAmmo("secondary")) + getCodClientEscapeCharacter();

		toSend = toSend + ChatColor.RED + "K " + Integer.toString(getKills()) + getCodClientEscapeCharacter();
		toSend = toSend + ChatColor.BLUE + "D    " + Integer.toString(getDeaths()) + getCodClientEscapeCharacter();
		toSend = toSend + ChatColor.YELLOW + "R       " + Double.toString(getKDR()) + getCodClientEscapeCharacter();
		toSend = toSend + ChatColor.GREEN + "Streak: " + Integer.toString(this.killStreak) + getCodClientEscapeCharacter();

		toSend = toSend + ChatColor.GREEN + Integer.toString(this.arena.timer) + "s" + getCodClientEscapeCharacter();

		toSend = toSend + ChatColor.DARK_RED + "â•�â•�â•�â•�â•£ " + ChatColor.RED + this.arena.name + ChatColor.DARK_RED + " â• â•�â•�â•�â•�" + getCodClientEscapeCharacter();
		toSend = toSend + ChatColor.GRAY + "Gamemode: " + ChatColor.GOLD + this.arena.getArenaType() + ChatColor.WHITE + "     " + getTeams() + getCodClientEscapeCharacter();
		toSend = toSend + ChatColor.GRAY + "Level: " + ChatColor.YELLOW + this.profile.level + "   " + ChatColor.RED + drawXp() + ChatColor.GRAY + "   Credits: " + ChatColor.YELLOW + Integer.toString(this.profile.credits) + getCodClientEscapeCharacter();

		//Packet3Chat p = new Packet3Chat(toSend);
		//((CraftPlayer)this.player).getHandle().playerConnection.sendPacket(p);
		
		
		return 3;
	}

	public String getCodClientEscapeCharacter() {
		return "" + ChatColor.WHITE + ChatColor.RESET + getCodClientSeperator();
	}

	public String getCodClientSeperator() {
		return "âˆ«";
	}

	public ItemStack setColor(ItemStack is, Color color) {
		LeatherArmorMeta lam = (LeatherArmorMeta)is.getItemMeta();
		lam.setColor(color);
		is.setItemMeta(lam);
		return is;
	}

	public int getStringLength(String str) {
		if(str == null) {
			return 0;
		} else {
			int ret = str.length();

			for(int i = 0; i < str.length(); ++i) {
				char c = str.charAt(i);
				if(c == 167) {
					ret -= 2;
				}
			}

			return ret;
		}
	}

	public int getColorLength(String str) {
		int ret = 0;
		if(str == null) {
			return ret;
		} else {
			for(int i = 0; i < str.length(); ++i) {
				char c = str.charAt(i);
				if(c == 167) {
					ret += 2;
				}
			}

			return ret;
		}
	}

	public void sayMessage(Player from, String msg)
	{
		if ((from != null) && 
				(!this.receiveChat) && (!PermissionInterface.hasPermission(from, "mcwar.admin"))) {
			return;
		}
		if (this.specialMessage == null)
			this.player.sendMessage(msg);
	}

	public void doMessage(String msg)
	{
		for (int i = this.chat.length - 2; i >= 0; i--) {
			this.chat[(i + 1)] = this.chat[i];
		}
		this.chat[0] = msg;
	}

	public void clearChat() {
		for (int i = 1; i <= 32; i++) {
			this.player.sendMessage("");
		}
		for (int i = this.chat.length - 1; i >= 0; i--)
			this.chat[i] = "";
	}

	public void setChat()
	{
		if (this.specialMessage == null) {
			return;
		}
		clearChat();
		this.player.sendMessage("");
		this.player.sendMessage("");
		this.player.sendMessage("");
		this.player.sendMessage("");
		this.specialMessage.draw();
		if (this.specialMessage.getTicks() > this.specialMessage.maxTicks) {
			this.specialMessage.clear();
			this.specialMessage = null;
		}
	}

	public int getKills() {
		if (this.arena.type.equals("lobby")) {
			return this.profile.kills;
		}
		return this.kills;
	}

	public int getDeaths() {
		if (this.arena.type.equals("lobby")) {
			return this.profile.deaths;
		}
		return this.deaths;
	}

	public void decideHat() {
		checkInventory();
		if (this.alive < 4) {
			return;
		}
		int teamcolor = getWoolColor();
		int hexColor = 255;
		if (this.team.equals(KitArena.Teams.RED)) {
			hexColor = 16711680;
			if (this.arena.gameModifier.equals(KitArena.GameModeModifier.INFECT)) {
				hexColor = 3381504;
			}
		}
		if (this.team.equals(KitArena.Teams.FFA)) {
			hexColor = 16711935;
		}
		Color color = Color.fromRGB(hexColor);

		ItemStack c0 = setColor(new ItemStack(Material.LEATHER_HELMET, 1), color);
		ItemStack c1 = setColor(new ItemStack(Material.LEATHER_CHESTPLATE, 1), color);
		ItemStack c2 = setColor(new ItemStack(Material.LEATHER_LEGGINGS, 1), color);
		ItemStack c3 = setColor(new ItemStack(Material.LEATHER_BOOTS, 1), color);

		if ((teamcolor != -1) && 
				(this.player.getInventory().getHelmet() == null)) {
			this.player.getInventory().setHelmet(c0);
		}

		if (this.player.getInventory().getChestplate() == null)
			this.player.getInventory().setChestplate(c1);
		if (this.player.getInventory().getLeggings() == null)
			this.player.getInventory().setLeggings(c2);
		if (this.player.getInventory().getBoots() == null)
			this.player.getInventory().setBoots(c3);
	}

	public int getWoolColor() {
		int arenacolor = this.arena.getWoolColor(this);
		if (arenacolor == 999)
			return -1;
		if (arenacolor == -1) {
			int ret = 11;
			if (this.team.equals(KitArena.Teams.RED))
				ret = 14;
			return ret;
		}
		return arenacolor;
	}

	public String drawXpBar() {
		int max = 22;
		double ratio = this.profile.xp / this.profile.xpn;
		int have = (int)(max * ratio);
		int need = max - have;
		if (have > max) {
			return "";
		}

		String ret = "";
		for (int i = 0; i < have; i++) {
			ret = ret + ChatColor.AQUA + "â–“";
		}

		for (int i = 0; i < need; i++) {
			ret = ret + ChatColor.GRAY + "â–‘";
		}
		ret = ret + ChatColor.WHITE + " " + drawXp();
		return ret;
	}

	public String drawXp() {
		return this.profile.xp + "/" + this.profile.xpn;
	}

	public String getTeams() {
		if (this.arena.getMode().equals("tdm")) {
			return ChatColor.BLUE + Integer.toString(this.arena.getPlayersOnTeam(KitArena.Teams.BLUE).size()) + ChatColor.AQUA + "/" + ChatColor.RED + Integer.toString(this.arena.getPlayersOnTeam(KitArena.Teams.RED).size()) + ChatColor.AQUA + " players";
		}
		return ChatColor.YELLOW + Integer.toString(this.arena.getPlayersOnTeam(KitArena.Teams.ALL).size()) + ChatColor.AQUA + " players";
	}

	public ChatColor getTeamColor()
	{
		if (this.team.equals(KitArena.Teams.BLUE))
			return ChatColor.BLUE;
		if (this.team.equals(KitArena.Teams.RED))
			return ChatColor.RED;
		if (this.team.equals(KitArena.Teams.NEUTRAL)) {
			return ChatColor.GRAY;
		}
		return ChatColor.LIGHT_PURPLE;
	}

	public String getTeamName() {
		if (this.team.equals(KitArena.Teams.BLUE))
			return "blue" + ChatColor.WHITE;
		if (this.team.equals(KitArena.Teams.RED)) {
			return "red" + ChatColor.WHITE;
		}
		return "ffa" + ChatColor.WHITE;
	}

	public int getHealth() {
		return (int)(this.player.getHealth() / 20.0D * 100.0D);
	}

	public double getKDR() {
		double big = (double)this.getKills();
		if(this.getDeaths() > 0) {
			big = (double)this.getKills() / (double)this.getDeaths() * 100.0D;
		} else {
			big *= 100.0D;
		}

		double round = (double)Math.round(big);
		return round / 100.0D;
	}

	public void spawn_gunGame() {
		String gunName = (String)this.arena.plugin.guns_gungame.get(this.gungameLevel);
		KitGun gun = this.plugin.getGun(gunName);
		if (gun != null) {
			this.player.getInventory().clear();

			if (this.kclass.armor0 > 0) this.player.getInventory().setHelmet(getItemStack(this.kclass.armor0, this.kclass.enchanthelmet));
			if (this.kclass.armor1 > 0) this.player.getInventory().setChestplate(getItemStack(this.kclass.armor1, this.kclass.enchantchest));
			if (this.kclass.armor2 > 0) this.player.getInventory().setLeggings(getItemStack(this.kclass.armor2, this.kclass.enchantlegs));
			if (this.kclass.armor3 > 0) this.player.getInventory().setBoots(getItemStack(this.kclass.armor3, this.kclass.enchantboots));

			int type = gun.type;
			int ammo = this.plugin.getGunAmmo(gunName);
			if (ammo > -1) {
				int amt = this.plugin.getAmmo(this.player, "primary");
				if (this.profile.knife == 1)
					giveItem(this.player, Material.IRON_SWORD.getId(), new KitEnchantment(), (byte)0, 1, 0);
				if (this.profile.knife == 2) {
					giveItem(this.player, Material.DIAMOND_SWORD.getId(), new KitEnchantment(), (byte)0, 1, 0);
				}
				giveItem(this.player, type, null, (byte)0, 1, 1);
				giveItem(this.player, ammo, null, (byte)0, amt, 8);
			}
		}
	}

	public void spawn() {
		if (!this.player.isOnline())
			return;
		TagAPI.refreshPlayer(this.player);
		this.dead = false;
		if (this.lastSpawn <= 2)
			this.player.teleport(this.arena.getSpawnLocation(this), TeleportCause.PLUGIN);
		try {
			TabAPI.setPriority(this.plugin, this.player, -2);
			TabAPI.updatePlayer(this.player);
		} catch (Exception localException1) {
			
		}
		TabAPI.setPriority(this.plugin, this.player, 2);

		sayMessage(null, ChatColor.DARK_AQUA + "http://mcbrawl.com");
		sayMessage(null, ChatColor.GRAY + "  to donate, learn tips, tricks, and more!");
		try {
			Player p = Util.MatchPlayer(this.player.getName());
			p.getInventory().clear();
			if (this.kclass == null) {
				if (!this.arena.checkSpawn(this)) {
					p.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE, 1));
					p.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS, 1));
					p.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS, 1));
					p.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD, 1));
				}
			} else {
				try {
					for (int i = 0; i < this.kclass.pots.size(); i++) {
						this.player.addPotionEffect((PotionEffect)this.kclass.pots.get(i));
					}
					if (this.kclass.armor0 > 0) p.getInventory().setHelmet(getItemStack(this.kclass.armor0, this.kclass.enchanthelmet));
					if (this.kclass.armor1 > 0) p.getInventory().setChestplate(getItemStack(this.kclass.armor1, this.kclass.enchantchest));
					if (this.kclass.armor2 > 0) p.getInventory().setLeggings(getItemStack(this.kclass.armor2, this.kclass.enchantlegs));
					if (this.kclass.armor3 > 0) p.getInventory().setBoots(getItemStack(this.kclass.armor3, this.kclass.enchantboots));

					int amtGrenade = 0;

					if (!this.arena.checkSpawn(this)) {
						if (this.profile.knife == 1)
							giveItem(p, Material.IRON_SWORD.getId(), new KitEnchantment(), (byte)0, 1, 0);
						if (this.profile.knife == 2) {
							giveItem(p, Material.DIAMOND_SWORD.getId(), new KitEnchantment(), (byte)0, 1, 0);
						}
						giveItem(p, this.kclass.weapon2, this.kclass.enchant2, this.kclass.special2, this.kclass.amt2, 1);
						giveItem(p, this.kclass.weapon3, this.kclass.enchant3, this.kclass.special3, this.kclass.amt3, 2);
						giveItem(p, this.kclass.weapon4, this.kclass.enchant4, this.kclass.special4, this.kclass.amt4, 3);
						giveItem(p, this.kclass.weapon5, this.kclass.enchant5, this.kclass.special5, this.kclass.amt5, 4);
						giveItem(p, this.kclass.weapon6, this.kclass.enchant6, this.kclass.special6, this.kclass.amt6, 5);
						giveItem(p, this.kclass.weapon7, this.kclass.enchant7, this.kclass.special7, this.kclass.amt7, 6);
						giveItem(p, this.kclass.weapon8, this.kclass.enchant8, this.kclass.special8, this.kclass.amt8, 18);
						giveItem(p, this.kclass.weapon9, this.kclass.enchant9, this.kclass.special9, this.kclass.amt9, 26);

						if (this.plugin.hasVoted(this.player)) {
							amtGrenade++;
						}
					}
					if (!this.arena.gameModifier.equals(KitArena.GameModeModifier.GUNGAME)) {
						ItemStack lethal = getItemStack_Lethal();
						if (lethal != null) {
							new KitItem(lethal).give(this);
						}
						
						ItemStack tactical = getItemStack_Tactical();
						if (tactical != null) {
							new KitItem(tactical).give(this);
						}
					}

					for (int i = 0; i < this.boughtItems.size(); i++) {
						((KitItem)this.boughtItems.get(i)).give(this);
					}

					for (int i = 0; i < this.perks.size(); i++) {
						((Perk)this.perks.get(i)).giveToPlayer(this.player);
					}

					int time = 160;
					String myteam = getTeamName();
					if (myteam.contains("ffa")) {
						time = 40;
					}
					p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, time, 14));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if (this.arena.type.equals("lobby")) {
			this.player.getInventory().clear();
			ItemStack itm = new ItemStack(Material.EYE_OF_ENDER, 1);
			ItemMeta meta = itm.getItemMeta();
			meta.setDisplayName(ChatColor.AQUA + "Gun Menu");
			ArrayList lore = new ArrayList();
			lore.add(ChatColor.RED + "Use this to open the gun menu");
			meta.setLore(lore);
			itm.setItemMeta(meta);
			this.player.getInventory().addItem(new ItemStack[] { itm });

			ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
			BookMeta bmeta = (BookMeta)book.getItemMeta();
			bmeta.setAuthor("orange451");
			bmeta.setTitle("How to play");
			bmeta.addPage(new String[] { ChatColor.RED + "How to play MCWar! \n\n" + ChatColor.GOLD + "1:" + ChatColor.GRAY + "Index\n" + ChatColor.GOLD + "2:" + ChatColor.GRAY + "Guns\n" + ChatColor.GOLD + "3:" + ChatColor.GRAY + "Donating\n" + ChatColor.GOLD + "4:" + ChatColor.GRAY + "Objectives" });
			bmeta.addPage(new String[] { ChatColor.RED + "Guns \n Normal Minecraft items are what make up GUNS in MCWar. \n\n" + 
					ChatColor.DARK_GRAY + "To shoot them, right click. \nTo aim in, left click" });
			bmeta.addPage(new String[] { ChatColor.RED + "Donating \n" + 
					ChatColor.DARK_GRAY + " Donators in MCWar get special benefits! \n\n You can get different guns, grenades, knives, or perks! \n\n if you are at all interested, type " + ChatColor.BLUE + "/buy" });
			bmeta.addPage(new String[] { ChatColor.RED + "Objectives \n" + 
					ChatColor.DARK_GRAY + " Every game has an objective, which is the goal you or you and your team are supposed to do. \n" });
			bmeta.addPage(new String[] { "Objectives (continued) \n\n" + 
					ChatColor.RED + " TDM:\n" + 
					ChatColor.DARK_GRAY + "  Your team has to get the most kills to win" });
			bmeta.addPage(new String[] { "Objectives (continued) \n\n" + 
					ChatColor.RED + " CTF:\n" + 
					ChatColor.DARK_GRAY + "  Your team has to capture the most flags to win" });
			bmeta.addPage(new String[] { "Objectives (continued) \n\n" + 
					ChatColor.RED + " FFA:\n" + 
					ChatColor.DARK_GRAY + "  You have to be the first person to reach 20 kills to win" });
			bmeta.addPage(new String[] { "Objectives (continued) \n\n" + 
					ChatColor.RED + " GUNGAME:\n" + 
					ChatColor.DARK_GRAY + "  Every person starts off with a level 1 gun. With each kill, their gun gets upgraded. \n\n The first person to 25 kills wins." });
			bmeta.addPage(new String[] { "Objectives (continued) \n\n" + 
					ChatColor.RED + " INFECT:\n" + 
					ChatColor.DARK_GRAY + "  You have to survive the longest without dying. \n\n At the start, one person is spawned as a zombie. When a person on the " + ChatColor.BLUE + "survivor" + ChatColor.DARK_GRAY + " team dies, they become a zombie." });
			bmeta.addPage(new String[] { "Objectives (continued) \n\n" + 
					ChatColor.RED + " ONEIN:" + ChatColor.GRAY + " (one in the chamber)\n" + 
					ChatColor.DARK_GRAY + "  Avoid getting shot, but shoot other people! \n  When you are shot, you lose a life. When your three lives are up, you are \"out\", the last person left standing wins." });
			book.setItemMeta(bmeta);

			this.player.getInventory().addItem(new ItemStack[] { book });

			ItemStack book2 = new ItemStack(Material.WRITTEN_BOOK, 1);
			BookMeta bmeta2 = (BookMeta)book2.getItemMeta();
			bmeta2.setAuthor("orange451");
			bmeta2.setTitle(ChatColor.RED + "NEW UPDATE (7-5-2013)");
			bmeta2.addPage(new String[] { ChatColor.BLACK + "Updates for\nMinecraft 1.6.1" });
			bmeta2.addPage(new String[] { ChatColor.RED + "CLASS LOADOUT\n\n " + ChatColor.BLACK + "We started work on a class system.\n\nAt the moment, you can only have one class, this will change soon!" });
			bmeta2.addPage(new String[] { ChatColor.RED + "UPDATES\n\n " + ChatColor.BLACK + "We added C4 to lethal slots" });
			bmeta2.addPage(new String[] { ChatColor.RED + "UPDATES\n\n " + ChatColor.BLACK + "We added 24 new maps!" });
			bmeta2.addPage(new String[] { ChatColor.RED + "UPDATES\n\n " + ChatColor.BLACK + "Explosions are no longer fireworks" });
			bmeta2.addPage(new String[] { ChatColor.RED + "BUG FIXES\n\n " + ChatColor.BLACK + "ONEIN gamemode now works normally!" });
			bmeta2.addPage(new String[] { ChatColor.RED + "BUG FIXES\n\n " + ChatColor.BLACK + "A lot of map bugs were fixed" });
			bmeta2.addPage(new String[] { ChatColor.RED + "BUG FIXES\n\n " + ChatColor.BLACK + "Experience bar now shows the correct amount of experience" });
			bmeta2.addPage(new String[] { ChatColor.RED + "BUG FIXES\n\n " + ChatColor.BLACK + "Your KDR should now display correctly" });
			book2.setItemMeta(bmeta2);

			this.player.getInventory().setItem(6, book2);
		}
	}

	public boolean hasTag(String tag)
	{
		for (int i = 0; i < this.boughtItems.size(); i++) {
			if (((KitItem)this.boughtItems.get(i)).tag.toLowerCase().equals(tag.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public ItemStack getItemStack(int id, KitEnchantment e) {
		ItemStack itm = new ItemStack(Material.getMaterial(id), 1);
		if (e != null) {
			e.add(itm);
		}
		return itm;
	}

	public void giveItem(Player p, int weapon1, KitEnchantment ench, byte dat, int amt, int slot) {
		if (weapon1 > 0) {
			Material mat = Material.getMaterial(weapon1);
			if ((mat != null) && 
					(!mat.equals(Material.AIR))) {
				ItemStack itm = new ItemStack(mat, amt, dat);
				if (ench != null) {
					ench.add(itm);
				}
				p.getInventory().setItem(slot, itm);
			}
		}
	}

	public String getRank()
	{
		if ((this.profile.tag != null) && 
				(this.clan != null)) {
			return this.clan.getName();
		}

		if (this.profile.level > 30) {
			return "31+";
		}
		switch (this.profile.level) { case 1:
			return "PVT";
		case 2:
			return "PV2";
		case 3:
			return "PFC";
		case 4:
			return "SPC";
		case 5:
			return "CPL";
		case 6:
			return "SGT";
		case 7:
			return "SSG";
		case 8:
			return "SFC";
		case 9:
			return "MSG";
		case 10:
			return "1SG";
		case 11:
			return "SGM";
		case 12:
			return "CSM";
		case 13:
			return "SMA";
		case 14:
			return "WO1";
		case 15:
			return "CW2";
		case 16:
			return "CW3";
		case 17:
			return "CW4";
		case 18:
			return "CW5";
		case 19:
			return "2LT";
		case 20:
			return "1LT";
		case 21:
			return "CPT";
		case 22:
			return "MAJ";
		case 23:
			return "LTC";
		case 24:
			return "COL";
		case 25:
			return "BG";
		case 26:
			return "MG";
		case 27:
			return "LTG";
		case 28:
			return "GEN";
		case 29:
			return "MLG";
		case 30:
			return "PRO";
		}
		return "NUB";
	}

	public String getTag() {
		ChatColor color = getTeamColor();
		String prefix = getRank();
		String lol = this.player.getName();
		String n = prefix + color + lol;
		return n;
	}

	public void calculate()
	{
		if (this.profile.xp >= this.profile.xpn) {
			this.profile.xp -= this.profile.xpn;
			this.profile.level += 1;

			if (this.profile.level < 1)
				this.profile.level = 1;
			this.profile.xpn = getXpto(this.profile.level);
			if (this.profile.xpn <= 0) {
				this.profile.xpn = 125;
			}
			this.player.playSound(this.player.getLocation(), Sound.LEVEL_UP, 20.0F, 1.0F);

			sayMessage(null, ChatColor.GREEN + "Level Gained!");
			sayMessage(null, ChatColor.GRAY + "        You are now level " + ChatColor.YELLOW + this.profile.level);
		}
	}

	public int getXpto(int level) {
		return (int)Math.floor(Math.pow(46 * level, 1.38D) / 10.0D) * 10;
	}

	private int getAmountXpToLevel(int level)
	{
		int count = 0;
		for (int i = 0; i < level; i++) {
			count += getXpto(i);
		}
		return count;
	}

	public void giveXp(int i) {
		this.profile.xp += i;
		this.profile.gainxp += i;
	}

	public void onDamagedByEvent(EntityDamageByEntityEvent event) {
		if (hasPerk("juggernaut") && !arena.gameModifier.equals(GameModeModifier.INFECT))
			event.setDamage((int)(event.getDamage() / 2.0D));
	}

	public void onDamage(double damage, DamageType dmgType, KitPlayer damager) {
		this.lastDamager = damager;
		if (dmgType.equals(DamageType.EXPLOSION)) {
			if (hasPerk("flakjacket"))
				damage *= 0.4;
		}
		
		this.player.damage(damage,damager.player);
		this.player.setLastDamage(0);
		
	}

	public void onAttack(EntityDamageByEntityEvent event) {
		if (hasPerk("stoppingpower"))
			event.setDamage(event.getDamage() + 2);
	}

	public void onKill(KitPlayer kp) {
		this.killStreak += 1;
		if (hasPerk("scavenger")) {
			addAmmo(10);
		}
		if (this.arena.gameModifier.equals(KitArena.GameModeModifier.GUNGAME)) {
			this.gungameLevel += 1;
			spawn_gunGame();
			String newGun = (String)this.arena.plugin.guns_gungame.get(this.gungameLevel);
			sayMessage(null, ChatColor.YELLOW + "RANK UP! gun:" + ChatColor.WHITE + newGun + ChatColor.YELLOW + "    +30" + ChatColor.WHITE + " xp");
		}
		doKillStreak();
	}

	private int getAmtAmmo(String string) {
		if (this.player == null)
			return 0;
		if (!this.player.isOnline())
			return 0;
		if (string.equals("primary")) {
			int itemid = this.plugin.getGunAmmo(this.kclass.primary);
			return InventoryHelper.amtItem(this.player.getInventory(), itemid);
		}
		int itemid = this.plugin.getGunAmmo(this.kclass.secondary);
		return InventoryHelper.amtItem(this.player.getInventory(), itemid);
	}

	public void addAmmo(int amt) {
		try {
			ItemStack itm1 = new ItemStack(this.plugin.getGunAmmo(this.kclass.primary), amt);
			ItemStack itm2 = new ItemStack(this.plugin.getGunAmmo(this.kclass.secondary), (int)(amt * 1.5D));
			int slot = InventoryHelper.getItemPosition(this.player.getInventory(), itm1.getType());
			int slot2 = InventoryHelper.getItemPosition(this.player.getInventory(), itm2.getType());

			if (slot > -1)
				this.player.getInventory().getItem(slot).setAmount(this.player.getInventory().getItem(slot).getAmount() + itm1.getAmount());
			else
				this.player.getInventory().setItem(InventoryHelper.getFirstFreeSlot(this.player.getInventory()), itm1);
			if (slot2 > -1)
				this.player.getInventory().getItem(slot2).setAmount(this.player.getInventory().getItem(slot2).getAmount() + itm2.getAmount());
			else
				this.player.getInventory().setItem(InventoryHelper.getFirstFreeSlot(this.player.getInventory()), itm2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onDeath() {
		this.killStreak = 0;
		this.alive = 0;
		this.lives -= 1;
		this.deaths += 1;
		this.dead = true;
		
		if (hasPerk("martyrdom")) {
			PVPGunPlus pvpgunplus = PVPGunPlus.getPlugin();
			GunPlayer gplayer = pvpgunplus.getGunPlayer(player);
			if (gplayer != null) {
				gplayer.forceFireGun("grenade");
			}
		}
			
	}

	public void doKillStreak() {
		int subt = 0;
		if (hasPerk("hardline"))
			subt = 1;
		if ((this.killStreak == 5 - subt) && (!this.arena.gameModifier.equals(KitArena.GameModeModifier.ONEINCHAMBER))) {
			for (int i = 0; i < 4; i++)
				sayMessage(null, ChatColor.AQUA + "5 Killstreak! Unlocked Ammo!");
			addAmmo(20);
		}

		if ((this.killStreak == 15 - subt) && (!this.arena.gameModifier.equals(KitArena.GameModeModifier.ONEINCHAMBER))) {
			this.arena.broadcastMessage(getTag() + ChatColor.BOLD + ChatColor.RED + " LAUNCHED EMP!");
			for (int i = this.arena.players.size() - 1; i >= 0; i--) {
				KitPlayer kp = (KitPlayer)this.arena.players.get(i);
				if ((kp != null) && 
						(kp.player != null) && (kp.player.isOnline()) && 
						(kp.team != this.team)) {
					PotionEffect pot = new PotionEffect(PotionEffectType.BLINDNESS, 300, 2);
					kp.player.addPotionEffect(pot);
				}

			}

		}

		if ((this.killStreak == 21 - subt) && (!this.arena.gameModifier.equals(KitArena.GameModeModifier.ONEINCHAMBER)) && (!this.arena.gameModifier.equals(KitArena.GameModeModifier.INFECT))) {
			this.arena.broadcastMessage(getTag() + ChatColor.BOLD + ChatColor.RED + " LAUNCHED TACTICAL NUKE!");
			for (int i = 0; i < this.arena.players.size(); i++) {
				arena.players.get(i).player.playSound(arena.players.get(i).player.getLocation(), Sound.GHAST_DEATH, 1, 1);
			}
			
			final KitPlayer kitplayer = this;
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					for (int i = arena.players.size() - 1; i >= 0; i--) {
						KitPlayer kp = (KitPlayer)arena.players.get(i);
						if ((kp != null) && 
								(kp.player != null) && (kp.player.isOnline()) && 
								(kp.team != team)) {
							kp.lastDamager = kitplayer;
							kp.player.damage(9999, player);
							ParticleEffects.sendParticle(null, 64, "hugeexplosion", kp.player.getLocation().clone().add(0, 1, 0), 0.3f, 0.3f, 0.3f, 0.2f, 1);
						}
					}
				}
			}, 20 * 7);
		}
	}

	public boolean hasPerk(String string)
	{
		for (int i = 0; i < this.perks.size(); i++) {
			Perk p = (Perk)this.perks.get(i);
			if (p.name.toLowerCase().equals(string.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public void checkInventory() {
		if (this.player.getItemOnCursor().getTypeId() == Material.WOOL.getId())
			this.player.setItemOnCursor(null);
	}

	public void CLEARPLAYER()
	{
		if (this.player != null)
			try {
				TabAPI.setPriority(this.plugin, this.player, -2);
				TabAPI.updatePlayer(this.player);
			}
		catch (Exception localException)
		{
		}
		this.profile.CLEAR();
		this.kclass = null;
		this.player = null;
		this.boughtItems.clear();
		for (int i = 0; i < this.perks.size(); i++) {
			((Perk)this.perks.get(i)).clear();
		}
		this.perks.clear();
	}

	public void setLastArenaStats(KitArenaStats mystats) {
		this.specialMessage = mystats;
	}

	public int getXpPerKill() {
		return this.arena.plugin.getKillCredits(this.player);
	}

	public int getCreditsEarned() {
		return this.profile.creditsGain;
	}

	public Clan getClan() {
		return this.clan;
	}

	public void inviteToClan(Clan clan) {
		this.clanInviteTimer = 60;
		this.clanInviteTo = clan.getName();
		sayMessage(null, ChatColor.GREEN + "INVITED TO CLAN: " + ChatColor.BLUE + clan.getName());
	}

	public void setClan(Clan clan) {
		this.clanInviteTimer = 0;
		this.clan = clan;
	}

	public void giveSpecialMessage(SpecialMessage msg) {
		this.specialMessage = msg;
		this.specialMessage.setPlayer(this);
	}
}