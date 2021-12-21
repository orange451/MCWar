package com.orange451.mcwarfare.listeners;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.KitArena;
import com.orange451.mcwarfare.arena.KitPlayer;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class PluginEntityListener
implements Listener
{
	KitPvP plugin;

	public PluginEntityListener(KitPvP plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onEntityExplode(EntityExplodeEvent event) {
		if ((this.plugin.isInArena(event.getLocation())) && 
				(event.blockList().size() >= 1))
			event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.getCause().toString().contains("EXPLOSION")) {
			event.setCancelled(true);
		}
		if (event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK))
			event.setDamage(event.getDamage() + 1);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		try {
			Player defender = (Player)event.getEntity();
			if (!(event.getEntity() instanceof Player)) {
				return;
			}
			Entity att = event.getDamager();
			if (((att instanceof Player)) || ((att instanceof Projectile))) {
				Player attacker = null;
				if ((event.getDamager() instanceof Projectile)) {
					Projectile arrow = (Projectile)att;
					attacker = (Player)arrow.getShooter();
				} else {
					attacker = (Player)att;
				}

				if (attacker != null) {
					boolean canDamage = this.plugin.canDamagePlayer(attacker, defender);
					event.setCancelled(!canDamage);

					KitPlayer shootKP = this.plugin.getKitPlayer(attacker);
					KitPlayer defendKP = this.plugin.getKitPlayer(defender);
					if ((!event.isCancelled()) && (event.getDamage() < 1000))
						shootKP.arena.onDamage(event, shootKP, defendKP);
				}
			}
		}
		catch (Exception localException)
		{
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event)
	{
		event.getDrops().clear();
		event.setDroppedExp(0);
	}
}