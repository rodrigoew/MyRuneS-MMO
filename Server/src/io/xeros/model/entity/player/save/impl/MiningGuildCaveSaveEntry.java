package io.xeros.model.entity.player.save.impl;
import io.xeros.model.entity.player.Player;import io.xeros.model.entity.player.save.PlayerSaveEntry;import java.util.List;
/** Separate field keeps the original mineral-toggle save format compatible. */
public final class MiningGuildCaveSaveEntry implements PlayerSaveEntry {
 public List<String> getKeys(Player p){return List.of("mining-guild-cave-access-v1");}
 public String encode(Player p,String key){return p.getMiningGuild().encodeCaveAccess();}
 public boolean decode(Player p,String key,String value){return p.getMiningGuild().decodeCaveAccess(value);}
 public void login(Player p){}
}
