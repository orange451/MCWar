package com.orange451.mcwarfare.commands;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.opex.permissions.PermissionInterface;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PCommandReload extends PBaseCommand
{
  public PCommandReload(KitPvP plugin)
  {
    this.plugin = plugin;
    this.aliases.add("reload");
    this.aliases.add("r");

    this.mode = "hidden";

    this.desc = (ChatColor.YELLOW + "to reload");
  }

  public void perform()
  {
    if (PermissionInterface.hasPermission(this.player, "kitpvp.admin")) {
      this.plugin.onDisable();
      this.plugin.onEnable();
    } else {
      this.player.sendMessage("You need perms brah!");
    }
  }
}