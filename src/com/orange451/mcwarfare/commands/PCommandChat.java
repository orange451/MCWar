package com.orange451.mcwarfare.commands;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.KitPlayer;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PCommandChat extends PBaseCommand
{
  public PCommandChat(KitPvP plugin)
  {
    this.plugin = plugin;
    this.aliases.add("chat");

    this.desc = (ChatColor.YELLOW + "to turn off/on your chat");
  }

  public void perform()
  {
    KitPlayer kp = this.plugin.getKitPlayer(this.player);
    if (kp != null) {
      kp.receiveChat = (!kp.receiveChat);
      kp.clearChat();
      kp.sayMessage(null, ChatColor.GREEN + "Chat is on?  " + Boolean.toString(kp.receiveChat));
    } else {
      this.player.sendMessage("type" + ChatColor.BLUE + " /war join" + ChatColor.WHITE + "first!");
    }
  }
}