package com.orange451.mcwarfare.listeners;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.KitPlayer;
import com.orange451.mcwarfare.arena.mapitem.C4;
import com.orange451.mcwarfare.arena.mapitem.MapItem;
import com.orange451.opex.permissions.PermissionInterface;
import com.orange451.pvpgunplus.events.PVPGunPlusBulletCollideEvent;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class PluginBlockListener
implements Listener
{
	KitPvP plugin;

	public PluginBlockListener(KitPvP plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onBulletHit(PVPGunPlusBulletCollideEvent event) {
		if (event.getBlockHit().getType().equals(org.bukkit.Material.LEVER)) {
			List<MapItem> mapItems = plugin.getMapItems();
			for (int i = mapItems.size() - 1; i >= 0; i--) {
				if (mapItems.get(i).getLocation().equals(event.getBlockHit().getLocation())) {
					if (mapItems.get(i) instanceof C4) {
						((C4)mapItems.get(i)).detonate();
					}
				}
			}
		}
		if (event.getBlockHit().getType().equals(org.bukkit.Material.THIN_GLASS)) {
			this.plugin.glassThinReplace.add(event.getBlockHit().getLocation());
			event.getBlockHit().setType(org.bukkit.Material.AIR);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		event.setCancelled(true);
		Player player = event.getPlayer();
		if (player != null) {
			if (player.isOp()) {
				event.setCancelled(false);
				return;
			}
			try {
				if (event.getBlock().getType().equals(org.bukkit.Material.THIN_GLASS)) {
					this.plugin.glassThinReplace.add(event.getBlock().getLocation());
					return;
				}
				if (!player.isOp() && event.getBlock().getType().isSolid()) {
					KitPlayer kp = plugin.getKitPlayer(event.getPlayer());
					if (kp != null) {
						kp.lastDamager = null;
						event.getPlayer().setHealth(0);
						event.getPlayer().damage(9999);
						this.plugin.giveMessage(event.getPlayer(), ChatColor.RED + "NO BLOCK GLITCHING");
					}
				}
			}
			catch (Exception localException) {
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		try {
			Player player = event.getPlayer();
			if ((player != null) && (!PermissionInterface.hasPermission(player, "mcwar.admin"))) {
				ItemStack itm = event.getItemInHand();
				if ((itm != null) && ((itm.getTypeId() < 256) || (itm.getTypeId() == 397))) {
					if (itm.getTypeId() != Material.LEVER.getId()) {
						event.setCancelled(true);
						event.getPlayer().damage(9999);
						this.plugin.giveMessage(event.getPlayer(), ChatColor.RED + "NO BLOCK JUMPING");
					}
				}
			}
		} catch (Exception localException) {
			//
		}
	}
}