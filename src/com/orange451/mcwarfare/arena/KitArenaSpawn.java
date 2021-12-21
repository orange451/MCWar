package com.orange451.mcwarfare.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class KitArenaSpawn
{
  public int lastSpawn = 999;
  public Location location;

  public void spawn(Player p)
  {
    p.teleport(this.location.clone().add(0.0D, 0.5D, 0.0D));
    this.lastSpawn = 999;
    p = null;
  }

  public void tick() {
    this.lastSpawn -= 1;
  }
}