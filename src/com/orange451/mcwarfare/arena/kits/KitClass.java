package com.orange451.mcwarfare.arena.kits;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.KitEnchantment;

public class KitClass {
	public KitPvP plugin;
	public String name;
	public String permissionNode = "";
	public String primary = "m16";
	public String secondary = "usp45";
	public Player player;
	public int armor0 = 0;
	public int armor1 = 0;
	public int armor2 = 0;
	public int armor3 = 0;
	public int weapon1 = 0;
	public int weapon2 = 0;
	public int weapon3 = 0;
	public int weapon4 = 0;
	public int weapon5 = 0;
	public int weapon6 = 0;
	public int weapon7 = 0;
	public int weapon8 = 0;
	public int weapon9 = 0;
	public ArrayList<PotionEffect> pots = new ArrayList<PotionEffect>();
	public KitEnchantment enchanthelmet = new KitEnchantment();
	public KitEnchantment enchantchest = new KitEnchantment();
	public KitEnchantment enchantlegs = new KitEnchantment();
	public KitEnchantment enchantboots = new KitEnchantment();
	public KitEnchantment enchant1 = new KitEnchantment();
	public KitEnchantment enchant2 = new KitEnchantment();
	public KitEnchantment enchant3 = new KitEnchantment();
	public KitEnchantment enchant4 = new KitEnchantment();
	public KitEnchantment enchant5 = new KitEnchantment();
	public KitEnchantment enchant6 = new KitEnchantment();
	public KitEnchantment enchant7 = new KitEnchantment();
	public KitEnchantment enchant8 = new KitEnchantment();
	public KitEnchantment enchant9 = new KitEnchantment();
	public int amt1 = 1;
	public int amt2 = 1;
	public int amt3 = 1;
	public int amt4 = 1;
	public int amt5 = 1;
	public int amt6 = 1;
	public int amt7 = 1;
	public int amt8 = 24;
	public int amt9 = 24;
	public byte special1 = 0;
	public byte special2 = 0;
	public byte special3 = 0;
	public byte special4 = 0;
	public byte special5 = 0;
	public byte special6 = 0;
	public byte special7 = 0;
	public byte special8 = 0;
	public byte special9 = 0;
	public boolean loaded = true;
	public File file;
	public ArrayList<String> tocompute;
	
	public KitClass(KitPvP plugin, File file) {
		this.plugin = plugin;
		this.file = file;
		this.load();
	}
	
	public KitClass(KitPvP plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
	}

