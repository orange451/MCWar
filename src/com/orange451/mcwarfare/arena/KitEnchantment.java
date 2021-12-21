package com.orange451.mcwarfare.arena;

import java.util.ArrayList;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class KitEnchantment
{
  public ArrayList<Enchantment> enchantments = new ArrayList();
  public ArrayList<Integer> levels = new ArrayList();

  public void add(ItemStack itm) {
    for (int i = 0; i < this.enchantments.size(); i++) {
      Enchantment e = (Enchantment)this.enchantments.get(i);
      itm.addUnsafeEnchantment(e, ((Integer)this.levels.get(i)).intValue());
    }
  }

  public String dumpEnchant() {
    String str = ",";
    for (int i = 0; i < this.enchantments.size(); i++) {
      String add = "";
      String name = ((Enchantment)this.enchantments.get(i)).getName().toUpperCase();
      String level = Integer.toString(((Integer)this.levels.get(i)).intValue());
      if (i + 1 < this.enchantments.size())
        add = ",";
      str = str + "e:" + name + "*" + level + add;
    }
    return str;
  }
}