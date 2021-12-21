package com.orange451.mcwarfare.commands;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.KitItem;
import com.orange451.mcwarfare.arena.KitPlayer;
import com.orange451.mcwarfare.arena.KitProfile;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PCommandBuy extends PBaseCommand
{
  public PCommandBuy(KitPvP plugin)
  {
    this.plugin = plugin;
    this.aliases.add("buy");
    this.aliases.add("b");

    this.desc = (ChatColor.YELLOW + "to buy MCWarfare items");
  }

  public void perform()
  {
    KitPlayer kp = this.plugin.getKitPlayer(this.player);
    if (kp != null)
      try {
        String param = (String)this.parameters.get(1);

        if (param.equals("list")) {
          listAvailableItems(this.player, kp);
          return;
        }

        if (param.equals("grenade")) {
          int credits = kp.profile.credits;
          if (credits >= 100) {
            kp.profile.credits -= 100;
            kp.boughtItems.add(new KitItem(new ItemStack(Material.SLIME_BALL, 3)));
            kp.sayMessage(null, ChatColor.GREEN + "BOUGHT ITEM: " + ChatColor.WHITE + " grenades");
          } else {
            kp.sayMessage(null, "NOT ENOUGH CREDITS!");
          }
        }

        if (param.equals("molotov")) {
          int credits = kp.profile.credits;
          if (credits >= 100) {
            kp.profile.credits -= 100;
            kp.boughtItems.add(new KitItem(new ItemStack(Material.GLOWSTONE_DUST, 2)));
            kp.sayMessage(null, ChatColor.GREEN + "BOUGHT ITEM: " + ChatColor.WHITE + " molotov");
          } else {
            kp.sayMessage(null, "NOT ENOUGH CREDITS!");
          }
        }

        if (param.equals("armor")) {
          int credits = kp.profile.credits;
          if (credits >= 125) {
            kp.profile.credits -= 125;
            kp.boughtItems.add(new KitItem(new ItemStack(Material.IRON_CHESTPLATE, 1)));
            kp.boughtItems.add(new KitItem(new ItemStack(Material.IRON_BOOTS, 1)));
            kp.sayMessage(null, ChatColor.GREEN + "BOUGHT ITEM: " + ChatColor.WHITE + " armor (iron chestplate/boots)");
          } else {
            kp.sayMessage(null, "NOT ENOUGH CREDITS!");
          }
        }

        if (!param.equals("superarmor")) return;
        int credits = kp.profile.credits;
        if (credits >= 550) {
          kp.profile.credits -= 550;
          kp.boughtItems.add(new KitItem(new ItemStack(Material.DIAMOND_CHESTPLATE, 1)));
          kp.boughtItems.add(new KitItem(new ItemStack(Material.DIAMOND_BOOTS, 1)));
          kp.sayMessage(null, ChatColor.GREEN + "BOUGHT ITEM: " + ChatColor.WHITE + " armor (diamond chestplate/boots)");
        } else {
          kp.sayMessage(null, "NOT ENOUGH CREDITS!");
        }
      }
      catch (Exception e) {
        listAvailableItems(this.player, kp);
      }
    else
      this.player.sendMessage("type" + ChatColor.BLUE + " /war join" + ChatColor.WHITE + "first!");
  }

  private void listAvailableItems(Player player, KitPlayer kp)
  {
    kp.sayMessage(null, ChatColor.GRAY + "------" + ChatColor.YELLOW + "MCWAR ITEMS" + ChatColor.GRAY + "-----");
    kp.sayMessage(null, ChatColor.DARK_GREEN + "armor  " + ChatColor.YELLOW + "[125]");
    kp.sayMessage(null, ChatColor.DARK_GREEN + "superarmor  " + ChatColor.YELLOW + "[550]");
    kp.sayMessage(null, ChatColor.DARK_GREEN + "grenades(3) " + ChatColor.YELLOW + "[100]");
    kp.sayMessage(null, ChatColor.DARK_GREEN + "molotov(2) " + ChatColor.YELLOW + "[100]");
    kp.sayMessage(null, ChatColor.GRAY + "----------------------");
  }
}