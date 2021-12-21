package com.orange451.mcwarfare.arena;

import com.orange451.mcwarfare.KitPvP;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class KitFlagStand
{
  public Location location;
  public int color;
  public KitFlag flag;
  public KitArena arena;
  public int capcheck = 0;
  public int ticks = 0;

  public KitFlagStand(KitArena arena, int color, Location loc) {
    this.arena = arena;
    this.location = loc;
    this.color = color;
    this.flag = new KitFlag(this, color);
  }

  public void setup() {
    this.location.getBlock().setType(Material.FENCE);
    this.location.clone().add(0.0D, -1.0D, 1.0D).getBlock().setType(Material.STONE);
    this.location.clone().add(0.0D, -1.0D, -1.0D).getBlock().setType(Material.STONE);
    this.location.clone().add(1.0D, -1.0D, 0.0D).getBlock().setType(Material.STONE);
    this.location.clone().add(-1.0D, -1.0D, 0.0D).getBlock().setType(Material.STONE);
    this.location.clone().add(0.0D, -1.0D, 0.0D).getBlock().setType(Material.GOLD_BLOCK);
    this.flag.spawn(this.location.clone().add(0.0D, 2.0D, 0.0D));
  }

  public void stop() {
    try {
      this.flag.removeFlag();
    }
    catch (Exception localException) {
    }
  }

  public KitFlagStand getOtherFlagStand() {
    for (int i = 0; i < this.arena.flags.size(); i++) {
      if (((KitFlagStand)this.arena.flags.get(i)).color != this.color) {
        return (KitFlagStand)this.arena.flags.get(i);
      }
    }
    return null;
  }

  public KitFlag getOtherFlag() {
    return getOtherFlagStand().flag;
  }

  public void score() {
    if (this.flag.teamColor == 11)
      this.arena.bluescore += 1;
    else
      this.arena.redscore += 1;
  }

  public void tick()
  {
    this.ticks += 1;
    this.flag.tick();

    if (this.ticks == 2) {
      setup();
    }
    if (this.ticks == 3) {
      List nearby = this.flag.itemInWorld.getNearbyEntities(4.0D, 4.0D, 4.0D);
      for (int i = nearby.size() - 1; i >= 0; i--) {
        if ((nearby.get(i) instanceof Item)) {
          Item it = (Item)nearby.get(i);
          if (it.getEntityId() != this.flag.itemInWorld.getEntityId()) {
            it.remove();
          }
        }
      }
    }
    if (getOtherFlag().isHolding)
      for (int i = 0; i < Bukkit.getServer().getOnlinePlayers().length; i++) {
        Player p = Bukkit.getServer().getOnlinePlayers()[i];
        if ((p != null) && 
          (!p.isDead())) {
          KitPlayer kp = this.arena.plugin.getKitPlayer(p);
          if ((kp != null) && 
            (kp.getWoolColor() == this.flag.teamColor))
            try {
              if ((getOtherFlag().carrier.getName().equals(kp.player.getName())) && 
                (p.getLocation().distanceSquared(this.location) < 3.5D)) {
                this.arena.broadcastMessage(this.arena.plugin.getKitPlayerName(p) + ChatColor.AQUA + " captured the" + getOtherFlag().flag + ChatColor.AQUA + "flag!");
                kp.giveXp(75);
                kp.profile.credits += 2;
                score();
                getOtherFlag().removeFlag();
                getOtherFlagStand().setup();
              }
            }
            catch (Exception localException)
            {
            }
        }
      }
  }
}