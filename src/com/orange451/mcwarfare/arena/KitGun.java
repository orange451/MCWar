package com.orange451.mcwarfare.arena;

public class KitGun
{
  public String name;
  public String desc;
  public String slot;
  public int cost;
  public int level;
  public int type;

  public KitGun(String name, String desc, int cost, int level, int type)
  {
    this.name = name;
    this.desc = desc;
    this.cost = cost;
    this.level = level;
    this.type = type;
  }

  public KitGun()
  {
  }

  public boolean isUnlocked(KitPlayer kp) {
    return kp.profile.level >= this.level;
  }
}