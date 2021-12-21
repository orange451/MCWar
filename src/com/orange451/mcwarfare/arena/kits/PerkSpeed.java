package com.orange451.mcwarfare.arena.kits;

import org.bukkit.entity.Player;

public class PerkSpeed extends Perk
{
  public PerkSpeed(Player p)
  {
    super(p);
    this.name = "speed";
    addPotion("SPEED", 0);
  }
}