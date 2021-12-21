package com.orange451.mcwarfare;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R1.*;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ParticleEffects {
	public static void sendEffect(Player player, double viewDistance, int id, Location location, int data, boolean disableRelativeVolume) {
		net.minecraft.server.v1_7_R1.PacketPlayOutWorldEvent packet = new net.minecraft.server.v1_7_R1.PacketPlayOutWorldEvent(id, location.getBlockX(), location.getBlockY(), location.getBlockZ(), data, disableRelativeVolume);
		sendPacket(packet, location, viewDistance, player);
	}

	public static void sendPacket(net.minecraft.server.v1_7_R1.Packet packet, Location location, double viewDistance, Player player)
	{
		for (Player other : location.getWorld().getPlayers())
		{
			if (player != null && !other.canSee(player))
				continue;

			if (other.getLocation().distance(location) <= viewDistance)
				sendPacket(other, packet);

		}

	}

	public static void sendPacket(Player player, net.minecraft.server.v1_7_R1.Packet packet)
	{
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	public static void sendParticle(Player player, double viewDistance, String name, Location location, float offsetX, float offsetY, float offsetZ, float speed, int numberOfParticles)
	{
		net.minecraft.server.v1_7_R1.PacketPlayOutWorldParticles packet = new net.minecraft.server.v1_7_R1.PacketPlayOutWorldParticles();
		setObject(packet, "a", name);
		setFloat(packet, "b", (float) location.getX());
		setFloat(packet, "c", (float) location.getY());
		setFloat(packet, "d", (float) location.getZ());
		setFloat(packet, "e", offsetX);
		setFloat(packet, "f", offsetY);
		setFloat(packet, "g", offsetZ);
		setFloat(packet, "h", speed);
		setInt(packet, "i", numberOfParticles);
		sendPacket(packet, location, viewDistance, player);
	}

	private static void setObject(Object object, String name, Object value)
	{
		try
		{
			java.lang.reflect.Field field = object.getClass().getDeclaredField(name);
			try
			{
				field.setAccessible(true);
				field.set(object, value);
			}
			finally
			{
				field.setAccessible(false);
			}
		}
		catch (Exception ex) { }
	}

	private static void setFloat(Object object, String name, float value)
	{
		try
		{
			java.lang.reflect.Field field = object.getClass().getDeclaredField(name);
			try
			{
				field.setAccessible(true);
				field.setFloat(object, value);
			}
			finally
			{
				field.setAccessible(false);
			}
		}
		catch (Exception ex) { }
	}

	private static void setInt(Object object, String name, int value)
	{
		try
		{
			java.lang.reflect.Field field = object.getClass().getDeclaredField(name);
			try
			{
				field.setAccessible(true);
				field.setInt(object, value);
			}
			finally
			{
				field.setAccessible(false);
			}
		}
		catch (Exception ex) { }
	}
}
