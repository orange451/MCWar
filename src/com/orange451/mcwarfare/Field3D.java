package com.orange451.mcwarfare;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitScheduler;

public class Field3D extends Field
{
  public World world;
  public int minz;
  public int maxz;
  public int height;
  public KitPvP plugin;

  public Field3D(double x, double y, double z, double x2, double y2, double z2)
  {
    setParam(x, y, z, x2, y2, z2);
  }

  public Field3D() {
  }

  public Field3D(KitPvP plugin, World world) {
    this.plugin = plugin;
    this.world = world;
  }

  public void setParam(double x, double y, double z, double x2, double y2, double z2) {
    setParam(x, y, x2, y2);

    this.minz = ((int)z);
    this.maxz = ((int)z2);

    if (this.minz > this.maxz) {
      this.maxz = this.minz;
      this.minz = ((int)z2);
    }

    this.height = (this.maxz - this.minz);
  }

  public Block getBlockAt(int i, int ii, int iii) {
    return this.world.getBlockAt(this.minx + i, this.minz + ii, this.miny + iii);
  }

  public boolean isInside(Location loc) {
    if (super.isInside(loc)) {
      int locy = loc.getBlockY();
      if ((locy >= this.minz) && (locy <= this.maxz)) {
        return true;
      }
    }
    return false;
  }

  public boolean isUnder(Location loc) {
    if ((super.isInside(loc)) && 
      (loc.getBlockY() < this.maxz)) {
      return true;
    }

    return false;
  }

  public void setType(Material mat) {
    setType(mat.getId());
  }

  public void setType(final int id) {
    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
      public void run() {
        for (int i = Field3D.this.minx; i <= Field3D.this.maxx; i++)
          for (int ii = Field3D.this.miny; ii <= Field3D.this.maxy; ii++)
            for (int iii = Field3D.this.minz; iii <= Field3D.this.maxz; iii++) {
              Block b = Field3D.this.world.getBlockAt(i, iii, ii);
              b.setTypeId(id);
            }
      }
    });
  }
}