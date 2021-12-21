package com.orange451.mcwarfare.commands;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.create.KitArenaCreator;
import com.orange451.opex.permissions.PermissionInterface;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PCommandCreate extends PBaseCommand
{
  public PCommandCreate(KitPvP plugin)
  {
    this.plugin = plugin;
    this.aliases.add("create");

    this.mode = "build";

    this.desc = (ChatColor.YELLOW + "to create a MCWARFARE arena");
  }

  public void perform()
  {
    if (PermissionInterface.hasPermission(this.player, "kitpvp.admin")) {
      String arenaName = (String)this.parameters.get(1);
      String arenaType = (String)this.parameters.get(2);
      if (this.plugin.getKitArena(arenaName) == null) {
        KitArenaCreator kac = this.plugin.getKitArenaCreator(this.player);
        if (kac == null)
          this.plugin.creatingArena.add(new KitArenaCreator(this.plugin, this.player, arenaName, arenaType));
        else
          this.player.sendMessage("You are already creating an arena!");
      }
      else {
        this.player.sendMessage("A Kit Arena with this name already exists!");
      }
    } else {
      this.player.sendMessage("You need perms brah!");
    }
  }
}