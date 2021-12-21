package com.orange451.mcwarfare.commands;

import com.orange451.mcwarfare.KitPvP;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PCommandLeave extends PBaseCommand
{
  public PCommandLeave(KitPvP plugin)
  {
    this.plugin = plugin;
    this.aliases.add("leave");
    this.aliases.add("l");

    this.desc = (ChatColor.YELLOW + "to leave MCWARFARE");
  }

  public void perform()
  {
    if (this.plugin.isInArena(this.player))
      this.plugin.leaveArena(this.plugin.getKitPlayer(this.player));
    else
      this.player.sendMessage("You have already left war!");
  }
}