	public void load() {
		name = file.getName();
		ArrayList<String> file = new ArrayList<String>();
	    try{
			FileInputStream fstream = new FileInputStream(this.file.getAbsolutePath());
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				file.add(strLine);
			}
			br.close();
			in.close();
			fstream.close();
        }catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }
	    
	    for (int i= 0; i < file.size(); i++) {
	    	computeData(file.get(i));
	    }
	    if (loaded) {
	    	System.out.println("[KITPVP] CLASS LOADED: " + name);
	    }else{
	    	System.out.println("[KITPVP] ERROR LOADING CLASS: " + name);
	    }
	}

	public int readWep(String[] str) {
		int ret = 0;
		for (int i = 0; i < str.length; i++) {
			String st = str[i];
			if (st.startsWith("i:")) {
				String news = st.substring(2);
				int ii = Integer.parseInt(news);
				ret = ii;
			}
		}
		return ret;
	}
	
	public int readDat(String[] str) {
		int ret = 0;
		for (int i = 0; i < str.length; i++) {
			String st = str[i];
			if (st.startsWith("d:")) {
				String news = st.substring(2);
				int ii = Integer.parseInt(news);
				ret = ii;
			}
		}
		return ret;
	}
	
	public KitEnchantment readEnchantment(String[] str) {
		KitEnchantment ret = new KitEnchantment();
		for (int i = 0; i < str.length; i++) {
			String st = str[i];
			if (st.startsWith("e:")) {
				String news = st.substring(2);
				int level = 1;
				if (news.contains("*")) {
					String nn = news.substring(news.indexOf('*')+1);
					news = news.substring(0, news.indexOf('*'));
					try{
						level = Integer.parseInt(nn);
					}catch(Exception e) {
						//
					}
				}
				Enchantment temp = Enchantment.getByName(news.toUpperCase());
				if (temp != null) {
					ret.enchantments.add(temp);
					ret.levels.add(level);
				}
			}
		}
		return ret;
	}
	
	public void readPotion(String[] str) {
		for (int i = 0; i < str.length; i++) {
			String news = str[i];
			int level = 1;
			if (news.contains("*")) {
				String nn = news.substring(news.indexOf('*')+1);
				news = news.substring(0, news.indexOf('*'));
				try{
					level = Integer.parseInt(nn);
				}catch(Exception e) {
					//
				}
			}
			PotionEffectType pet = PotionEffectType.getByName(news);
			if (pet != null) {
				PotionEffect po = new PotionEffect(PotionEffectType.getByName(news), 9999, level);
				pots.add(po);
				System.out.println("[KITPVP] loaded potion: " + news + " into Kit Class: " + this.name);
			}else{
				System.out.println("[KITPVP] error loading potion: " + news + " into Kit Class: " + this.name);
			}
		}
	}
	
	public int readAmt(String[] str) {
		int ret = 1;
		for (int i = 0; i < str.length; i++) {
			String st = str[i];
			if (st.startsWith("a:")) {
				String news = st.substring(2);
				int ii = Integer.parseInt(news);
				ret = ii;
			}
		}
		return ret;
	}
	
	public void computeData(String str) {
		try{
			if (str.indexOf("=") >=1 ) {
				String str2 = str.substring(0, str.indexOf("="));
				if (str2.equalsIgnoreCase("primary")) {
					String line = str.substring(str.indexOf("=")+1);
					primary=line;
				}
				if (str2.equalsIgnoreCase("secondary")) {
					String line = str.substring(str.indexOf("=")+1);
					secondary=line;
				}
				
				if (str2.equalsIgnoreCase("helmet")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					armor0 = readWep(strarr);
					enchanthelmet = readEnchantment(strarr);
				}
				if (str2.equalsIgnoreCase("chestplate")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					armor1 = readWep(strarr);
					enchantchest = readEnchantment(strarr);
				}
				if (str2.equalsIgnoreCase("leggings")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					armor2 = readWep(strarr);
					enchantlegs = readEnchantment(strarr);
				}
				if (str2.equalsIgnoreCase("boots")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					armor3 = readWep(strarr);
					enchantboots = readEnchantment(strarr);
				}
				if (str2.equalsIgnoreCase("node")) {
					String line = str.substring(str.indexOf("=")+1);
					permissionNode = line;
				}
				if (str2.equalsIgnoreCase("tool1")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					int value = readWep(strarr);
					int value2 = readDat(strarr);
					int value3 = readAmt(strarr);
					KitEnchantment value4 = readEnchantment(strarr);
					weapon1 = value;
					special1 = (byte) value2;
					amt1 = value3;
					enchant1 = value4;
				}
				
				if (str2.equalsIgnoreCase("tool2")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					int value = readWep(strarr);
					int value2 = readDat(strarr);
					int value3 = readAmt(strarr);
					KitEnchantment value4 = readEnchantment(strarr);
					weapon2 = value;
					special2 = (byte) value2;
					amt2 = value3;
					enchant2 = value4;
				}
				
				if (str2.equalsIgnoreCase("tool3")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					int value = readWep(strarr);
					int value2 = readDat(strarr);
					int value3 = readAmt(strarr);
					KitEnchantment value4 = readEnchantment(strarr);
					weapon3 = value;
					special3 = (byte) value2;
					amt3 = value3;
					enchant3 = value4;
				}
				
				if (str2.equalsIgnoreCase("tool4")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					int value = readWep(strarr);
					int value2 = readDat(strarr);
					int value3 = readAmt(strarr);
					KitEnchantment value4 = readEnchantment(strarr);
					weapon4 = value;
					special4 = (byte) value2;
					amt4 = value3;
					enchant4 = value4;
				}
				
				if (str2.equalsIgnoreCase("tool5")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					int value = readWep(strarr);
					int value2 = readDat(strarr);
					int value3 = readAmt(strarr);
					KitEnchantment value4 = readEnchantment(strarr);
					weapon5 = value;
					special5 = (byte) value2;
					amt5 = value3;
					enchant5 = value4;
				}
				
				if (str2.equalsIgnoreCase("tool6")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					int value = readWep(strarr);
					int value2 = readDat(strarr);
					int value3 = readAmt(strarr);
					KitEnchantment value4 = readEnchantment(strarr);
					weapon6 = value;
					special6 = (byte) value2;
					amt6 = value3;
					enchant6 = value4;
				}
				
				if (str2.equalsIgnoreCase("tool7")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					int value = readWep(strarr);
					int value2 = readDat(strarr);
					int value3 = readAmt(strarr);
					KitEnchantment value4 = readEnchantment(strarr);
					weapon7 = value;
					special7 = (byte) value2;
					amt7 = value3;
					enchant7 = value4;
				}
				
				if (str2.equalsIgnoreCase("tool8")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					int value = readWep(strarr);
					int value2 = readDat(strarr);
					//int value3 = readAmt(strarr);
					KitEnchantment value4 = readEnchantment(strarr);
					weapon8 = value;
					special8 = (byte) value2;
					if (player != null) {
						amt8 = plugin.getAmmo(player, "primary");
					}else{
						int value3 = readAmt(strarr);
						amt8 = value3;
					}
					enchant8 = value4;
				}
				
				if (str2.equalsIgnoreCase("tool9")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					int value = readWep(strarr);
					int value2 = readDat(strarr);
					KitEnchantment value4 = readEnchantment(strarr);
					weapon9 = value;
					special9 = (byte) value2;
					if (player != null) {
						amt9 = plugin.getAmmo(player, "secondary");
					}else{
						int value3 = readAmt(strarr);
						amt9 = value3;
					}
					enchant9 = value4;
				}
				
				if (str2.equalsIgnoreCase("addpot")) {
					String line = str.substring(str.indexOf("=")+1);
					String[] strarr = line.split(",");
					readPotion(strarr);
				}
			}
		}catch(Exception e) {
			//problem loading the class
			loaded = false;
		}
	}

	public ArrayList<String> dumpClass() {
		if (player != null) {
			Plugin p = Bukkit.getPluginManager().getPlugin("PVPGunPlus");
			if (p != null && p.isEnabled()) {
				//System.out.println("SAVING STATS player: " + player.getName());
				ArrayList<String> ret = new ArrayList<String>();
				ret.add(":defclass:");
				ret.add("primary=" + primary);
				ret.add("secondary=" + secondary);
				ret.add("helmet=i:" +     Integer.toString(armor0) + ",a:1,d:0" + enchanthelmet.dumpEnchant());
				ret.add("chestplate=i:" + Integer.toString(armor1) + ",a:1,d:0" + enchantchest.dumpEnchant());
				ret.add("leggings=i:" +   Integer.toString(armor2) + ",a:1,d:0" + enchantlegs.dumpEnchant());
				ret.add("boots=i:" +      Integer.toString(armor3) + ",a:1,d:0" + enchantboots.dumpEnchant());
				ret.add("tool1=i:" + Integer.toString(weapon1) + ",a:" + Integer.toString(amt1) + ",d:" + Byte.toString(special1) + enchant1.dumpEnchant());
				ret.add("tool2=i:" + plugin.getGun(primary).type);
				ret.add("tool3=i:" + plugin.getGun(secondary).type);
				//ret.add("tool2=i:" + Integer.toString(weapon2) + ",a:" + Integer.toString(amt2) + ",d:" + Byte.toString(special2) + enchant2.dumpEnchant());
				//ret.add("tool3=i:" + Integer.toString(weapon3) + ",a:" + Integer.toString(amt3) + ",d:" + Byte.toString(special3) + enchant3.dumpEnchant());
				ret.add("tool4=i:" + Integer.toString(weapon4) + ",a:" + Integer.toString(amt4) + ",d:" + Byte.toString(special4) + enchant4.dumpEnchant());
				ret.add("tool5=i:" + Integer.toString(weapon5) + ",a:" + Integer.toString(amt5) + ",d:" + Byte.toString(special5) + enchant5.dumpEnchant());
				ret.add("tool6=i:" + Integer.toString(weapon6) + ",a:" + Integer.toString(amt6) + ",d:" + Byte.toString(special6) + enchant6.dumpEnchant());
				ret.add("tool7=i:" + Integer.toString(weapon7) + ",a:" + Integer.toString(amt7) + ",d:" + Byte.toString(special7) + enchant7.dumpEnchant());
				
				ret.add("tool8=i:" + plugin.getGunAmmo(primary) + ",a:" + plugin.getAmmo(player, "primary"));
				ret.add("tool9=i:" + plugin.getGunAmmo(secondary) + ",a:" + plugin.getAmmo(player, "secondary"));
				//System.out.println("AMMO FOR: " + player.getName() + " ====== " + plugin.getAmmo(player, "primary"));
				//ret.add("tool8=i:" + Integer.toString(weapon8) + ",a:" + Integer.toString(amt8) + ",d:" + Byte.toString(special8) + enchant8.dumpEnchant());
				//ret.add("tool9=i:" + Integer.toString(weapon9) + ",a:" + Integer.toString(amt9) + ",d:" + Byte.toString(special9) + enchant9.dumpEnchant());
				for (int i = 0; i < pots.size(); i++) {
					PotionEffect pet = pots.get(i);
					String pname = pet.getType().getName().toUpperCase();
					ret.add("addpot=" + pname + "*" + pet.getAmplifier());
				}
				ret.add(":endclass:");
				return ret;
			}
		}
	
		System.out.println("SAVING BLANK PLAYER");
		ArrayList<String> ret = new ArrayList<String>();
		ret.add(":defclass:");
		ret.add("primary=" + primary);
		ret.add("secondary=" + secondary);
		ret.add("helmet=i:" +     Integer.toString(armor0) + ",a:1,d:0" + enchanthelmet.dumpEnchant());
		ret.add("chestplate=i:" + Integer.toString(armor1) + ",a:1,d:0" + enchantchest.dumpEnchant());
		ret.add("leggings=i:" +   Integer.toString(armor2) + ",a:1,d:0" + enchantlegs.dumpEnchant());
		ret.add("boots=i:" +      Integer.toString(armor3) + ",a:1,d:0" + enchantboots.dumpEnchant());
		ret.add("tool1=i:" + Integer.toString(weapon1) + ",a:" + Integer.toString(amt1) + ",d:" + Byte.toString(special1) + enchant1.dumpEnchant());
		ret.add("tool2=i:" + plugin.getGun(primary).type);
		ret.add("tool3=i:" + plugin.getGun(secondary).type);
		//ret.add("tool2=i:" + Integer.toString(weapon2) + ",a:" + Integer.toString(amt2) + ",d:" + Byte.toString(special2) + enchant2.dumpEnchant());
		//ret.add("tool3=i:" + Integer.toString(weapon3) + ",a:" + Integer.toString(amt3) + ",d:" + Byte.toString(special3) + enchant3.dumpEnchant());
		ret.add("tool4=i:" + Integer.toString(weapon4) + ",a:" + Integer.toString(amt4) + ",d:" + Byte.toString(special4) + enchant4.dumpEnchant());
		ret.add("tool5=i:" + Integer.toString(weapon5) + ",a:" + Integer.toString(amt5) + ",d:" + Byte.toString(special5) + enchant5.dumpEnchant());
		ret.add("tool6=i:" + Integer.toString(weapon6) + ",a:" + Integer.toString(amt6) + ",d:" + Byte.toString(special6) + enchant6.dumpEnchant());
		ret.add("tool7=i:" + Integer.toString(weapon7) + ",a:" + Integer.toString(amt7) + ",d:" + Byte.toString(special7) + enchant7.dumpEnchant());
		
		//ret.add("tool8=i:" + plugin.getGunAmmo(primary) + ",a:" + plugin.getAmmo(player));
		//ret.add("tool9=i:" + plugin.getGunAmmo(secondary) + ",a:" + plugin.getAmmo(player));
		ret.add("tool8=i:" + Integer.toString(weapon8) + ",a:" + Integer.toString(amt8) + ",d:" + Byte.toString(special8) + enchant8.dumpEnchant());
		ret.add("tool9=i:" + Integer.toString(weapon9) + ",a:" + Integer.toString(amt9) + ",d:" + Byte.toString(special9) + enchant9.dumpEnchant());
		for (int i = 0; i < pots.size(); i++) {
			PotionEffect pet = pots.get(i);
			String pname = pet.getType().getName().toUpperCase();
			ret.add("addpot=" + pname + "*" + pet.getAmplifier());
		}
		ret.add(":endclass:");
		return ret;
	}

	public void update() {
		this.weapon2 = plugin.getGun(primary).type;
		this.weapon3 = plugin.getGun(secondary).type;
		
		this.weapon8 = plugin.getGunAmmo(primary);
		this.weapon9 = plugin.getGunAmmo(secondary);
	}

	public void clear() {
		this.file = null;
		this.player = null;
		this.pots.clear();
	}	
}
