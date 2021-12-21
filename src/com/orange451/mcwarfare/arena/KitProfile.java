package com.orange451.mcwarfare.arena;

import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.orange451.mcwarfare.FileIO;
import com.orange451.mcwarfare.KitPvP;
import com.orange451.mcwarfare.arena.kits.KitClass;

public class KitProfile {
	public int level = 1;
	public int xp;
	public int gainxp;
	public int kills;
	public int deaths;
	public int knife = 1;
	public String lethal = "grenade";
	public String tactical = "flashbang";
	public ArrayList<String> myContents = new ArrayList<String>();
	public ArrayList<KitClass> classes =  new ArrayList<KitClass>();
	public ArrayList<String> boughtGuns = new ArrayList<String>();
	public ArrayList<String> compute;
	public int myclass = 0;
	public int credits = 0;
	public int creditsGain = 0;
	public int xpn;
	public KitPvP plugin;
	public Player player;
	public String perk = "";
	public String tag = "";
	public ArrayList<String> perks = new ArrayList<String>();
	public long lastModified;
	public String filename;
	
	public KitProfile(KitPvP plugin, Player player, ArrayList<String> compute) {
		this.plugin = plugin;
		this.compute = compute;
		this.player = player;
		this.filename = player.getName().toLowerCase() + ".mcw";
		File f = getFile(player.getName());
		if (f != null)
			this.lastModified = f.lastModified();
	}
	
	public KitProfile(KitPvP plugin, String player, ArrayList<String> compute) {
		this.plugin = plugin;
		this.compute = compute;
		this.player = null;
		this.filename = player.toLowerCase() + ".mcw";
		File f = getFile(player);
		if (f != null)
			this.lastModified = f.lastModified();
	}
	
	public File getFile(String name) {
		KitPvP plugin = (KitPvP) Bukkit.getPluginManager().getPlugin("MCWarfare");
		if (plugin != null) {
			String path = plugin.getUsers() + "/" + name.toLowerCase() + ".mcw";
			File f = new File(path);
			return f;
		}
		return null;
	}
	
