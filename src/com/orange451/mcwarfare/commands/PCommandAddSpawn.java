package com.orange451.mcwarfare.commands;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.create.KitArenaCreator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PCommandAddSpawn extends PBaseCommand
{
  public PCommandAddSpawn(KitPvP plugin)
  {
    this.plugin = plugin;
    this.aliases.add("addspawn");
    this.aliases.add("as");

    this.mode = "hidden";

    this.desc = (ChatColor.YELLOW + "to add a MCWARFARE spawn");
  }

  public void perform()
  {
    KitArenaCreator kac = this.plugin.getKitArenaCreator(this.player);
    if (kac == null) {
      this.player.sendMessage("You need to be creating a kit arena!");
    } else {
      this.plugin.getKitArenaCreator(this.player).addSpawn();
      this.player.sendMessage("Added spawn!");
    }
  }
}