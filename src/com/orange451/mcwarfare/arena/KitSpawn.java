package com.orange451.mcwarfare.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class KitSpawn
{
  public Location location;
  public int lastSpawn;

  public KitSpawn(Location location)
  {
    this.location = location.clone().add(0.0D, 1.25D, 0.0D);
  }

  public void spawn(KitPlayer kp) {
    this.lastSpawn = 9999;
    kp.player.teleport(this.location.clone());
  }

  public void tick() {
    this.lastSpawn -= 1;
    if (this.lastSpawn < 0)
      this.lastSpawn += 400;
  }

  public Location getLocation() {
    return this.location;
  }
}