package com.orange451.mcwarfare.arena.mapitem;

import org.bukkit.Location;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.KitPlayer;

public abstract class PlayerPlacedMapItem extends MapItem {
	protected KitPlayer owner;
	
	public PlayerPlacedMapItem(KitPvP plugin, KitPlayer owner, Location location) {
		super(plugin, location);
		this.owner = owner;
	}
	
	public KitPlayer getOwner() {
		return this.owner;
	}
}
