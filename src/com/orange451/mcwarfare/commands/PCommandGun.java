package com.orange451.mcwarfare.commands;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.KitArena;
import com.orange451.mcwarfare.arena.KitGun;
import com.orange451.mcwarfare.arena.KitPlayer;
import com.orange451.mcwarfare.arena.KitProfile;
import com.orange451.mcwarfare.arena.kits.KitClass;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PCommandGun extends PBaseCommand
{
	public PCommandGun(KitPvP plugin)
	{
		this.plugin = plugin;
		this.aliases.add("gun");
		this.aliases.add("g");

		this.desc = (ChatColor.YELLOW + "to buy/apply/view MCWarfare guns");
	}

	public void perform()
	{
		KitPlayer kp = this.plugin.getKitPlayer(this.player);
		if (kp != null) {
			try {
				String param = (String)this.parameters.get(1);

				if (param.equals("list")) {
					listAvailableGuns(this.player, kp);
					return;
				}

				if (param.equals("buy")) {
					String param2 = (String)this.parameters.get(2);
					KitGun kg = this.plugin.getGun(param2);
					if (kg != null) {
						if (!kp.profile.hasGun(param2)) {
							int cost = kg.cost;
							if (kg.isUnlocked(kp)) {
								if (kp.profile.credits >= cost) {
									kp.profile.credits -= cost;
									kp.sayMessage(null, "bought: " + ChatColor.YELLOW + param2);
									kp.profile.boughtGuns.add(param2);
									kp.profile.save(kp);
									return;
								}
								kp.sayMessage(null, "This gun costs: " + ChatColor.YELLOW + cost + ChatColor.WHITE + " credits!");
								return;
							}

							kp.sayMessage(null, "You cannot buy this gun yet!");
							return;
						}

						kp.sayMessage(null, "You already have this gun!");
						return;
					}

					kp.sayMessage(null, "This gun is not available at this time!");
					return;
				}

				if (kp.arena.type.equals("lobby")) {
					if (param.equals("apply")) {
						String param2 = (String)this.parameters.get(2);
						KitGun kg = this.plugin.getGun(param2);
						if (kp.profile.hasGun(param2)) {
							if (kg.slot.equals("primary")) {
								((KitClass)kp.profile.classes.get(kp.profile.myclass)).primary = param2;
								((KitClass)kp.profile.classes.get(kp.profile.myclass)).update();
								kp.sayMessage(null, "set primary to: " + ChatColor.YELLOW + param2);
							}

							if (kg.slot.equals("secondary")) {
								((KitClass)kp.profile.classes.get(kp.profile.myclass)).secondary = param2;
								((KitClass)kp.profile.classes.get(kp.profile.myclass)).update();
								kp.sayMessage(null, "set secondary to: " + ChatColor.YELLOW + param2);
							}
						} else {
							kp.sayMessage(null, "You dont have this gun!");
						}
					}
				}
				else kp.sayMessage(null, "You need to be in a lobby to do this!");
			}
			catch (Exception e)
			{
				listAvailableGuns(this.player, kp);
			}
		}
		else
			this.player.sendMessage("type" + ChatColor.BLUE + " /war join" + ChatColor.WHITE + "first!");
	}

	private void listAvailableGuns(Player player, KitPlayer kp)
	{
		kp.sayMessage(null, ChatColor.GRAY + "------" + ChatColor.YELLOW + "MCWAR GUNS" + ChatColor.GRAY + "------");
		String str = ChatColor.BLUE + "Listing available Guns: ";
		kp.sayMessage(null, str);
		str = "";
		for (int i = 0; i < this.plugin.loadedGuns.size(); i++) {
			String g = ((KitGun)this.plugin.loadedGuns.get(i)).name;
			boolean has = kp.profile.hasGun(g);
			if ((!has) && (this.plugin.getGun(g).isUnlocked(kp))) {
				ChatColor color = ChatColor.WHITE;
				String send = ChatColor.YELLOW + "[" + ((KitGun)this.plugin.loadedGuns.get(i)).cost + "]" + color + g;
				if (str.length() + send.length() > 42) {
					kp.sayMessage(null, str);
					str = send + " ";
				} else {
					str = str + send + " ";
				}
			}
		}

		if (str.length() > 0) {
			kp.sayMessage(null, str);
		}

		kp.sayMessage(null, ChatColor.GRAY + "----------------------");
	}
}