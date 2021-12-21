package com.orange451.mcwarfare.arena;

import com.orange451.mcwarfare.FileIO;
import com.orange451.mcwarfare.KitPvP;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;

public class Clan
{
	private String name;
	private String owner;
	private File file;
	private ArrayList<String> members = new ArrayList();
	private ArrayList<String> moderators = new ArrayList();

	private ArrayList<String> moderators_new = new ArrayList();
	private ArrayList<String> members_new = new ArrayList();
	private ArrayList<String> left_new = new ArrayList();
	private String searchingFor = "";

	private boolean changed = false;

	public Clan(File file) {
		this.file = file;
	}

	public Clan(String name) {
		this.name = name;
		this.file = new File(KitPvP.getMcWar() + "/clans/" + name.toLowerCase());
	}

	public void init() {
		if ((this.members.size() > 0) && (this.owner == null))
			this.owner = ((String)this.members.get(0));
	}

	public void update(boolean checkOldValues)
	{
		if (!this.changed) {
			return;
		}
		if (this.file != null) {
			Clan temp = new Clan(this.file);
			temp.load();
			String oldName = temp.name;
			ArrayList temp_players = temp.getMembers();
			ArrayList temp_mods = temp.getModerators();
			for (int i = 0; i < this.members_new.size(); i++) {
				temp_players.add((String)this.members_new.get(i));
			}
			for (int i = 0; i < this.moderators_new.size(); i++) {
				temp_mods.add((String)this.moderators_new.get(i));
			}
			for (int i = 0; i < this.left_new.size(); i++) {
				String leaving = (String)this.left_new.get(i);
				for (int ii = temp_players.size() - 1; ii >= 0; ii--) {
					if (((String)temp_players.get(ii)).equals(leaving)) {
						temp_players.remove(ii);
					}
				}

			}

			BufferedWriter bw = FileIO.file_text_open_write(this.file.getAbsolutePath());
			if (!checkOldValues)
				FileIO.file_text_write_line(bw, "name=" + oldName);
			else
				FileIO.file_text_write_line(bw, "name=" + this.name);
			if (this.owner != null) {
				FileIO.file_text_write_line(bw, "--owner");
				FileIO.file_text_write_line(bw, this.owner);
			}

			FileIO.file_text_write_line(bw, "--moderators");
			for (int i = 0; i < temp_mods.size(); i++) {
				FileIO.file_text_write_line(bw, (String)temp_mods.get(i));
			}
			FileIO.file_text_write_line(bw, "--members");
			for (int i = 0; i < temp_players.size(); i++)
			{
				FileIO.file_text_write_line(bw, (String)temp_players.get(i));
			}
			FileIO.file_text_write_line(bw, "--end");

			FileIO.file_text_close(bw);

			this.moderators_new.clear();
			this.members_new.clear();
			this.left_new.clear();
			this.changed = false;
		} else {
			System.out.println("CANNOT SAVE FILE");
		}
	}

	public void load() {
		if ((this.file != null) && 
				(this.file.exists())) {
			ArrayList<String> file = new ArrayList<String>();
			FileInputStream fstream = null;
			DataInputStream in = null;
			BufferedReader br = null;
			InputStreamReader isr = null;
			try {
				fstream = new FileInputStream(this.file.getAbsolutePath());
				in = new DataInputStream(fstream);
				isr = new InputStreamReader(in);
				br = new BufferedReader(isr);
				String strLine;
				while ((strLine = br.readLine()) != null) {
					file.add(strLine);
				}
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
			}try {
				br.close(); } catch (Exception localException1) {
				}try { in.close(); } catch (Exception localException2) {
				}try { fstream.close(); } catch (Exception localException3) {
				}try { isr.close(); } catch (Exception localException4) {
				}
				for (int i = 0; i < file.size(); i++) {
					computeData((String)file.get(i));
				}

				init();
		}
	}

	public void computeData(String str)
	{
		try {
			if (str.equals("--end")) {
				this.searchingFor = "";
				return;
			}
			if (str.equalsIgnoreCase("--members")) {
				this.searchingFor = "members";
				return;
			}
			if (str.equalsIgnoreCase("--moderators")) {
				this.searchingFor = "moderators";
				return;
			}

			if (str.equalsIgnoreCase("--owner")) {
				this.searchingFor = "owner";
				return;
			}

			if (this.searchingFor.length() > 0) {
				if (this.searchingFor.equals("members")) {
					this.members.add(str);
					return;
				}
				if (this.searchingFor.equals("moderators")) {
					this.moderators.add(str);
					return;
				}
				if (this.searchingFor.equals("owner")) {
					this.owner = str;
				}

			}
			else if (str.indexOf("=") >= 1) {
				String str2 = str.substring(0, str.indexOf("="));
				String s_val = str.substring(str.indexOf("=") + 1);

				if (str2.equalsIgnoreCase("name"))
					this.name = s_val;
			}
		}
		catch (Exception localException) {
		}
	}

	public ArrayList<String> getMembers() {
		return this.members;
	}

	public ArrayList<String> getModerators() {
		return this.moderators;
	}

	public String getOwner() {
		return this.owner;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String string) {
		this.changed = true;
		this.name = string;
		update(true);
	}

	public void join(String string) {
		this.changed = true;
		this.members.add(string);
		this.members_new.add(string);
		this.left_new.remove(string);
		if ((this.members.size() == 1) || (this.owner == null)) {
			this.owner = string;
		}
		update(false);
	}

	public void leave(String string) {
		this.changed = true;
		for (int i = this.members.size() - 1; i >= 0; i--) {
			if (string.toLowerCase().equals(((String)this.members.get(i)).toLowerCase())) {
				this.members.remove(i);
			}
		}
		this.members_new.remove(string);
		this.left_new.add(string);
		if (this.owner.equals(string)) {
			if (this.members.size() > 0)
				this.owner = ((String)this.members.get(0));
			else {
				this.owner = null;
			}
		}
		update(false);
	}

	public boolean hasMember(String playername) {
		for (int i = this.members.size() - 1; i >= 0; i--) {
			if (((String)this.members.get(i)).equalsIgnoreCase(playername)) {
				return true;
			}
		}
		return false;
	}

	public boolean isModerator(String name2) {
		for (int i = 0; i < this.moderators.size(); i++) {
			if (((String)this.moderators.get(i)).equalsIgnoreCase(name2)) {
				return true;
			}
		}
		return false;
	}

	public void doModerator(String name) {
		if (isModerator(name))
			removeModerator(name);
		else
			giveModerator(name);
	}

	private void giveModerator(String name2)
	{
		this.moderators.add(name2);
		this.moderators_new.add(name2);
		this.changed = true;
	}

	private void removeModerator(String name2) {
		this.moderators.remove(name2);
		this.moderators_new.remove(name2);
		this.changed = true;
	}

	public void setOwner(String name2) {
		this.changed = true;
		this.owner = name2;
	}

	public String getFilename() {
		return this.file.getName();
	}
}