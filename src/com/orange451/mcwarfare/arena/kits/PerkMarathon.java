package com.orange451.mcwarfare.arena.kits;

import org.bukkit.entity.Player;

public class PerkMarathon extends Perk
{
  public PerkMarathon(Player p)
  {
    super(p);
    this.name = "marathon";
  }

  public void step()
  {
    this.p.setFoodLevel(20);
  }
}