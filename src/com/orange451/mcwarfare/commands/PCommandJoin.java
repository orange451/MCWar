package com.orange451.mcwarfare.commands;

import com.orange451.mcwarfare.KitPvP;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PCommandJoin extends PBaseCommand
{
  public PCommandJoin(KitPvP plugin)
  {
    this.plugin = plugin;
    this.aliases.add("join");
    this.aliases.add("j");

    this.desc = (ChatColor.YELLOW + "to join MCWARFARE");
  }

  public void perform()
  {
    if (this.plugin.isInArena(this.player))
      this.player.sendMessage("You're already in MCWARFARE...");
    else
      this.plugin.joinArena(this.player, null);
  }
}