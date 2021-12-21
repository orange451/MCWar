package com.orange451.mcwarfare.commands;

import com.orange451.mcwarfare.KitPvP;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PBaseCommand
{
  public List<String> aliases;
  public CommandSender sender;
  public Player player;
  public String desc;
  public List<String> parameters;
  public KitPvP plugin;
  public String mode = "";

  public PBaseCommand() {
    this.aliases = new ArrayList();
  }

  public void execute(CommandSender sender, List<String> parameters) {
    this.sender = sender;
    this.parameters = parameters;

    if ((sender instanceof Player)) {
      this.player = ((Player)sender);
    }

    perform();
  }

  public String getdesc() {
    return this.desc;
  }

  public void perform()
  {
  }

  public List<String> getAliases() {
    return this.aliases;
  }

  public void sendMessage(String message) {
    if (this.player != null)
      this.sender.sendMessage(message);
    else
      System.out.println(message);
  }

  public void sendMessage(List<String> messages)
  {
    for (String message : messages)
      sendMessage(message);
  }
}