package com.orange451.mcwarfare.arena.create;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.Util;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class KitArenaCreator
{
  public Player player;
  public Location corner1;
  public Location corner2;
  public Location spawnloc;
  public Location spawnloc2;
  public KitPvP plugin;
  public String arenaName;
  public String arenaType;
  public String modifier;
  public ArrayList<Location> spawns = new ArrayList();

  public KitArenaCreator(KitPvP plugin, Player player, String arenaName2, String arenaType) {
    this.player = player;
    this.plugin = plugin;
    this.arenaType = arenaType;
    this.arenaName = arenaName2;
    this.modifier = "";
    player.sendMessage(ChatColor.GRAY + "STARTING TO CREATE ARENA!");
    player.sendMessage(ChatColor.GRAY + "  PLEASE SET CORNER 1 LOCATION");
    player.sendMessage(ChatColor.LIGHT_PURPLE + "    /kit setpoint");
  }

  public void setPoint() {
    Location ploc = this.player.getLocation();
    boolean changed = false;
    if (this.corner1 == null) {
      this.corner1 = ploc;
      changed = true;
      this.player.sendMessage(ChatColor.GRAY + "CORNER 1 LOCATION SET");
      this.player.sendMessage(ChatColor.GRAY + "  SET CORNER 2 LOCATION");
      return;
    }
    if (this.corner2 == null) {
      this.corner2 = ploc;
      changed = true;
      this.player.sendMessage(ChatColor.GRAY + "CORNER 2 LOCATION SET");
      this.player.sendMessage(ChatColor.GRAY + "  SET SPAWN LOCATION (for blue)");
      return;
    }
    if (this.spawnloc == null) {
      this.spawnloc = ploc;
      changed = true;
      this.player.sendMessage(ChatColor.GRAY + "blue SPAWN LOCATION SET");
      this.player.sendMessage(ChatColor.GRAY + "  SET SPAWN LOCATION (for red)");
      return;
    }
    if (this.spawnloc2 == null) {
      this.spawnloc2 = ploc;
      changed = false;
      this.player.sendMessage(ChatColor.GRAY + "red SPAWN LOCATION SET");
      this.player.sendMessage(ChatColor.GRAY + "  ...ATTEMPTING TO SAVE ARENA...");
    }

    if (!changed) {
      finish();
    }

    this.plugin.loadArena(this.arenaName);
  }

  public void finish() {
    this.plugin.stopMakingArena(this.player);
    saveArena();
  }

  private void saveArena()
  {
    if (this.arenaType.equals("onein")) {
      this.arenaType = "ffa";
      this.modifier = "OneInChamber";
    }

    if (this.arenaType.equals("ctf")) {
      this.arenaType = "tdm";
      this.modifier = "ctf";
    }

    if (this.arenaType.equals("infect")) {
      this.arenaType = "tdm";
      this.modifier = "infect";
    }

    if (this.arenaType.equals("gungame")) {
      this.arenaType = "ffa";
      this.modifier = "gungame";
    }

    String path = this.plugin.getRoot().getAbsolutePath() + "/arenas/" + this.arenaName;
    FileWriter outFile = null;
    PrintWriter out = null;
    try {
      outFile = new FileWriter(path);
      out = new PrintWriter(outFile);
      out.println(this.corner1.getBlockX() + "," + this.corner1.getBlockZ());
      out.println(this.corner2.getBlockX() + "," + this.corner2.getBlockZ());
      out.println(this.spawnloc.getBlockX() + "," + this.spawnloc.getBlockY() + "," + this.spawnloc.getBlockZ());
      out.println(this.spawnloc2.getBlockX() + "," + this.spawnloc2.getBlockY() + "," + this.spawnloc2.getBlockZ());

      out.println("--config--");
      out.println("type=" + this.arenaType);
      out.println("maxPlayers=" + Util.server.getMaxPlayers());
      out.println("minPlayers=" + (Util.server.getMaxPlayers() - 40));
      out.println("modifier=" + this.modifier);
      if (this.spawns.size() > 0) {
        for (int i = 0; i < this.spawns.size(); i++) {
          out.println("addspawn=" + ((Location)this.spawns.get(i)).getBlockX() + "," + ((Location)this.spawns.get(i)).getBlockY() + "," + ((Location)this.spawns.get(i)).getBlockZ());
        }
      }

      System.out.println("KITARENA: " + this.arenaName + " SUCCESFULLY SAVED!");
      this.player.sendMessage(ChatColor.YELLOW + "KitArena Saved!");
    }
    catch (IOException localIOException)
    {
    }
    try {
      out.close();
      outFile.close();
    }
    catch (Exception localException) {
    }
  }

  public void addSpawn() {
    this.spawns.add(this.player.getLocation().clone());
  }
}