package com.orange451.mcwarfare.commands;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.KitPlayer;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PCommandHelp extends PBaseCommand
{
  public PCommandHelp(KitPvP plugin)
  {
    this.plugin = plugin;
    this.aliases.add("help");
    this.aliases.add("h");
    this.aliases.add("?");

    this.desc = (ChatColor.YELLOW + "to view MCWARFARE help");
  }

  public void perform()
  {
    KitPlayer kp = this.plugin.getKitPlayer(this.player);
    if (kp == null) {
      this.player.sendMessage("JOIN THE WAR FIRST   /war join");
    } else {
      kp.sayMessage(null, ChatColor.YELLOW + "/war buy " + ChatColor.WHITE + "to access buyable items");
      kp.sayMessage(null, ChatColor.YELLOW + "/war chat " + ChatColor.WHITE + "to turn chat on/off");
      kp.sayMessage(null, ChatColor.YELLOW + "/war gui " + ChatColor.WHITE + "to turn GUI on/off");
      kp.sayMessage(null, ChatColor.YELLOW + "/war gun " + ChatColor.WHITE + "to list guns you can buy");
      kp.sayMessage(null, ChatColor.YELLOW + "/war list " + ChatColor.WHITE + "to list guns you have");
      kp.sayMessage(null, ChatColor.YELLOW + "/war leave " + ChatColor.WHITE + "to leave the war");
    }
  }
}