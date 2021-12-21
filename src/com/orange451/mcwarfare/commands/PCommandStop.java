package com.orange451.mcwarfare.commands;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.KitArena;
import com.orange451.opex.permissions.PermissionInterface;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PCommandStop extends PBaseCommand
{
  public PCommandStop(KitPvP plugin)
  {
    this.plugin = plugin;
    this.aliases.add("stop");

    this.mode = "hidden";

    this.desc = (ChatColor.YELLOW + "to stop MCWARFARE");
  }

  public void perform()
  {
    if (PermissionInterface.hasPermission(this.player, "kitpvp.admin")) {
      for (int i = this.plugin.activeArena.size() - 1; i >= 0; i--)
        try {
          ((KitArena)this.plugin.activeArena.get(i)).stop();
        }
        catch (Exception localException)
        {
        }
      this.plugin.stopped = true;
    } else {
      this.player.sendMessage("You need perms brah!");
    }
  }
}