package com.orange451.mcwarfare.arena;

import com.orange451.mcwarfare.KitPvP;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class KitFlag
{
  public boolean isHolding = false;
  public KitFlagStand stand;
  public Item itemInWorld;
  public Item temp;
  public int teamColor;
  public Player carrier;
  public String flag;

  public KitFlag(KitFlagStand stand, int color)
  {
    this.stand = stand;
    this.teamColor = color;

    ChatColor c = ChatColor.BLUE;
    String name = "BLUE";
    if (this.teamColor == 14) {
      c = ChatColor.RED;
      name = "RED";
    }
    this.flag = (c + name);
  }

  public void spawn(Location add) {
    MaterialData data = new MaterialData(Material.WOOL.getId());
    data.setData((byte)this.teamColor);
    ItemStack itm = data.toItemStack(1);
    this.itemInWorld = add.getWorld().dropItem(add.clone().add(0.5D, 0.0D, 0.5D), itm);
    this.itemInWorld.setVelocity(new Vector(0, 0, 0));
  }

  public void tick() {
    if ((this.itemInWorld != null) && (!this.isHolding)) {
      this.itemInWorld.setTicksLived(64);
    }
    if (this.carrier != null) {
      KitPlayer k = this.stand.arena.plugin.getKitPlayer(this.carrier);
      if ((this.isHolding) && ((k == null) || (k.team.equals(KitArena.Teams.NEUTRAL)))) {
        drop();
      }
      if (this.carrier.isDead()) {
        drop();
      }
      else if (!this.isHolding) {
        Player c = this.carrier;
        removeFlag();
        this.carrier = c;
        this.isHolding = true;
        this.stand.arena.broadcastMessage(this.stand.arena.plugin.getKitPlayer(this.carrier).getTeamColor() + this.carrier.getName() + ChatColor.GOLD + " picked up the " + this.flag + ChatColor.GOLD + " flag!");
      } else {
        if (this.temp != null) {
          this.temp.remove();
          this.temp = null;
        }
        Location add = this.carrier.getLocation().add(0.0D, 1.5D, 0.0D);
        MaterialData data = new MaterialData(Material.WOOL.getId());
        data.setData((byte)this.teamColor);
        ItemStack itm = data.toItemStack(1);
        this.temp = add.getWorld().dropItem(add, itm);
        this.temp.setVelocity(new Vector(0.0D, 0.425D, 0.0D));
        this.temp.setPickupDelay(99999);
      }

    }
    else if (this.isHolding) {
      drop();
    }
  }

  public void drop() {
    if (this.temp != null) {
      this.temp.remove();
      this.temp = null;
    }
    if (this.stand != null) {
      KitPlayer kitPlayer = this.stand.arena.plugin.getKitPlayer(this.carrier);
      if ((this.carrier != null) && (kitPlayer != null))
        this.stand.arena.broadcastMessage(kitPlayer.getTeamColor() + this.carrier.getName() + ChatColor.GOLD + " dropped the " + this.flag + ChatColor.GOLD + " flag!");
    }
    removeFlag();
    this.stand.setup();
  }

  public void removeFlag() {
    try {
      this.itemInWorld.remove();
    }
    catch (Exception localException) {
    }
    if (this.temp != null) {
      this.temp.remove();
      this.temp = null;
    }
    this.isHolding = false;
    this.carrier = null;
  }
}