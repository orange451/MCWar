package com.orange451.mcwarfare;

import com.orange451.mcwarfare.KitPvP;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Util {

   public static KitPvP plugin;
   public static World world;
   public static Server server;


   public static void Initialize(KitPvP kitPvP) {
      plugin = kitPvP;
      server = kitPvP.getServer();
      world = (World)server.getWorlds().get(0);
   }

   public static Player MatchPlayer(String player) {
      List players = server.matchPlayer(player);
      return players.size() == 1?(Player)players.get(0):null;
   }

   public static List Who() {
      Player[] players = server.getOnlinePlayers();
      ArrayList players1 = new ArrayList();

      for(int i = 0; i < players.length; ++i) {
         players1.add(players[i]);
      }

      return players1;
   }

   public static void playEffect(Effect e, Location l, int num) {
      for(int i = 0; i < server.getOnlinePlayers().length; ++i) {
         server.getOnlinePlayers()[i].playEffect(l, e, num);
      }

   }

   public static double point_distance(Location loc1, Location loc2) {
      double p1x = loc1.getX();
      double p1y = loc1.getY();
      double p1z = loc1.getZ();
      double p2x = loc2.getX();
      double p2y = loc2.getY();
      double p2z = loc2.getZ();
      double xdist = p1x - p2x;
      double ydist = p1y - p2y;
      double zdist = p1z - p2z;
      return Math.sqrt(xdist * xdist + ydist * ydist + zdist * zdist);
   }

   public static int random(int x) {
      Random rand = new Random();
      return rand.nextInt(x);
   }

   public static double lengthdir_x(double len, double dir) {
      return len * Math.cos(Math.toRadians(dir));
   }

   public static double lengthdir_y(double len, double dir) {
      return -len * Math.sin(Math.toRadians(dir));
   }

   public static double point_direction(double x1, double y1, double x2, double y2) {
      double d;
      try {
         d = Math.toDegrees(Math.atan((y2 - y1) / (x2 - x1)));
      } catch (Exception var11) {
         d = 0.0D;
      }

      if(x1 > x2 && y1 > y2) {
         return -d + 180.0D;
      } else if(x1 < x2 && y1 > y2) {
         return -d;
      } else {
         if(x1 == x2) {
            if(y1 > y2) {
               return 90.0D;
            }

            if(y1 < y2) {
               return 270.0D;
            }
         }

         if(x1 > x2 && y1 < y2) {
            return -d + 180.0D;
         } else if(x1 < x2 && y1 < y2) {
            return -d + 360.0D;
         } else {
            if(y1 == y2) {
               if(x1 > x2) {
                  return 180.0D;
               }

               if(x1 < x2) {
                  return 0.0D;
               }
            }

            return 0.0D;
         }
      }
   }
}
