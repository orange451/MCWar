package com.orange451.mcwarfare;

import java.io.BufferedWriter;
import org.bukkit.entity.Player;

public class VoteForServer
{
  public VoteForServer(Player pl)
  {
    this(pl.getName());
    pl = null;
  }

  public VoteForServer(String str) {
    String path = KitPvP.getFTP() + "/voted/" + str;
    BufferedWriter wr = FileIO.file_text_open_write(path);
    FileIO.file_text_write_line(wr, "grenade");
    FileIO.file_text_close(wr);
  }
}