package com.orange451.mcwarfare.listeners;

import com.orange451.mcwarfare.FileIO;
import com.orange451.mcwarfare.InventoryHelper;
import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.LaunchPad;
import com.orange451.mcwarfare.Util;
import com.orange451.mcwarfare.VoteForServer;
import com.orange451.mcwarfare.arena.Clan;
import com.orange451.mcwarfare.arena.KilledPlayer;
import com.orange451.mcwarfare.arena.KitArena;
import com.orange451.mcwarfare.arena.mapitem.C4;
import com.orange451.mcwarfare.arena.mapitem.MapItem;
import com.orange451.mcwarfare.arena.KitFlag;
import com.orange451.mcwarfare.arena.KitPlayer;
import com.orange451.mcwarfare.arena.SpecialMessage;
import com.orange451.opex.permissions.PermissionInterface;
import com.orange451.pvpgunplus.events.PVPGunPlusReloadGunEvent;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
//import com.vexsoftware.votifier.model.Vote;
//import com.vexsoftware.votifier.model.VotifierEvent;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

public class PluginPlayerListener
implements Listener
{
	private KitPvP plugin;

	public PluginPlayerListener(KitPvP plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player pl = event.getPlayer();
		if (pl != null) {
			this.plugin.stopMakingArena(pl);
			event.setQuitMessage(null);
			this.plugin.onQuit(pl);
			this.plugin.broadcastMessage(null, ChatColor.YELLOW + event.getPlayer().getName() + " has quit!");
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onCraft(CraftItemEvent event) {
		HumanEntity clicker = event.getWhoClicked();
		if (!clicker.isOp())
			event.setCancelled(true);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerChat(PlayerChatEvent event) {
		if (!event.isCancelled()) {
			Player p = event.getPlayer();
			if (this.plugin.getKitPlayer(p) != null) {
				this.plugin.sayMessage(event.getPlayer(), event.getMessage());
			} else {
				this.plugin.sayNonWarMessage(event.getPlayer(), event.getMessage());
			}

			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent event)
	{
		Player who = (Player)event.getWhoClicked();
		if (who != null) {
			KitPlayer csplayer = this.plugin.getKitPlayer(who);
			if (csplayer != null)
				csplayer.onClickItem(event);
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPVPGunPlusGunReload(PVPGunPlusReloadGunEvent event) {
		KitPlayer kp = this.plugin.getKitPlayer(event.getPlayer());
		if ((kp != null) && 
				(kp.hasPerk("sleightofhand")))
			event.setReloadTime(event.getReloadTime() / 2);
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerKick(PlayerKickEvent event)
	{
		Player player = event.getPlayer();
		if ((this.plugin.isInArena(player)) && 
				(this.plugin.isInArena(player.getLocation())) && 
				(event.getReason().equals("You moved too quickly :( (Hacking?)")))
			event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerReceiveNameTag(PlayerReceiveNameTagEvent event)
	{
		KitPlayer pl = this.plugin.getKitPlayer(event.getNamedPlayer());
		if (pl != null)
			event.setTag(pl.getTeamColor() + pl.player.getName());
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player pl = event.getPlayer();
		String path = KitPvP.getMcWar() + "/banned-players.txt";
		BufferedReader in = FileIO.file_text_open_read(path);
		String strLine = null;
		while ((strLine = FileIO.file_text_read_line(in)) != null) {
			if ((strLine.toLowerCase().equals(event.getPlayer().getName().toLowerCase())) && (event.getPlayer() != null)) {
				pl.kickPlayer("You are banned from this server!");
				event.setJoinMessage(null);
				pl = null;
			}
		}
		FileIO.file_text_close(in);

		if (pl != null) {
			this.plugin.onJoin(pl);

			if (event.getJoinMessage() != null) {
				event.setJoinMessage(null);
				this.plugin.broadcastMessage(null, ChatColor.YELLOW + event.getPlayer().getName() + " has joined!");
				this.plugin.joinArena(event.getPlayer(), null);
			}
			try
			{
				int servernum = this.plugin.getServerNumber();
				this.plugin.giveMessage(pl, ChatColor.AQUA + "Welcome to MC-warfare #" + servernum + "!");
			}
			catch (Exception localException) {
			}
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerDeath(PlayerDeathEvent event) {
		event.setDeathMessage(null);
		event.setDroppedExp(0);

		KitPlayer kp = this.plugin.getKitPlayer(event.getEntity());
		if (kp != null) {
			kp.lastSpawn = 0;
			if (kp.arena.timeSinceStart < 10)
				return;
			kp.arena.onDeath(kp);
			KitPlayer attacker = kp.lastDamager;
			if (attacker != null)
				new KilledPlayer(this.plugin, attacker.player, event.getEntity());
		}
		
		List<MapItem> mapItems = plugin.getMapItems();
		for (int i = mapItems.size() - 1; i >= 0; i--) {
			if (mapItems.get(i) instanceof C4) {
				C4 c4 = (C4)mapItems.get(i);
				if (c4.getOwner().name.equals(kp.name))
					c4.detonate();
			}
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		Player player = event.getPlayer();
		String[] split = event.getMessage().split(" ");
		split[0] = split[0].substring(1);
		String label = split[0];
		String[] args = new String[split.length - 1];
		for (int i = 1; i < split.length; i++) {
			args[(i - 1)] = split[i];
		}
		
		if ((label.equalsIgnoreCase("plugins")) || (label.equalsIgnoreCase("pl"))) {
			event.setCancelled(true);
			ArrayList<String> plugins = new ArrayList<String>();
			plugins.add("MCWarfare");
			plugins.add("PVPGun");
			plugins.add("NoCheatPlus");
			plugins.add("WorldEdit");
			String str = "Plugins (" + plugins.size() + ")";
			for (int i = 0; i < plugins.size(); i++) {
				str = str + " " + ChatColor.GREEN + (String)plugins.get(i);
				if (i < plugins.size() - 1) {
					str = str + ChatColor.WHITE + ",";
				}
			}
			player.sendMessage(str);
		}
		
		if ((label.equalsIgnoreCase("kill"))) {
			event.setCancelled(true);
		}
		
		if (label.equalsIgnoreCase("texture")) {
			event.setCancelled(true);
			player.setTexturePack("dl.dropbox.com/s/j544h4i03y8wbai/MCWar_1_6.zip");
		}
		
		if (label.equalsIgnoreCase("vote")) {
			event.setCancelled(true);
			this.plugin.giveMessage(player, ChatColor.GREEN + "" + ChatColor.BOLD + "VOTE FOR OUR SERVER AND GET GRENADES! Vote Here: " + ChatColor.RESET + ChatColor.RED + "http://vote.mcbrawl.com/");
		}

		if (label.equalsIgnoreCase("server")) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.GREEN + "You are in server #" + ChatColor.WHITE + this.plugin.getServerNumber());
		}

		if (label.equalsIgnoreCase("clan")) {
			event.setCancelled(true);
			try {
				if (args.length == 0) {
					player.sendMessage(ChatColor.GREEN + "Clan help");
					player.sendMessage(" " + ChatColor.DARK_AQUA + "/clan create {name} " + ChatColor.WHITE + " to create a clan " + ChatColor.YELLOW + " [2000] credits!");
					player.sendMessage(" " + ChatColor.DARK_AQUA + "/clan invite {name} " + ChatColor.WHITE + " to invite a player");
					player.sendMessage(" " + ChatColor.DARK_AQUA + "/clan join {name} " + ChatColor.WHITE + " to join a clan");
					player.sendMessage(" " + ChatColor.DARK_AQUA + "/clan info {name} " + ChatColor.WHITE + " to view your clan info");
					player.sendMessage(" " + ChatColor.DARK_AQUA + "/clan leave " + ChatColor.WHITE + " to leave your clan");
					player.sendMessage(" " + ChatColor.DARK_AQUA + "/clan mod {name} " + ChatColor.WHITE + " to mod/demote a user in the clan");
					player.sendMessage(" " + ChatColor.DARK_AQUA + "/clan rename {name} " + ChatColor.WHITE + " rename your clan" + ChatColor.YELLOW + " [750] credits!");
				} else {
					KitPlayer kp = this.plugin.getKitPlayer(player);
					if (kp != null) {
						if (args[0].equals("rename")) {
							if (kp.profile.credits >= 750) {
								if (kp.getClan() != null) {
									if (kp.getClan().getOwner().equals(kp.name)) {
										String map = args[1].replaceAll("[^\\p{Alpha}\\p{Digit}]+", "");
										if (map.equals(args[1])) {
											if (map.length() > 4) {
												kp.sayMessage(null, ChatColor.RED + "Your clan name cannot be longer than 4 characters");
												return;
											}
											map.replace(" ", "");
											String check = map.toLowerCase();
											if ((check.contains("op")) || (check.contains("adm")) || (check.contains("mod")) || (check.contains("onr")) || (check.contains("0p")) || (check.contains("m0d"))) {
												kp.sayMessage(null, ChatColor.RED + "This name is not allowed!");
												return;
											}
											boolean nameExists = false;
											List clans = this.plugin.getClans();
											for (int i = 0; i < clans.size(); i++) {
												if (!((Clan)clans.get(i)).getFilename().equals(kp.getClan().getFilename())) {
													Clan c = (Clan)clans.get(i);
													if (c.getName().toLowerCase().equals(check))
														nameExists = true;
													if (c.getFilename().toLowerCase().equals(check))
														nameExists = true;
												}
											}
											if (check.toLowerCase().equals(kp.getClan().getFilename().toLowerCase())) {
												nameExists = false;
											}
											if (!nameExists) {
												kp.getClan().setName(map);
												kp.profile.credits -= 750;
												kp.sayMessage(null, ChatColor.GREEN + "Clan renamed to " + ChatColor.WHITE + map);
											} else {
												kp.sayMessage(null, ChatColor.RED + "This name is not available!");
											}
										} else {
											kp.sayMessage(null, ChatColor.RED + "Your name contains invaid characters!");
										}
									} else {
										kp.sayMessage(null, ChatColor.RED + "You need to be the clan owner to do this!");
									}
								}
								else kp.sayMessage(null, ChatColor.RED + "You are not in a clan!");
							}
							else {
								kp.sayMessage(null, ChatColor.RED + "You do not have enough money!");
							}
						}
						if (args[0].equals("create")) {
							if (kp.getClan() != null) {
								kp.sayMessage(null, ChatColor.RED + "Leave your clan first!");
								return;
							}
							if (kp.profile.level > 30) {
								if (kp.profile.credits >= 2000) {
									String map = args[1].replaceAll("[^\\p{Alpha}\\p{Digit}]+", "");
									if (map.equals(args[1])) {
										if (map.length() > 4) {
											map = map.substring(0, 4);
										}
										map.replace(" ", "");
										String check = map.toLowerCase();
										if (((!PermissionInterface.hasPermission(player, "mcwar.admin")) || (!PermissionInterface.hasPermission(player, "mcwar.mod"))) && (
												(check.contains("op")) || (check.contains("adm")) || (check.contains("mod")) || (check.contains("onr")) || (check.contains("0p")) || (check.contains("m0d")))) {
											kp.sayMessage(null, ChatColor.RED + "This name is not allowed!");
											return;
										}

										Clan temp = this.plugin.getClanByTag(map);
										Clan temp2 = this.plugin.getClanByFile(map);
										if ((temp == null) || ((temp != null) && (temp.getMembers().size() == 0))) {
											if ((temp2 == null) || ((temp2 != null) && (temp2.getMembers().size() == 0))) {
												Clan c = this.plugin.createClan(map);
												c.join(kp.name);
												kp.setClan(c);
												kp.profile.credits -= 1000;
											} else {
												kp.sayMessage(null, ChatColor.RED + "THIS CLAN ALREAY EXISTS");
											}
										}
										else kp.sayMessage(null, ChatColor.RED + "THIS CLAN ALREAY EXISTS");
									}
									else
									{
										kp.sayMessage(null, ChatColor.RED + "THIS NAME CANNOT BE USED");
									}
								} else {
									kp.sayMessage(null, ChatColor.RED + "You need at least 1000 credits!");
								}
							}
							else kp.sayMessage(null, ChatColor.RED + "You need to be level 31+ to make a clan!");
						}
						else if (args[0].equals("invite")) {
							if (kp.getClan() != null)
								if ((kp.getClan().getOwner().equals(kp.name)) || (kp.getClan().isModerator(kp.name))) {
									Player pln = Util.MatchPlayer(args[1]);
									if (pln != null) {
										KitPlayer np = this.plugin.getKitPlayer(pln);
										if (np != null)
											np.inviteToClan(kp.getClan());
									}
								}
								else {
									kp.sayMessage(null, ChatColor.RED + "You do not have permission to invite players");
								}
						}
						else if (args[0].equals("kick")) {
							if (kp.getClan() != null)
								if ((kp.getClan().getOwner().equals(kp.name)) || (kp.getClan().isModerator(kp.name))) {
									Player pln = Util.MatchPlayer(args[1]);
									if (pln != null) {
										KitPlayer np = this.plugin.getKitPlayer(pln);
										if ((np != null) && (np.getClan().equals(kp.getClan())) && (!kp.getClan().getOwner().equals(pln.getName()))) {
											np.sayMessage(null, ChatColor.RED + "You have been kicked from your clan");
											np.getClan().leave(np.player.getName());
											np.setClan(null);
										}
									}
								}
								else
								{
									kp.sayMessage(null, ChatColor.RED + "You do not have permission to invite players");
								}
						}
						else if (args[0].equals("mod")) {
							if (kp.getClan() != null)
								if (kp.getClan().getOwner().equals(kp.name)) {
									Player pln = Util.MatchPlayer(args[1]);
									if (pln != null) {
										KitPlayer np = this.plugin.getKitPlayer(pln);
										if ((np != null) && 
												(np.getClan().equals(kp.getClan()))) {
											kp.getClan().doModerator(np.player.getName());
											if (kp.getClan().isModerator(np.player.getName())) {
												kp.sayMessage(null, "Player: " + ChatColor.GREEN + np.player.getName() + ChatColor.WHITE + " is now a moderator");
												np.sayMessage(null, ChatColor.LIGHT_PURPLE + "You are now a clan moderator!");
											} else {
												kp.sayMessage(null, "Player: " + ChatColor.GREEN + np.player.getName() + ChatColor.WHITE + " is no longer a moderator");
												np.sayMessage(null, ChatColor.LIGHT_PURPLE + "You are no longer a clan moderator :(");
											}
										}
									}
								}
								else {
									kp.sayMessage(null, ChatColor.RED + "You do not have permission to invite players");
								}
						}
						else if (args[0].equals("setowner")) {
							if (kp.getClan() != null)
								if (kp.getClan().getOwner().equals(kp.name)) {
									Player pln = Util.MatchPlayer(args[1]);
									if (pln != null) {
										KitPlayer np = this.plugin.getKitPlayer(pln);
										if ((np != null) && 
												(np.getClan().equals(kp.getClan()))) {
											kp.getClan().setOwner(np.name);
											np.sayMessage(null, ChatColor.LIGHT_PURPLE + "You are now the owner of " + ChatColor.YELLOW + kp.getClan().getName());
											kp.sayMessage(null, ChatColor.LIGHT_PURPLE + "You are no longer the owner of " + ChatColor.YELLOW + kp.getClan().getName());
										}
									}
								}
								else {
									kp.sayMessage(null, ChatColor.RED + "You do not have permission to invite players");
								}
						}
						else if (args[0].equals("join")) {
							if (kp.getClan() == null) {
								if (kp.clanInviteTo.length() > 0) {
									Clan clan = this.plugin.getClanByTag(kp.clanInviteTo);
									if (clan != null) {
										this.plugin.joinClan(kp, clan);
										kp.setClan(clan);
									}
								} else {
									kp.sayMessage(null, ChatColor.RED + "You are not invited to this clan!");
								}
							}
							else kp.sayMessage(null, ChatColor.RED + "Leave your current clan first!");
						}
						else if (args[0].equals("leave")) {
							Clan clan = kp.getClan();
							if (clan != null) {
								clan.leave(kp.name);
								kp.sayMessage(null, ChatColor.RED + "LEFT CLAN!");
								kp.setClan(null);
							}
						} else if (args[0].equals("info")) {
							if (args.length == 1) {
								Clan clan = kp.getClan();
								if (clan != null) {
									SpecialMessage msg = new SpecialMessage();
									msg.lines[0] = ("" + ChatColor.GOLD + ChatColor.BOLD + "CLAN: " + ChatColor.RESET + ChatColor.AQUA + clan.getName());
									msg.lines[2] = (ChatColor.WHITE + "OWNER:         " + ChatColor.GREEN + clan.getOwner());
									msg.lines[3] = (ChatColor.WHITE + "MODERATORS:  " + ChatColor.GREEN + Integer.toString(clan.getModerators().size()));
									msg.lines[4] = (ChatColor.WHITE + "MEMBERS:      " + ChatColor.GREEN + Integer.toString(clan.getMembers().size()));
									kp.giveSpecialMessage(msg);
								}
							} else if (args.length == 2) {
								Clan clan = this.plugin.getClanByTag(args[1]);
								if (clan != null) {
									SpecialMessage msg = new SpecialMessage();
									msg.lines[0] = ("" + ChatColor.GOLD + ChatColor.BOLD + "CLAN: " + ChatColor.RESET + ChatColor.AQUA + clan.getName());
									msg.lines[2] = (ChatColor.WHITE + "OWNER:    " + ChatColor.GREEN + clan.getOwner());
									msg.lines[3] = (ChatColor.WHITE + "MEMBERS: " + ChatColor.GREEN + Integer.toString(clan.getMembers().size()));
									kp.giveSpecialMessage(msg);
								} else {
									kp.sayMessage(null, ChatColor.RED + "This clan doesn't exist!");
								}
							}
						}
					} else {
						this.plugin.giveMessage(player, "Cannot use this command!");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (label.equalsIgnoreCase("buy")) {
			event.setCancelled(true);
			String msg = "Please visit: ";
			this.plugin.giveMessage(player, msg);
			msg = ChatColor.DARK_AQUA + "http://mcbrawl.com/pages/shop/" + ChatColor.WHITE + " to shop!";
			this.plugin.giveMessage(player, msg);
		}
		if (label.equalsIgnoreCase("help")) {
			event.setCancelled(true);
			this.plugin.giveMessage(player, ChatColor.YELLOW + "--------MCWAR HELP--------");
			this.plugin.giveMessage(player, ChatColor.DARK_AQUA + "/war join" + ChatColor.WHITE + " to play!");
			this.plugin.giveMessage(player, ChatColor.DARK_AQUA + "/war leave" + ChatColor.WHITE + " to leave!");
			this.plugin.giveMessage(player, ChatColor.DARK_AQUA + "/buy" + ChatColor.WHITE + " to donate!");
		}
		if (label.equalsIgnoreCase("rules")) {
			event.setCancelled(true);
			this.plugin.giveMessage(player, ChatColor.RED + "1) NO CHEATING, GLITCHING, SPAMMING");
			this.plugin.giveMessage(player, ChatColor.RED + "2) NO EXPLOITING");
			this.plugin.giveMessage(player, ChatColor.RED + "3) NO ARGUING WITH MODS OR ADMINS");
		}
		if (label.equalsIgnoreCase("who")) {
			event.setCancelled(true);
			this.plugin.giveMessage(player, "players online: " + ChatColor.YELLOW + Util.Who().size());
		}
		if ((label.equalsIgnoreCase("test")) && 
				(PermissionInterface.hasPermission(player, "mcwar.admin"))) {
			this.plugin.onStartLobby();
		}

		if (label.equalsIgnoreCase("fp")) {
			if ((PermissionInterface.hasPermission(player, "mcwar.admin")) || (PermissionInterface.hasPermission(player, "mcwar.mod"))) {
				KitArena ka = this.plugin.getFirstKitArena("lobby");
				if (args.length == 0) {
					ka.tomap = this.plugin.getRandomKitArena(ka, ka.last, ka.getActivePlayers());
				} else {
					String map = args[0];
					KitArena gmap = this.plugin.getKitArena(map);
					if ((!ka.stopped) && (gmap != null)) {
						ka.tomap = gmap;
						ka.announceMap();
					}
				}
			}
			else if (args.length > 0) {
				player.kickPlayer("You are not allowed to glitch on this server");
			} else {
				this.plugin.giveMessage(player, ChatColor.GRAY + "I wouldn't if I were you");
			}

		}

		if (label.equalsIgnoreCase("tag")) {
			KitPlayer kp = this.plugin.getKitPlayer(player);
			if (kp != null) {
				kp.sayMessage(null, "" + ChatColor.RED + ChatColor.BOLD + "Sorry you need to use /clan!");
			}
			event.setCancelled(true);
		}

		if (label.equalsIgnoreCase("codclient")) {
			this.plugin.addCodClient(player.getName());
			event.setCancelled(true);
		}

		if (label.equalsIgnoreCase("spawn")) {
			KitPlayer pl = this.plugin.getKitPlayer(player);
			if (pl != null) {
				this.plugin.leaveArena(pl);
				this.plugin.joinArena(player, null);
			}
		}

		int powers = 0;
		if (player.isOp() || PermissionInterface.hasPermission(player, "mcwar.admin"))
			powers = 2;
		else if (PermissionInterface.hasPermission(player, "mcwar.mod"))
			powers = 1;

		if (label.equalsIgnoreCase("kick"))
		{
			if (powers >= 1)
			{
				if (args.length < 1)
				{
					player.sendMessage("Usage: /kick [players...]");
					return;
				}
				int count = args.length;
				if (count > 1 && powers < 2)
				{
					count = 1;
					player.sendMessage("You may only kick 1 player at a time.");
				}
				for (int i = 0; i != count; ++i)
				{
					Player target = plugin.getServer().getPlayerExact(args[i]);
					if (target == null)
					{
						player.sendMessage("Unnable to find: \"" + args[i] + "\", make sure you use the exact name/case.");
						continue;
					}
					target.kickPlayer("You have been kicked by: " + player.getName());
					player.sendMessage("Kicked: " + target.getName());
				}

			}
			else
				player.sendMessage("You do not have access to this command.");

		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player pl = event.getPlayer();
		if ((pl != null) && (event.getTo() != null) && (event.getFrom() != null)) {
			double dist = event.getFrom().distanceSquared(event.getTo());
			if (dist > 0.0D)
			{
				try
				{
					Block b = pl.getLocation().add(0.0D, -1.0D, 0.0D).getBlock();
					Material mat = b.getType();
					if (mat.equals(Material.SPONGE)) {
						LaunchPad lp = LaunchPad.getLaunchPad(b);
						if (lp != null)
							lp.launch(pl);
					}
				}
				catch (Exception localException)
				{
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		Player pl = event.getPlayer();
		if (pl != null) {
			KitPlayer kp = this.plugin.getKitPlayer(pl);
			if (kp != null)
				kp.lastLoc = event.getTo();
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Player pl = event.getPlayer();
		if ((pl != null) && 
				(this.plugin.isInArena(pl))) {
			KitPlayer kp = this.plugin.getKitPlayer(pl);
			Location loc = kp.arena.getSpawnLocation(kp);
			event.setRespawnLocation(loc);
			kp.isSpawning = true;
			kp.spawnLoc = loc;
			kp.lastSpawn = 3;
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		event.setCancelled(true);
		Player pl = event.getPlayer();
		if ((pl != null) && 
				(this.plugin.isInArena(pl)) && 
				(this.plugin.isInArena(pl.getLocation()))) {
			KitPlayer kp = this.plugin.getKitPlayer(pl);
			if ((kp != null) && 
					(kp.arena.gameModifier.equals(KitArena.GameModeModifier.CTF))) {
				KitFlag kf = kp.arena.getFlag(event.getItem());
				if ((kf != null) && 
						(kf.teamColor != kp.getWoolColor())) {
					kf.carrier = pl;
					event.getItem().remove();
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player pl = event.getPlayer();
		if ((pl != null) && 
				(this.plugin.isInArena(pl.getLocation()))) {
			event.getItemDrop().remove();
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		ItemStack iteminhand = player.getItemInHand();
		if ((iteminhand != null) && (iteminhand.getType().equals(Material.ENDER_PEARL))) {
			event.setCancelled(true);
		}

		Material mat = iteminhand.getType();
		if (mat.equals(Material.EYE_OF_ENDER) && event.getAction().toString().toLowerCase().contains("right")) {
			KitPlayer kp = this.plugin.getKitPlayer(player);
			if (kp != null) {
				event.setCancelled(true);
				kp.openGunMenu();
			}
		}
		
		if (mat.equals(Material.SHEARS)) {
			KitPlayer kp = this.plugin.getKitPlayer(player);
			if (kp != null) {
				List<MapItem> mapItems = plugin.getMapItems();
				for (int i = mapItems.size() - 1; i >= 0; i--) {
					if (mapItems.get(i) instanceof C4) {
						C4 c4 = (C4)mapItems.get(i);
						if (c4.getOwner().name.equals(kp.name)) {
							c4.detonate();
						}
					}
				}
			}
			player.setItemInHand(null);
		}
		if (event.hasBlock()) {
			try {
				if (iteminhand != null) {
					String holding = iteminhand.getType().toString().toLowerCase();
					if (iteminhand.getTypeId() == Material.LEVER.getId()) {
						KitPlayer kp = this.plugin.getKitPlayer(player);
						if (kp != null) {
							if (iteminhand.getTypeId() == Material.LEVER.getId() && event.getAction().toString().toLowerCase().contains("right")) {
								Location loc = event.getClickedBlock().getLocation().add(event.getBlockFace().getModX(), event.getBlockFace().getModY(), event.getBlockFace().getModZ());
								if (loc.getBlock().getType().equals(Material.AIR)) {
									//event.setCancelled(true);
									//loc.getBlock().setType(Material.LEVER);
									plugin.getMapItems().add(new C4(plugin, kp, loc));
									int amtShears = InventoryHelper.amtItem(event.getPlayer().getInventory(), Material.SHEARS.getId());
									if (amtShears == 0)
										event.getPlayer().getInventory().addItem(new ItemStack(Material.SHEARS));
									event.getPlayer().updateInventory();
									return;
								}
							}
						}
					}
					if (iteminhand.getTypeId() == 397) {
						KitPlayer kp = this.plugin.getKitPlayer(player);
						if (kp != null) {
							event.setCancelled(true);
						}
					}
					if (((event.getClickedBlock().getType().equals(Material.DIRT)) || (event.getClickedBlock().getType().equals(Material.GRASS)) || (event.getClickedBlock().getType().equals(Material.SOIL))) && 
							(holding.contains("hoe"))) {
						event.setCancelled(true);
					}

					if ((event.getClickedBlock().getType().equals(Material.CHEST)) || 
							(event.getClickedBlock().getType().equals(Material.DISPENSER)) || 
							(event.getClickedBlock().getType().equals(Material.DROPPER)) ||
							(event.getClickedBlock().getType().equals(Material.HOPPER)) ||
							(event.getClickedBlock().getType().equals(Material.BED)) ||
							(event.getClickedBlock().getType().equals(Material.ANVIL)) ||
							(event.getClickedBlock().getType().equals(Material.ENCHANTMENT_TABLE)) ||
							(event.getClickedBlock().getType().equals(Material.LEVER)) ||
							((event.getClickedBlock().getType().equals(Material.FURNACE)) && (!player.isOp())))
						event.setCancelled(true);
				}
			}
			catch (Exception localException)
			{
			}
			Block block = event.getClickedBlock();
			if ((block.getState() instanceof Sign)) {
				Sign s = (Sign)block.getState();
				String line1 = s.getLine(0).toLowerCase();
				String line2 = s.getLine(1).toLowerCase();
				if (this.plugin.isInArena(player)) {
					if (line1.contains("buy")) {
						player.chat("/war gun buy " + line2);
					}
					if (line1.contains("perk")) {
						this.plugin.setPerk(player, line2);
					}
					if ((line1.contains("knife")) && 
							(line2 != null)) {
						KitPlayer kp = this.plugin.getKitPlayer(player);
						if (kp != null) {
							if (line2.contains("normal")) {
								kp.profile.knife = 1;
								kp.sayMessage(null, ChatColor.WHITE + "Set knife to " + ChatColor.YELLOW + "default");
							} else if (line2.contains("superknife")) {
								if (PermissionInterface.hasPermission(player, "mcwar.superknife")) {
									kp.profile.knife = 2;
									kp.sayMessage(null, ChatColor.WHITE + "Set knife to " + ChatColor.YELLOW + "superknife");
									kp.profile.save(kp);
								} else {
									kp.sayMessage(null, ChatColor.RED + "You cannot use this knife");
								}
							}
						}
					}

					if (line1.contains("choose")) {
						player.chat("/war gun apply " + line2);
					}
					if (line1.contains("bought")) {
						player.chat("/war list");
					}
					if (line1.contains("unlocked")) {
						player.chat("/war gun list");
					}
				}
				if (line1.contains("givegun"))
					this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "givegun " + player.getName() + " " + line2);
			}
		}
	}

	/*@EventHandler(priority=EventPriority.NORMAL)
	public void onVotifierEvent(VotifierEvent event) {
		Vote vote = event.getVote();
		String v = vote.getUsername();
		new VoteForServer(v);
		Player pl = Util.MatchPlayer(v);
		if (pl != null)
			this.plugin.giveMessage(pl, "" + ChatColor.BOLD + ChatColor.RED + "THANKS FOR VOTING!");
	}*/
}