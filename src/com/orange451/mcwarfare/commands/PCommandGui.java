package com.orange451.mcwarfare.commands;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.KitPlayer;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PCommandGui extends PBaseCommand
{
  public PCommandGui(KitPvP plugin)
  {
    this.plugin = plugin;
    this.aliases.add("gui");

    this.desc = (ChatColor.YELLOW + "to turn off/on your display GUI");
  }

  public void perform()
  {
    KitPlayer kp = this.plugin.getKitPlayer(this.player);
    if (kp != null)
      kp.displayGUI = (!kp.displayGUI);
    else
      this.player.sendMessage("type" + ChatColor.BLUE + " /war join" + ChatColor.WHITE + "first!");
  }
}