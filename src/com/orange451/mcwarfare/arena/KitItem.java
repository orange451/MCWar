package com.orange451.mcwarfare.arena;

import com.orange451.mcwarfare.InventoryHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class KitItem
{
  public ItemStack itm;
  public String tag = "";

  public KitItem() {
  }

  public KitItem(ItemStack itm) {
    this.itm = itm;
  }

  public void give(KitPlayer p) {
    if (this.itm == null)
      return;
    String item = this.itm.getType().toString().toLowerCase();
    if (item.contains("chestplate")) {
      p.player.getInventory().setChestplate(this.itm);
      return;
    }
    if (item.contains("boots")) {
      p.player.getInventory().setBoots(this.itm);
      return;
    }
    int slot = InventoryHelper.getFirstFreeSlot(p.player.getInventory());
    if (slot > -1) {
      p.player.getInventory().setItem(slot, this.itm.clone());
      return;
    }
  }

  public KitItem setTag(String string) {
    this.tag = string;
    return this;
  }
}