	public void save(KitPlayer p) {
		try{
			KitProfile kp = plugin.getProfile(p);
			if (kp != null) {
				try{
					int nxp = kp.xp;
					if (nxp > this.xp && kp.lastModified > lastModified) {
						this.xp = nxp;
						kp.getFile(p.player.getName());
					}
					addNewGuns(kp.boughtGuns);
					addNewPerks(kp.perks);
				}catch(Exception e) {
					//
				}
				kp.CLEAR();
				kp = null;
			}
			String path = p.plugin.getUsers() + "/" + p.name.toLowerCase() + ".mcw";
			saveToPath(path);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void removeMultipleGuns() {
		int q;
		for(q = boughtGuns.size()-1; q >= 0; q--) {  
			if (amtGun(boughtGuns.get(q)) > 1)
				boughtGuns.remove(q);
		}
	}
	
	private void addNewGuns(ArrayList<String> a) {
		for (int i = 0; i < a.size(); i++) {
			//if (!this.hasGun(a.get(i))) {
				this.boughtGuns.add(a.get(i));
			//}
		}
	}
	
	private void addNewPerks(ArrayList<String> a) {
		for (int i = 0; i < a.size(); i++) {
			if (!this.hasPerk(a.get(i))) {
				this.perks.add(a.get(i));
			}
		}
	}

	public void save(String player) {
		try{
			KitProfile kp = plugin.loadProfile(player);
			if (kp != null) {
				try{
					addNewGuns(kp.boughtGuns);
					addNewPerks(kp.perks);
					
					if (kp.kills > this.kills)
						this.kills = kp.kills;
					if (kp.deaths > this.deaths)
						this.deaths = kp.deaths;
					if (kp.level > this.level)
						this.level = kp.level;
					
					int nxp = kp.xp;
					if (nxp > this.xp && kp.lastModified > lastModified) {
						this.xp = nxp;
						kp.getFile(player);
					}
				}catch(Exception e) {
					//
				}
				kp.CLEAR();
				kp = null;
			}
			KitPvP plugin = (KitPvP) Bukkit.getPluginManager().getPlugin("MCWarfare");
			if (plugin != null) {
				String path = plugin.getUsers() + "/" + player.toLowerCase() + ".mcw";
				saveToPath(path);
			}
		}catch(Exception e) {
			//e.printStackTrace();
		}
	}

	public void saveToPath(String path) {
		if (this.classes.size() == 0)
			return;
	    BufferedWriter out = FileIO.file_text_open_write(path);
	    try{
		    FileIO.file_text_write_line(out, "xp=" + xp);
		    FileIO.file_text_write_line(out, "level=" + level);
		    FileIO.file_text_write_line(out, "credits=" + credits);
		    FileIO.file_text_write_line(out, "kills=" + kills);
		    FileIO.file_text_write_line(out, "deaths=" + deaths);
		    FileIO.file_text_write_line(out, "perk=" + perk);
		    FileIO.file_text_write_line(out, "tag=" + tag);
		    FileIO.file_text_write_line(out, "knife=" + knife);
		    FileIO.file_text_write_line(out, "lethal=" + lethal);
		    FileIO.file_text_write_line(out, "tactical=" + tactical);
		    FileIO.file_text_write_line(out, "--perks");
		    for (int i = 0; i < perks.size(); i++) {
		    	FileIO.file_text_write_line(out, "::defperk=" + perks.get(i));
		    }
		    FileIO.file_text_write_line(out, "--classes");
		    boolean errorClass = false;
		    for (int i = 0; i < 1; i++) {
		    	try{
				    ArrayList<String> dump = classes.get(i).dumpClass();
				    for (int ii = 0; ii < dump.size(); ii++) {
				    	if (!FileIO.file_text_write_line(out, dump.get(ii))) {
				    		errorClass = true;
				    	}
				    }
		    	}catch(Exception e) {
		    		errorClass = true;
		    	}
		    }
		    if (classes.size() == 0 || errorClass) {
				try
				{
					ArrayList<String> dump = ((KitPvP)Bukkit.getPluginManager().getPlugin("MCWarfare")).classes.get(0).dumpClass();
					for (int i = 0; i < dump.size(); i++) {
						FileIO.file_text_write_line(out, dump.get(i));
					}
				}
				catch (Exception ex) { }
		    }
			if (this.boughtGuns.size() == 0) {
			    FileIO.file_text_write_line(out, "::defguns");
			    FileIO.file_text_write_line(out, "m16");
			    FileIO.file_text_write_line(out, "usp45");
			    FileIO.file_text_write_line(out, "m1014");
			    FileIO.file_text_write_line(out, "l118a");
			    FileIO.file_text_write_line(out, "::endguns");
			}else{
			    removeMultipleGuns();
			    FileIO.file_text_write_line(out, "::defguns");
			    for (int i = 0; i < boughtGuns.size(); i++) {
			    	//System.out.print(boughtGuns.get(i) + " ");
			    	FileIO.file_text_write_line(out, boughtGuns.get(i));
			    }
			    FileIO.file_text_write_line(out, "::endguns");
		    }
	    }catch(Exception e) {
	    	System.out.println("ERROR SAVING PLAYER PROFILE!");
	    	FileIO.file_text_close(out);
	    	out = FileIO.file_text_open_write(path);
	    	for (int i = 0; i < this.myContents.size(); i++) {
	    		FileIO.file_text_write_line(out, myContents.get(i));
	    	}
	    }
	    FileIO.file_text_close(out);
		File f = getFile(player.getName());
		if (f != null)
			this.lastModified = f.lastModified();	
	}
	
	public void execute(KitPvP plugin) {
		boolean isloadingClass = false;
		boolean isloadingGuns = false;
		KitClass kc = null;// = new KitClass();
		for (int i = 0; i < compute.size(); i++) {
			String str = compute.get(i);
			myContents.add(str);
			try{
				if (str.equalsIgnoreCase(":defclass:")) {
					kc = new KitClass(plugin, player);
					isloadingClass = true;
				}
				if (str.equalsIgnoreCase(":endclass:")) {
					classes.add(kc);
					isloadingClass = false;
				}
				if (str.equalsIgnoreCase("::defguns")) {
					isloadingGuns = true;
				}
				if (str.equalsIgnoreCase("::endguns")) {
					isloadingGuns = false;
				}
				if (!isloadingClass && !isloadingGuns) {
					if (str.indexOf("=") >=1 ) {
						String str2 = str.substring(0, str.indexOf("="));
						if (str2.equalsIgnoreCase("xp")) {
							String line = str.substring(str.indexOf("=")+1);
							xp = Integer.parseInt(line);
							if (xp < 0)
								xp = 0;
						}
						if (str2.equalsIgnoreCase("lethal")) {
							String line = str.substring(str.indexOf("=")+1);
							lethal = line;
						}
						if (str2.equalsIgnoreCase("tactical")) {
							String line = str.substring(str.indexOf("=")+1);
							tactical = line;
						}
						if (str2.equalsIgnoreCase("knife")) {
							String line = str.substring(str.indexOf("=")+1);
							knife = Integer.parseInt(line);
						}
						if (str2.equalsIgnoreCase("level")) {
							String line = str.substring(str.indexOf("=")+1);
							level = Integer.parseInt(line);
						}
						if (str2.equalsIgnoreCase("kills")) {
							String line = str.substring(str.indexOf("=")+1);
							kills = Integer.parseInt(line);
						}
						if (str2.equalsIgnoreCase("perk"))
							perk = str.substring(str.indexOf("=")+1);
						
						if (str2.equalsIgnoreCase("::defperk"))
							perks.add(str.substring(str.indexOf("=")+1));
						
						if (str2.equalsIgnoreCase("deaths"))
							deaths = Integer.parseInt(str.substring(str.indexOf("=")+1));
						
						if (str2.equalsIgnoreCase("credits"))
							credits = Integer.parseInt(str.substring(str.indexOf("=")+1));
						
						if (str2.equalsIgnoreCase("tag"))
							tag = str.substring(str.indexOf("=")+1);
					}
				}
				if (isloadingClass) {
					kc.computeData(str);
				}
				if (isloadingGuns) {
					if (!str.equals("::defguns")) {
						boughtGuns.add(str);
					}
				}
			}catch(Exception e) {
				//
			}
		}
	}

	public int amtGun(String g) {
		int amt = 0;
		for (int i = 0; i < boughtGuns.size(); i++) {
			if (boughtGuns.get(i).toLowerCase().equals(g.toLowerCase())) {
				amt++;
			}
		}
		return amt;
	}
	
	public boolean hasGun(String g) {
		for (int i = 0; i < boughtGuns.size(); i++) {
			if (boughtGuns.get(i).toLowerCase().equals(g.toLowerCase())) {
				return true;//boughtGuns.get(i);
			}
		}
		return false;//null;
	}
	
	public boolean hasPerk(String g) {
		for (int i = 0; i < perks.size(); i++) {
			if (perks.get(i).equals(g)) {
				return true;//boughtGuns.get(i);
			}
		}
		return false;//null;
	}
	
	public KitGun getGun(String str) {
		if (str.equals("primary")) {
			return plugin.getGun(classes.get(myclass).primary);
		}
		if (str.equals("secondary")) {
			return plugin.getGun(classes.get(myclass).secondary);
		}
		return null;
	}

	public void CLEAR() {
	    for (int i = 0; i < classes.size(); i++) {
		    classes.get(i).clear();
	    }
	    this.myContents.clear();
	    this.compute.clear();
		this.player = null;
	}

	public void dumpStats() {
		String mperk = "";
		String mgun = "";
		for (int i = 0; i < perks.size(); i++)
			mperk = mperk + perks.get(i) + " ";
		for (int i = 0; i < boughtGuns.size(); i++)
			mgun = mgun + boughtGuns.get(i) + " ";
		System.out.println("---------" + filename + "--------");
		System.out.println("|| level  " + level);
		System.out.println("|| xp     " + xp);
		System.out.println("|| kills  " + kills);
		System.out.println("|| deaths " + deaths);
		System.out.println("|| knife   " + knife);
		System.out.println("|| perks    " + mperk);
		System.out.println("|| guns    " + mgun);
		if (this.classes.size() > 0) {
			ArrayList<String> clas = this.classes.get(0).dumpClass();
			for (int i = 0; i < clas.size(); i++) {
				System.out.println(clas.get(i));
			}
		}else{
			System.out.println("USER DOES NOT HAVE A CLASS!");
		}
		System.out.println("-------------------------");
	}

	public void delete() {
		KitPvP plugin = (KitPvP) Bukkit.getPluginManager().getPlugin("MCWarfare");
		if (plugin != null) {
			String path = plugin.getUsers() + "/" + filename;
			File f = new File(path);
			if (f.exists()) {
				f.delete();
			}
		}
	}

}
