package com.orange451.mcwarfare.commands;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.KitGun;
import com.orange451.mcwarfare.arena.KitPlayer;
import com.orange451.mcwarfare.arena.KitProfile;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PCommandList extends PBaseCommand
{
  public PCommandList(KitPvP plugin)
  {
    this.plugin = plugin;
    this.aliases.add("list");
    this.aliases.add("li");

    this.desc = (ChatColor.YELLOW + "to view your MCWARFARE guns");
  }

  public void perform()
  {
    KitPlayer kp = this.plugin.getKitPlayer(this.player);
    if (kp != null) {
      kp.sayMessage(null, ChatColor.GRAY + "------" + ChatColor.YELLOW + "MCWAR GUNS" + ChatColor.GRAY + "------");
      String str = ChatColor.BLUE + "Listing your Guns: ";
      kp.sayMessage(null, str);
      str = "";
      for (int i = 0; i < this.plugin.loadedGuns.size(); i++) {
        String g = ((KitGun)this.plugin.loadedGuns.get(i)).name;
        boolean has = kp.profile.hasGun(g);
        if (has) {
          ChatColor color = ChatColor.GREEN;
          String send = color + g;
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
    } else {
      this.player.sendMessage("you are not in the war!");
    }
  }
}