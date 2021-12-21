package com.orange451.mcwarfare.arena.kits;

import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Perk
{
  public String name;
  public ArrayList<PotionEffect> pots = new ArrayList();
  public Player p;

  public Perk(Player p)
  {
    this.p = p;
  }

  public Perk addPotion(String news, int level) {
    PotionEffectType pet = PotionEffectType.getByName(news);
    if (pet != null) {
      PotionEffect po = new PotionEffect(PotionEffectType.getByName(news), 9999, level);
      this.pots.add(po);
    }
    return this;
  }

  public Perk giveToPlayer(Player p) {
    for (int i = 0; i < this.pots.size(); i++) {
      p.addPotionEffect((PotionEffect)this.pots.get(i));
    }
    return this;
  }

  public void step() {
  }

  public void clear() {
    this.name = null;
    this.p = null;
    this.pots.clear();
  }
}