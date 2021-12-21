package com.orange451.mcwarfare.arena.mapitem;

import org.bukkit.Location;

import com.orange451.mcwarfare.KitPvP;

public abstract class MapItem {
	protected Location location;
	protected KitPvP plugin;
	
	public MapItem(KitPvP plugin, Location location) {
		this.location = location;
		this.plugin = plugin;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public void remove() {
		plugin.removeMapEntity(this);
	}
}
