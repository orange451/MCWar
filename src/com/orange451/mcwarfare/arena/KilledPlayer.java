package com.orange451.mcwarfare.arena;

import com.orange451.mcwarfare.KitPvP;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class KilledPlayer
{
  public KitPvP plugin;
  public Player killer;
  public Player killed;

  public KilledPlayer(KitPvP plugin, Player killer, Player died)
  {
    this.plugin = plugin;
    this.killer = killer;
    this.killed = died;
    execute();
  }

  public void execute() {
    EntityDamageEvent dmg = this.killed.getLastDamageCause();

    KitPlayer kp = this.plugin.getKitPlayer(this.killer);
    KitPlayer dp = this.plugin.getKitPlayer(this.killed);
    if ((kp != null) && (dp != null)) {
      if (kp.player.equals(dp.player))
        return;
      if (kp.arena.timeSinceStart < 10)
        return;
      kp.arena.getKill(kp);
      int xp = this.plugin.getKillXP(kp.player);
      int credits = this.plugin.getKillCredits(kp.player);
      kp.giveXp(xp);
      kp.onKill(dp);
      kp.profile.creditsGain += credits;
      kp.profile.credits += credits;
      kp.profile.kills += 1;
      dp.profile.deaths += 1;
      kp.kills += 1;
      String k = kp.getTeamColor() + kp.player.getName();
      String d = dp.getTeamColor() + dp.player.getName();
      kp.sayMessage(null, k + ChatColor.GRAY + " killed " + d + " +" + 
        ChatColor.YELLOW + xp + " xp " + " +" + 
        ChatColor.YELLOW + credits + "$");

      dp.sayMessage(null, ChatColor.RED + "killed by " + ChatColor.GRAY + kp.name + ChatColor.RED + "!");
      kp.calculate();
    }
    this.killer = null;
    this.killed = null;
  }
}