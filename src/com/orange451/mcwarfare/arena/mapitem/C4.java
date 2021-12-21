package com.orange451.mcwarfare.arena.mapitem;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.orange451.mcwarfare.DamageType;
import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.ParticleEffects;
import com.orange451.mcwarfare.arena.KitPlayer;

public class C4 extends PlayerPlacedMapItem {
	private double radius = 4.4;
	
	public C4(KitPvP plugin, KitPlayer owner, Location location) {
		super(plugin, owner, location);
	}
	
	public void detonate() {
		//getLocation().getWorld().createExplosion(getLocation(), 0.1f);
		if (getLocation().getBlock().getType().equals(Material.LEVER)) {
			getLocation().getWorld().playSound(getLocation(), Sound.EXPLODE, 1, 2);
			ParticleEffects.sendParticle(null, 64, "explode", getLocation().clone().add(0, 1, 0), 0.3f, 0.3f, 0.3f, 0.2f, 96);
			List<Entity> entities = this.getLocation().getWorld().getEntities();
			for (int i = entities.size() - 1; i>= 0; i--) {
				if (entities.get(i) instanceof LivingEntity) {
					LivingEntity entity = ((LivingEntity) entities.get(i));
					if (entity.getLocation().distance(this.getLocation()) < radius) {
						if (entity instanceof Player) {
							owner.arena.plugin.damagePlayer((Player)entity, 26, DamageType.EXPLOSION, owner.player);
						} else {
							entity.damage(26, this.owner.player);
							entity.setLastDamage(0);
						}
					}
				}
			}
		}
		this.remove();
	}
}
