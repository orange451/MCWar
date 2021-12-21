package com.orange451.mcwarfare;

import org.bukkit.Location;

public class Field
{
  public int minx;
  public int miny;
  public int maxx;
  public int maxy;
  public int width;
  public int length;

  public Field(double x, double z, double x2, double z2)
  {
    setParam(x, z, x2, z2);
  }

  public Field() {
  }

  public void setParam(double x, double z, double x2, double z2) {
    this.minx = ((int)x);
    this.miny = ((int)z);
    this.maxx = ((int)x2);
    this.maxy = ((int)z2);

    if (this.minx > this.maxx) {
      this.maxx = this.minx;
      this.minx = ((int)x2);
    }

    if (this.miny > this.maxy) {
      this.maxy = this.miny;
      this.miny = ((int)z2);
    }

    this.width = (this.maxx - this.minx);
    this.length = (this.maxy - this.miny);
  }

  public boolean isInside(Location loc) {
    int locx = loc.getBlockX();
    int locz = loc.getBlockZ();
    if ((locx >= this.minx) && (locx <= this.maxx) && 
      (locz >= this.miny) && (locz <= this.maxy)) {
      return true;
    }

    return false;
  }
}